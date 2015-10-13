package edu.virginia.lib.iiif.uva;

public class IIIFHelper {

    private String thumbnailPattern;
    
    private String serviceURLPattern;
    
    private String canvasIdPattern;

    /**
     * @param thumbnailPattern a URL with values such as {parentPid} and {childPid} that will be
     *        used to build the IIIF thumbnail URL.
     * @param serviceURLPattern a URL with values such as {parentPid} and {childPid} that will be
     *        used to build the IIIF service URL for the specified resources.
     * @param serviceURLPattern a URL with values such as {parentPid} and {canvasNumber} that
     *        will be used to build canvas ID for a given canvas
     */
    public IIIFHelper(final String thumbnailPattern, final String serviceURLPattern, final String canvasIdPattern) {
        this.thumbnailPattern = thumbnailPattern;
        this.serviceURLPattern = serviceURLPattern;
        this.canvasIdPattern = canvasIdPattern;
    }
    
    public String getThumbnailURL(String parentPid, String childPid) {
        return thumbnailPattern.replace("{parentPid}", FedoraHelper.escapePid(parentPid)).replace("{childPid}", FedoraHelper.escapePid(childPid));
    }
    
    public String getServiceURL(String parentPid, String childPid) {
        return serviceURLPattern.replace("{parentPid}", FedoraHelper.escapePid(parentPid)).replace("{childPid}", FedoraHelper.escapePid(childPid));
    }
    
    public String getCanvasId(String parentPid, int number) {
        return canvasIdPattern.replace("{parentPid}", FedoraHelper.escapePid(parentPid)).replace("{canvasNumber}", String.valueOf(number));
    }
    
   
}
