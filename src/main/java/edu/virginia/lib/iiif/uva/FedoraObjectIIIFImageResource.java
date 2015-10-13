package edu.virginia.lib.iiif.uva;

import edu.virginia.lib.iiif.data.IIIFImageResource;

public class FedoraObjectIIIFImageResource extends IIIFImageResource {

    private FedoraObjectCanvas canvas;
    
    private String imageServiceURL;
    
    public FedoraObjectIIIFImageResource(FedoraObjectImageAnnotation parent, final String pagePid) {
        super(null, parent);
        canvas = (FedoraObjectCanvas) parent.getParent();
        imageServiceURL = getManifest().getIIIFHelper().getServiceURL(getManifest().getPid(), pagePid); 
    }

    @Override
    public long getWidth() {
        return canvas.getWidth();
    }

    @Override
    public long getHeight() {
        return canvas.getHeight();
    }

    @Override
    public String getImageServiceURL() {
        return imageServiceURL;
    }

    @Override
    public String getIIIFProfileURL() {
        return "http://iiif.io/api/image/2/level1.json";
    }

    @Override
    public String getLabel() {
        return null;
    }
    
    private FedoraObjectManifest getManifest() {
        return ((FedoraObjectImageAnnotation) getParent()).getManifest();
    }

}
