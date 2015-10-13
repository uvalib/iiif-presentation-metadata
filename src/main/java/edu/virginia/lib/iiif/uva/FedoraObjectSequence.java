package edu.virginia.lib.iiif.uva;

import java.util.ArrayList;
import java.util.List;

import edu.virginia.lib.iiif.data.Canvas;
import edu.virginia.lib.iiif.data.Sequence;

public class FedoraObjectSequence extends Sequence {
    
    private List<Canvas> canvases;
    
    public FedoraObjectSequence(FedoraObjectManifest parent) {
        super(null, parent);
        canvases = new ArrayList<Canvas>();
        int canvasNumber = 0;
        for (final String pid : FedoraHelper.getOrderedPagePids(getManifest().getPid(), getManifest().getFedoraClient())) {
            canvases.add(new FedoraObjectCanvas(this, pid, getManifest().getIIIFHelper().getCanvasId(parent.getPid(), canvasNumber ++)));
        }
    }

    @Override
    public String getViewingHint() {
        return "paged";
    }

    @Override
    public List<Canvas> getCanvases() {
        return canvases;
    }
    
    FedoraObjectManifest getManifest() {
        return (FedoraObjectManifest) getParent();
    }

}
