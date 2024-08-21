package artlighter.model.repack;

import java.util.List;

public interface Entry extends Comparable<Entry> {

    boolean isCompressed();
    void setCompressed(boolean compressed);
    String getFileName();
    long getSize();
    void setSize(long size);
    long getUncompressedSize();
    void setUncompressedSize(long uncompressedSize);
    List<byte[]> getRawData();
    Entry getMappedFile();
    void setMappedFile(Entry entry);
    void setRawData(List<byte[]> rawData);
    void addRawData(byte[] rawData, int length);

}
