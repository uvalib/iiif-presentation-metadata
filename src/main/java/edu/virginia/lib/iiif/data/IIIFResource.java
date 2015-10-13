package edu.virginia.lib.iiif.data;

import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.validation.constraints.NotNull;

public abstract class IIIFResource {
    
    private String id;
    
    private IIIFResource parent;
    
    public IIIFResource(final String id) {
        this.id = id;
    }

    public IIIFResource(final String id, final IIIFResource parent) {
        this.id = id;
        this.parent = parent;
    }
    
    protected JsonObjectBuilder getJsonObject() {
        final JsonObjectBuilder o = Json.createObjectBuilder();
        if (this.parent == null) {
            o.add("@context", "http://iiif.io/api/presentation/2/context.json");
        }
        if (id != null) {
            o.add("@id", getId());
        }
        o.add("@type", getType());

        if (getLabel() != null) {
            o.add("label", getLabel());
        }
        if (getDescription() != null) {
            o.add("description", "");
        }
        
        if (getMetadata() != null) {
            JsonArrayBuilder metadata = Json.createArrayBuilder();
            for (Map.Entry<String, List<String>> metadataEntry : this.getMetadata().entrySet()) {
                for (String value : metadataEntry.getValue()) {
                    JsonObjectBuilder entry = Json.createObjectBuilder();
                    entry.add("label", metadataEntry.getKey());
                    entry.add("value", value);
                    metadata.add(entry);
                }
            }
            o.add("metadata", metadata.build());
        }
        
        return o;
    }
    
    public @NotNull String getId() {
        return this.id;
    }
    
    public IIIFResource getParent() {
        return this.parent;
    }

    public @NotNull abstract String getType();

    /**
     * Gets the resource label.  Subclasses should override this method
     * when a label is available as this default implementation returns
     * null, indicating that no there is no label;
     */
    public String getLabel() {
        return null;
    }

    /**
     * Gets the resource description.  Subclasses should override this method
     * when a description is available as this default implementation returns
     * null, indicating that no there is no description.
     */
    public String getDescription() {
        return null;
    }
    
    /**
     * Gets arbitrary metadata for the resource.  Subclasses should override this
     * method when such metadata is available as this default implementation 
     * returns null indicating that no metadata is present.
     */
    protected Map<String, List<String>> getMetadata() {
        return null;
    }

}
