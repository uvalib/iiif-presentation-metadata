package edu.virginia.lib.iiif.uva;

import edu.virginia.lib.iiif.data.IIIFImageResource;
import edu.virginia.lib.iiif.data.ImageAnnotation;

public class FedoraObjectImageAnnotation extends ImageAnnotation {

    private FedoraObjectIIIFImageResource imageResource; 
    
    public FedoraObjectImageAnnotation(FedoraObjectCanvas parent, final String pagePid) {
        super(null, parent);
        imageResource = new FedoraObjectIIIFImageResource(this, pagePid);
    }

    @Override
    public IIIFImageResource getImageResource() {
        return imageResource;
    }
    
    FedoraObjectManifest getManifest() {
        return (FedoraObjectManifest) getParent().getParent().getParent();
    }

}
