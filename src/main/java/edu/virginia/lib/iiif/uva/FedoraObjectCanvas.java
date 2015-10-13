package edu.virginia.lib.iiif.uva;

import java.util.ArrayList;
import java.util.List;

import edu.virginia.lib.iiif.data.Canvas;
import edu.virginia.lib.iiif.data.ImageAnnotation;

public class FedoraObjectCanvas extends Canvas {

    private String thumbnailUrl;
    
    private long width;
    
    private long height;
    
    private String label;
    
    private List<ImageAnnotation> image;
    
    public FedoraObjectCanvas(FedoraObjectSequence parent, final String pagePid, String id) {
        super(id, parent);
        final FedoraObjectManifest m = getManifest();
        thumbnailUrl = m.getIIIFHelper().getThumbnailURL(m.getPid(), pagePid);
        width = FedoraHelper.getWidth(pagePid, m.getFedoraClient());
        height = FedoraHelper.getHeight(pagePid, m.getFedoraClient());
        label = FedoraHelper.getDCTitle(pagePid, m.getFedoraClient());
        image = new ArrayList<ImageAnnotation>();
        image.add(new FedoraObjectImageAnnotation(this, pagePid));
    }

    @Override
    public String getThumbnail() {
        return thumbnailUrl;
    }

    @Override
    public long getWidth() {
        return width;
    }

    @Override
    public long getHeight() {
        return height;
    }

    @Override
    public List<ImageAnnotation> getImages() {
        return image;
    }

    @Override
    public String getLabel() {
        return label;
    }
    
    FedoraObjectManifest getManifest() {
        return (FedoraObjectManifest) getParent().getParent();
    }

}
