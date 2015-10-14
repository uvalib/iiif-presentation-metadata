package edu.virginia.lib.iiif.uva;

import static com.yourmediashelf.fedora.client.FedoraClient.getDissemination;
import static com.yourmediashelf.fedora.client.FedoraClient.riSearch;
import static java.lang.Long.parseLong;
import static java.lang.System.err;
import static java.util.Objects.requireNonNull;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static javax.json.Json.createReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.json.JsonObject;

import com.yourmediashelf.fedora.client.FedoraClient;
import com.yourmediashelf.fedora.client.FedoraClientException;

public class FedoraHelper {

	public static final String IS_PAGE_OF = "http://fedora.lib.virginia.edu/relationships#isConstituentOf";
	public static final String FOLLOWS_PAGE = "http://fedora.lib.virginia.edu/relationships#isFollowingPageOf";
	private static final Pattern CSV_LINE_PATTERN = compile("\\Qinfo:fedora/\\E([^,]*),\\Qinfo:fedora/\\E([^,]*)");
	private static final Pattern URI_OBJECT_PATTERN = compile("\\Qobject : <info:fedora/\\E([^\\>]+)\\Q>\\E");
	private static final Pattern LITERAL_OBJECT_PATTERN = compile("\\Qobject : \"\\E([^\\>]+)\\Q\"\\E");;

	public static String getDCTitle(final String pid, final FedoraClient fc) {
		return getObjects(fc, pid, "http://purl.org/dc/elements/1.1/title").get(0);
	}

	public static String escapePid(final String pid) {
		return pid.replace(":", "_");
	}

	public static List<String> getOrderedPagePids(final String parentPid, final FedoraClient fc) {
		return getOrderedParts(fc, parentPid, IS_PAGE_OF, FOLLOWS_PAGE);
	}

	private static JsonObject getDjatokaMetadata(final String pid, final FedoraClient fc) {
		try {
			return createReader(
					getDissemination(pid, "djatoka:jp2SDef", "getMetadata").execute(fc).getEntityInputStream())
							.readObject();
		} catch (final FedoraClientException e) {
			throw new RuntimeException(e);
		}
	}

	public static long getWidth(final String pid, final FedoraClient fc) {
		return parseLong(getDjatokaMetadata(pid, fc).getString("width"));
	}

	public static long getHeight(final String pid, final FedoraClient fc) {
		return parseLong(getDjatokaMetadata(pid, fc).getString("height"));
	}

	public static List<String> getOrderedParts(final FedoraClient fc, final String parent,
			final String isPartOfPredicate, final String followsPredicate) {
		final String itqlQuery = "select $object $previous from <#ri> where $object <" + isPartOfPredicate
				+ "> <info:fedora/" + parent + "> and $object <" + followsPredicate + "> $previous";
		final Map<String, String> links;
		try (BufferedReader reader = queryRI(itqlQuery, "csv", fc)) {
			final Matcher m = CSV_LINE_PATTERN.matcher(reader.readLine()); // discard CSV headers
			links = reader.lines().filter(line -> m.reset(line).find()).map(line -> m.toMatchResult())
					.collect(toMap(r -> r.group(2), r -> r.group(1)));
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}

		final List<String> pids = new ArrayList<>();
		String pid = getFirstPart(fc, parent, isPartOfPredicate, followsPredicate);
		if (pid == null && !links.isEmpty()) {
			final List<String> firsts = links.keySet().stream().filter(p -> !links.containsValue(p)).collect(toList());
			if (firsts.size() > 1) throw new RuntimeException("Two \"first\" children!");
			pid = firsts.get(0);
		}
		while (pid != null) {
			pids.add(pid);
			final String nextPid = links.get(pid);
			links.remove(pid);
			pid = nextPid;
		}
		links.forEach((k, v) -> err.println(k + " --> " + v));
		if (!links.isEmpty()) throw new RuntimeException("Broken relationship chain");
		return pids;
	}

	public static String getFirstPart(final FedoraClient fc, final String parent, final String isPartOfPredicate,
			final String followsPredicate) {
		final String itqlQuery = "select $object from <#ri> where $object <" + isPartOfPredicate + "> <info:fedora/"
				+ parent + "> minus $object <" + followsPredicate + "> $other";
		try (final BufferedReader r = queryRI(itqlQuery, "simple", fc)) {
			final Matcher m = URI_OBJECT_PATTERN.matcher("");
			final List<String> pids = r.lines().filter(l -> m.reset(l).find()).map(l -> m.group(1)).collect(toList());
			if (pids.size() > 1)
				throw new RuntimeException("Multiple items are \"first\"! " + pids.get(0) + ", " + pids.get(1) + ")");
			return pids.isEmpty() ? null : pids.get(0);
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Gets the objects of the given predicate for which the subject is give given subject. For example, a relationship
	 * like "[subject] hasMarc [object]" this method would always return marc record objects for the given subject.
	 * 
	 * @param fc the fedora client that mediates access to fedora
	 * @param subject the pid of the subject that will have the given predicate relationship to all objects returned.
	 * @param predicate the predicate to query
	 * @return the URIs of the objects that are related to the given subject by the given predicate
	 * @throws IOException
	 */
	public static List<String> getObjects(final FedoraClient fc, final String subject, final String predicate) {
		final String itqlQuery = "select $object from <#ri> where "
				+ (subject != null ? "<info:fedora/" + subject + ">" : "$other") + " <"
				+ requireNonNull(predicate, "predicate must not be null!") + "> $object";
		try (final BufferedReader reader = queryRI(itqlQuery, "simple", fc)) {
			final Matcher uriMatcher = URI_OBJECT_PATTERN.matcher("");
			final Matcher literalMatcher = LITERAL_OBJECT_PATTERN.matcher("");
			final Function<String, String> getObject = line -> uriMatcher.reset(line).find() ? uriMatcher.group(1)
					: literalMatcher.reset(line).find() ? literalMatcher.group(1) : null;
			return reader.lines().map(getObject).filter(Objects::nonNull).collect(toList());
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static BufferedReader queryRI(final String query, final String format, final FedoraClient fc) {
		try {
			return new BufferedReader(new InputStreamReader(
					riSearch(query).lang("itql").format(format).execute(fc).getEntityInputStream()));
		} catch (final FedoraClientException e) {
			throw new RuntimeException(e);
		}
	}
}