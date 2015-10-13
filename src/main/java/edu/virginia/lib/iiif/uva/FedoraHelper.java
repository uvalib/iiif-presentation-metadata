package edu.virginia.lib.iiif.uva;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonObject;

import com.yourmediashelf.fedora.client.FedoraClient;
import com.yourmediashelf.fedora.client.FedoraClientException;

public class FedoraHelper {
    
    public static final String IS_PAGE_OF = "http://fedora.lib.virginia.edu/relationships#isConstituentOf";
    public static final String FOLLOWS_PAGE = "http://fedora.lib.virginia.edu/relationships#isFollowingPageOf";
    
    public static String getDCTitle(final String pid, final FedoraClient fc) {
        try {
            return getObjects(fc, pid, "http://purl.org/dc/elements/1.1/title").get(0);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String escapePid(final String pid) {
        return pid.replace(":", "_");
    }
    
    public static List<String> getOrderedPagePids(final String parentPid, final FedoraClient fc) {
        try {
            return getOrderedParts(fc, parentPid, IS_PAGE_OF, FOLLOWS_PAGE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private static JsonObject getDjatokaMetadata(final String pid, FedoraClient fc) {
        try {
            return Json.createReader(FedoraClient.getDissemination(pid, "djatoka:jp2SDef", "getMetadata").execute(fc).getEntityInputStream()).readObject();
        } catch (FedoraClientException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static long getWidth(final String pid, final FedoraClient fc) {
        return Long.parseLong(getDjatokaMetadata(pid, fc).getString("width"));
    }
    
    public static long getHeight(final String pid, final FedoraClient fc) {
        return Long.parseLong(getDjatokaMetadata(pid, fc).getString("height"));
    }
    
    public static List<String> getOrderedParts(FedoraClient fc, String parent, String isPartOfPredicate, String followsPredicate) throws Exception {
        String itqlQuery = "select $object $previous from <#ri> where $object <" + isPartOfPredicate + "> <info:fedora/" + parent + "> and $object <" + followsPredicate + "> $previous";
        BufferedReader reader = new BufferedReader(new InputStreamReader(FedoraClient.riSearch(itqlQuery).lang("itql").format("csv").execute(fc).getEntityInputStream()));
        Map<String, String> prevToNextMap = new HashMap<String, String>();
        String line = reader.readLine(); // read the csv labels
        Pattern p = Pattern.compile("\\Qinfo:fedora/\\E([^,]*),\\Qinfo:fedora/\\E([^,]*)");
        while ((line = reader.readLine()) != null) {
            Matcher m = p.matcher(line);
            if (m.matches()) {
                prevToNextMap.put(m.group(2), m.group(1));
            } else {
                throw new RuntimeException(line + " does not match pattern!");
            }
        }

        List<String> pids = new ArrayList<String>();
        String pid = getFirstPart(fc, parent, isPartOfPredicate, followsPredicate);
        if (pid == null && !prevToNextMap.isEmpty()) {
            // this is to handle some broke objects... in effect it treats
            // objects whose "previous" is not a sibling as if they had
            // no "previous"
            for (String prev : prevToNextMap.keySet()) {
                if (!prevToNextMap.values().contains(prev)) {
                    if (pid == null) {
                        pid = prev;
                    } else {
                        throw new RuntimeException("Two \"first\" children!");
                    }
                }
            }
        }
        while (pid != null) {
            pids.add(pid);
            String nextPid = prevToNextMap.get(pid);
            prevToNextMap.remove(pid);
            pid = nextPid;

        }
        if (!prevToNextMap.isEmpty()) {
            for (Map.Entry<String, String> entry : prevToNextMap.entrySet()) {
                System.err.println(entry.getKey() + " --> " + entry.getValue());
            }
            throw new RuntimeException("Broken relationship chain");
        }
        return pids;
    }
    
    public static String getFirstPart(FedoraClient fc, String parent, String isPartOfPredicate, String followsPredicate) throws Exception {
        String itqlQuery = "select $object from <#ri> where $object <" + isPartOfPredicate + "> <info:fedora/" + parent + "> minus $object <" + followsPredicate + "> $other";
        BufferedReader reader = new BufferedReader(new InputStreamReader(FedoraClient.riSearch(itqlQuery).lang("itql").format("simple").execute(fc).getEntityInputStream()));
        List<String> pids = new ArrayList<String>();
        String line = null;
        Pattern p = Pattern.compile("\\Qobject : <info:fedora/\\E([^\\>]+)\\Q>\\E");
        while ((line = reader.readLine()) != null) {
            Matcher m = p.matcher(line);
            if (m.matches()) {
                pids.add(m.group(1));
            }
        }
        if (pids.isEmpty()) {
            return null;
        } else if (pids.size() == 1) {
            return pids.get(0);
        } else {
            throw new RuntimeException("Multiple items are \"first\"! " + pids.get(0) + ", " + pids.get(1) + ")");
        }
    }
    
    /**
     * Gets the objects of the given predicate for which the subject is give given subject.
     * For example, a relationship like "[subject] hasMarc [object]" this method would always
     * return marc record objects for the given subject.
     * @param fc the fedora client that mediates access to fedora
     * @param subject the pid of the subject that will have the given predicate relationship
     * to all objects returned.
     * @param predicate the predicate to query
     * @return the URIs of the objects that are related to the given subject by the given
     * predicate
     */
    public static List<String> getObjects(FedoraClient fc, String subject, String predicate) throws Exception {
        if (predicate == null) {
            throw new NullPointerException("predicate must not be null!");
        }
        String itqlQuery = "select $object from <#ri> where " + (subject != null ? "<info:fedora/" + subject + ">" : "$other") + " <" + predicate + "> $object";
        BufferedReader reader = new BufferedReader(new InputStreamReader(FedoraClient.riSearch(itqlQuery).lang("itql").format("simple").execute(fc).getEntityInputStream()));
        List<String> pids = new ArrayList<String>();
        String line = null;
        Pattern pidPattern = Pattern.compile("\\Qobject : <info:fedora/\\E([^\\>]+)\\Q>\\E");
        while ((line = reader.readLine()) != null) {
            Matcher m = pidPattern.matcher(line);
            if (m.matches()) {
                pids.add(m.group(1));
            } else {
                Pattern literalPattern = Pattern.compile("\\Qobject : \"\\E([^\\>]+)\\Q\"\\E");
                m = literalPattern.matcher(line);
                if (m.matches()) {
                    pids.add(m.group(1));
                }
            }
        }
        return pids;
    }
    
}
