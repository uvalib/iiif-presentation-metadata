package edu.virginia.lib.iiif.data;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.validation.constraints.NotNull;

public abstract class IIIFImageResource extends IIIFResource {

    public IIIFImageResource(final String id, final ImageAnnotation parent) {
        super(id, parent);
    }
    
    public JsonObject toJson() {
        final JsonObjectBuilder o = getJsonObject();
        o.add("format",  getFormat());
        o.add("width", getWidth());
        o.add("height", getHeight());
        
        final JsonObjectBuilder serviceObject = Json.createObjectBuilder();
        serviceObject.add("@context", "http://iiif.io/api/image/2/context.json");
        serviceObject.add("@id", getImageServiceURL());
        serviceObject.add("profile", getIIIFProfileURL());
        o.add("service", serviceObject);
        return o.build();
    }

    public @NotNull String getFormat() {
        return "image/jp2";
    }
    
    public abstract long getWidth();
    
    public abstract long getHeight();
    
    public abstract @NotNull String getImageServiceURL();
    
    public abstract @NotNull String getIIIFProfileURL();

    @Override
    public String getType() {
        return "dcTypes:Image";
    }

}
