package edu.virginia.lib.iiif.data;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public abstract class Manifest extends IIIFResource {

    public Manifest(final String id) {
        super(id);
    }

    public JsonObject toJson() {
        final JsonObjectBuilder o = getJsonObject();
        JsonArrayBuilder sequenceArray = Json.createArrayBuilder();
        sequenceArray.add(getDefaultSequence().toJson());
        o.add("sequences", sequenceArray);
        return o.build();
    }

    public String getType() {
        return "SC:Manifest";
    }
    
    protected abstract Sequence getDefaultSequence();

}
