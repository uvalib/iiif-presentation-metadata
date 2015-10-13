package edu.virginia.lib.iiif.data;

import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.validation.constraints.NotNull;

public abstract class Sequence extends IIIFResource {

    public Sequence(String id, Manifest parent) {
        super(id, parent);
    }

    public JsonObject toJson() {
        JsonObjectBuilder o = getJsonObject();

        if (getViewingHint() != null) {
            o.add("viewingHint", getViewingHint());
        }

        JsonArrayBuilder canvasArray = Json.createArrayBuilder();
        for (Canvas c : getCanvases()) {
            canvasArray.add(c.toJson());
        }
        o.add("canvases", canvasArray);
        
        return o.build();
    }

    @Override
    public String getType() {
        return "sc:Sequence";
    }

    public abstract @NotNull String getViewingHint();
    
    public abstract @NotNull List<Canvas> getCanvases();

}
