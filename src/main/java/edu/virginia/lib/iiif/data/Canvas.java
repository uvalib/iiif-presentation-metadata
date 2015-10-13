package edu.virginia.lib.iiif.data;

import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.validation.constraints.NotNull;

public abstract class Canvas extends IIIFResource {

    public Canvas(String id, Sequence parent) {
        super(id, parent);
    }

    public JsonObject toJson() {
        final JsonObjectBuilder o = getJsonObject();
        o.add("thumbnail", getThumbnail());
        o.add("width", getWidth());
        o.add("height", getHeight());
        JsonArrayBuilder imageArray = Json.createArrayBuilder();
        for (ImageAnnotation i : getImages()) {
            imageArray.add(i.toJson());
        }
        o.add("images", imageArray);
        return o.build();
    }
    
    public String getType() {
        return "SC:Canvas";
    }
    
    public abstract @NotNull String getThumbnail();
    
    public abstract long getWidth();
    
    public abstract long getHeight();
    
    public abstract @NotNull List<ImageAnnotation> getImages();

}
