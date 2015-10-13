package edu.virginia.lib.iiif.uva;

import com.yourmediashelf.fedora.client.FedoraClient;

import edu.virginia.lib.iiif.data.Manifest;
import edu.virginia.lib.iiif.data.Sequence;

public class FedoraObjectManifest extends Manifest {

    private FedoraClient fc;
    
    private IIIFHelper iiifh;
    
    private String manifestURI;
    
    private Sequence defaultSequence;
    
    private String label;
        
    private String pid;
    
    public FedoraObjectManifest(final String manifestURI, final FedoraClient fc, final String pid, final IIIFHelper iiifhelper) {
        super(manifestURI);
        this.fc = fc;
        this.pid = pid;
        this.label = FedoraHelper.getDCTitle(pid, fc);
        this.iiifh = iiifhelper;
        this.defaultSequence = new FedoraObjectSequence(this);
    }

    @Override
    protected Sequence getDefaultSequence() {
        return defaultSequence;
    }

    @Override
    public String getLabel() {
        return label;
    }
    
    FedoraClient getFedoraClient() {
        return fc;
    }
    
    String getManifestURI() {
        return manifestURI;
    }
    
    IIIFHelper getIIIFHelper() {
        return this.iiifh;
    }
    
    String getPid() {
        return pid;
    }

}
