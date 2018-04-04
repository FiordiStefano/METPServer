/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metpserver;

/**
 *
 * @author Stefano Fiordi
 */
public class FileCRCIndex {
    
    protected String path;
    protected int chunkSize;
    protected long nPackets;
    protected long fileLength;

    public FileCRCIndex(String path, int chunkSize, long nPackets, long fileLength) {
        this.path = path;
        this.chunkSize = chunkSize;
        this.nPackets = nPackets;
        this.fileLength = fileLength;
    }
    
    static {
        System.loadLibrary("FileCRCIndex");
    }
    
    private native long[] calculateFileCRC(String fileName, int chunkSize, long nPackets, long fileLength);
    
    public long[] calcDigests()
    {
        long[] digests = calculateFileCRC(path, chunkSize, nPackets, fileLength);
        
        return digests;
    }
}
