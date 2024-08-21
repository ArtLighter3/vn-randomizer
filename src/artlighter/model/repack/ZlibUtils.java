package artlighter.model.repack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class ZlibUtils {
    public static void deflateEntry(Entry entry) throws IOException {
        Deflater deflater = new Deflater();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (byte[] buffer : entry.getRawData()) baos.write(buffer);
        deflater.setInput(baos.toByteArray());
        deflater.finish();

        entry.setUncompressedSize(entry.getSize());
        byte[] buffer = new byte[2048];
        List<byte[]> newData = new ArrayList<>();
        long size = 0;
        while (!deflater.finished()) {
            int compressed = deflater.deflate(buffer);
            newData.add(Arrays.copyOfRange(buffer, 0, compressed));
            size += compressed;
        }
        deflater.end();

        entry.setRawData(newData);
        entry.setSize(size);
        entry.setCompressed(true);
    }
    public static void inflateEntry(Entry entry) throws IOException {
        Inflater inflater = new Inflater();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (byte[] buffer : entry.getRawData()) baos.write(buffer);
        inflater.setInput(baos.toByteArray());

        //byte[] buffer = new byte[2048];
        byte[] buffer = new byte[2048];
        List<byte[]> newData = new ArrayList<>();
        try {
            while (!inflater.finished()) {
                int decompressed = inflater.inflate(buffer);
                newData.add(Arrays.copyOfRange(buffer, 0, decompressed));
            }
            entry.setRawData(newData);
            entry.setSize(entry.getUncompressedSize());
            entry.setCompressed(false);
        } catch (DataFormatException ex) {
            System.out.println("Compressed " + entry.getFileName() + " has invalid data format");
        }
        inflater.end();
    }
}
