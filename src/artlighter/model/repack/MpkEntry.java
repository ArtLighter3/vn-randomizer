package artlighter.model.repack;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MpkEntry implements Entry {
    private boolean compressed;
    private String fileName;
    private long size;
    private long uncompressedSize;
    private int index;
    private long position;
    private Entry mappingFile;
    private List<byte[]> rawData = new ArrayList<>();

    @Override
    public boolean isCompressed() {
        return compressed;
    }

    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public long getUncompressedSize() {
        return uncompressedSize;
    }

    public void setUncompressedSize(long uncompressedSize) {
        this.uncompressedSize = uncompressedSize;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    @Override
    public List<byte[]> getRawData() {
        return rawData;
    }

    @Override
    public MpkEntry getMappedFile() {
        if (mappingFile != null)
            return (MpkEntry) mappingFile;
        else return this;
    }

    @Override
    public void setMappedFile(Entry entry) {
        this.mappingFile = entry;
    }

    @Override
    public void setRawData(List<byte[]> rawData) {
        this.rawData = rawData;
    }

    @Override
    public void addRawData(byte[] rawData, int length) {
        this.rawData.add(Arrays.copyOf(rawData, length));
    }

    public int hashCode() {
        return 31 * fileName.hashCode();
    }

    public boolean isLayFile() {
        return fileName.endsWith(".lay");
    }
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MpkEntry)) return false;

        MpkEntry entry = (MpkEntry) obj;
        return entry.getFileName().equals(this.getFileName());
    }

    @Override
    public int compareTo(Entry o) {
        return this.getFileName().compareTo(o.getFileName());
    }
    /*public void addByte(int b) {
        this.rawData += b;
    }*/
}
