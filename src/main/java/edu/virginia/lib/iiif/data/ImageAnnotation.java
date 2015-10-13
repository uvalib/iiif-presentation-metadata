package edu.virginia.lib.iiif.data;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.validation.constraints.NotNull;

/**
 * In theory this would extend a generic Annotation, but 
 * since we have no use case for those yet, it's all 
 * collapsed into one class.
 */
public abstract class ImageAnnotation extends IIIFResource {

    public ImageAnnotation(String id, Canvas parent) {
        super(id, parent);
    }

    public JsonObject toJson() {
        final JsonObjectBuilder o = getJsonObject();
        o.add("motivation",  getMotivation());
        o.add("resource",  getImageResource().toJson());
        o.add("on", getParent().getId());
        return o.build();
    }
    
    public String getType() {
        return "oa:Annotation";
    }
    
    public @NotNull String getMotivation() {
        return "sc:painting";
    }
    
    public abstract @NotNull IIIFImageResource getImageResource();
    
}
