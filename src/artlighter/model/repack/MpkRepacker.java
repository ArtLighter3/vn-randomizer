package artlighter.model.repack;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MpkRepacker implements Repacker {
    private static final int HEADER_SIZE = 8;
    private static final int FILE_HEADERS_SIZE = 56;
    private static final int FILE_HEADER_SIZE = 256;
    private static final int NAME_SIZE = 224;
    public static final byte[] MPK_HEADER = new byte[]{77, 80, 75, 0, 0, 0, 2, 0};
    private static final int LAYFILE_HEADER_SIZE = 8;
    private static final int LAYFILE_SPRITE_SIZE = 12;
    private boolean lowMemoryMode = false;

    @Override
    public MpkEntry[] getFileData(String fileName) throws IOException {
        MpkEntry[] entries;
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName))) {
            bis.readNBytes(HEADER_SIZE);
            long fileCount = Long.reverseBytes(bytesToLong(bis.readNBytes(8)));
            if (fileCount > Integer.MAX_VALUE) throw new IOException("Too many files in " + fileName);
            entries = new MpkEntry[(int) fileCount];
            bis.readNBytes(FILE_HEADERS_SIZE - HEADER_SIZE);

            for (int i = 0; i < entries.length; i++) {
                entries[i] = new MpkEntry();
                entries[i].setCompressed(bis.read() == 1);
                bis.readNBytes(3);
                entries[i].setIndex(Integer.reverseBytes(bytesToInt(bis.readNBytes(4))));
                entries[i].setPosition(Long.reverseBytes(bytesToLong(bis.readNBytes(8))));
                entries[i].setSize(Long.reverseBytes(bytesToLong(bis.readNBytes(8))));
                entries[i].setUncompressedSize(Long.reverseBytes(bytesToLong(bis.readNBytes(8))));

                byte[] nameBytes = new byte[NAME_SIZE];
                bis.read(nameBytes);
                entries[i].setFileName(new String(nameBytes, StandardCharsets.UTF_8).replace("\0", ""));
            }
            long position = HEADER_SIZE + FILE_HEADERS_SIZE + (long) entries.length * FILE_HEADER_SIZE;
            for (int i = 0; i < entries.length; i++) {
                if (!lowMemoryMode || entries[i].isLayFile()) {
                    if (entries[i].getPosition() < position) {
                        bis.reset();
                        position = 0;
                    }
                    if (entries[i].getPosition() > position) bis.readNBytes((int) (entries[i].getPosition() - position));
                    fillRawData(entries[i], bis);
                    position = entries[i].getPosition() + entries[i].getSize();
                }
            }
        }
        return entries;
    }

    @Override
    public void writeMappedFiles(String sourceFilename, String destFilename, Entry[] entries) throws IOException {
        MpkEntry[] fileData = (MpkEntry[]) entries;

        for (int i = 0; i < fileData.length; i++) {
            if (fileData[i].getMappedFile().isLayFile()) processLayFile(fileData[i], fileData[i].getMappedFile());
        }

        if (lowMemoryMode)
            Files.copy(Paths.get(sourceFilename), Paths.get(sourceFilename + "_tmp"), StandardCopyOption.REPLACE_EXISTING);
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destFilename))) {
            bos.write(MPK_HEADER);
            bos.write(longToBytes(Long.reverseBytes(fileData.length)));
            bos.write(new byte[FILE_HEADERS_SIZE - HEADER_SIZE]);

            long start = HEADER_SIZE + FILE_HEADERS_SIZE  + (long) FILE_HEADER_SIZE * fileData.length;
            long position = start;
            long[] positions = new long[fileData.length];
            for (int i = 0; i < fileData.length; i++) {
                int isCompressed = fileData[i].getMappedFile().isCompressed() ? 1 : 0;
                bos.write(intToBytes(Integer.reverseBytes(isCompressed)));
                //System.out.println(fileData[i].getMappedFile().getFileName() + " is compressed?: " + Integer.reverseBytes(isCompressed));
                bos.write(intToBytes(Integer.reverseBytes(i)));

                if (position % 2048 != 0 || position < 2048) position = (position / 2048L + 1) * 2048L;
                positions[i] = position;
                bos.write(longToBytes(Long.reverseBytes(position)));
                bos.write(longToBytes(Long.reverseBytes(fileData[i].getMappedFile().getSize())));
              //  System.out.println(fileData[i].getMappedFile().getFileName() + " " + fileData[i].getMappedFile().getSize() + " " + position);
                bos.write(longToBytes(Long.reverseBytes(fileData[i].getMappedFile().getUncompressedSize())));

                byte[] name = fileData[i].getFileName().getBytes(StandardCharsets.UTF_8);
                bos.write(name);
                if (name.length < NAME_SIZE) bos.write(new byte[NAME_SIZE - name.length]);
                position += fileData[i].getMappedFile().getSize();
            }
            long pointer = start;
            for (int i = 0; i < fileData.length; i++) {
                if (fileData[i].getMappedFile().getRawData() == null || fileData[i].getMappedFile().getRawData().isEmpty()) {
                    try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sourceFilename + "_tmp"))) {
                        bis.skipNBytes(fileData[i].getMappedFile().getPosition());
                        fillRawData(fileData[i].getMappedFile(), bis);
                    }
                }

                if (positions[i] - pointer > 0)
                    bos.write(new byte[(int) (positions[i] - pointer)]);
                List<byte[]> rawData = fileData[i].getMappedFile().getRawData();
               // System.out.println("====raw data====" + fileData[i].getMappedFile().getFileName());
               // if (fileData[i].getMappedFile().getFileName().endsWith(".lay"))
               // for (byte b : rawData.get(0)) System.out.print(b + " ");
                for (int j = 0; j < rawData.size(); j++) {
                    bos.write(rawData.get(j));
                }

                pointer = positions[i] + fileData[i].getMappedFile().getSize();
                if (lowMemoryMode && !fileData[i].getMappedFile().isLayFile())
                    fileData[i].getMappedFile().getRawData().clear();
            }
        }
        Files.deleteIfExists(Paths.get(sourceFilename + "_tmp"));
    }

    @Override
    public void setLowMemoryMode(boolean lowMemoryMode) {
        this.lowMemoryMode = lowMemoryMode;
    }
    private void processLayFile(Entry original, Entry mappedLayFile) throws IOException {
        boolean mappedLayFileCompressed = mappedLayFile.isCompressed();
        boolean originalCompressed = original.isCompressed();
        if (mappedLayFileCompressed) ZlibUtils.inflateEntry(mappedLayFile);
        if (originalCompressed) ZlibUtils.inflateEntry(original);

        byte[] buffer = mappedLayFile.getRawData().get(0);
        int spriteCount = buffer[0];
        int originalSpriteCount = original.getRawData().get(0)[0];

        //System.out.println(mappedLayFile.getFileName() + " " + spriteCount + " " + originalSpriteCount);

        if (spriteCount >= originalSpriteCount) {
            if (mappedLayFileCompressed) ZlibUtils.deflateEntry(mappedLayFile);
            if (originalCompressed) ZlibUtils.deflateEntry(original);
            return;
        }
        byte[] copycat = null;
        byte[] copycat40 = null;
        byte max20Index = 0;
        byte max40Index = 0;

        try {
            for (int i = LAYFILE_HEADER_SIZE + 3; i < LAYFILE_HEADER_SIZE + spriteCount * LAYFILE_SPRITE_SIZE; i += LAYFILE_SPRITE_SIZE) {
                if (copycat == null && buffer[i] == 32 && buffer[i + LAYFILE_SPRITE_SIZE] == 64) {
                    copycat = Arrays.copyOfRange(buffer, i - 3, (i - 3) + 4 * LAYFILE_SPRITE_SIZE);
                }
                if (copycat40 == null && buffer[i] == 64 && (buffer[i - 2] == 1 || buffer[i - 2] == 2)) {
                    copycat40 = Arrays.copyOfRange(buffer, i - 3, (i - 3) + 3 * LAYFILE_SPRITE_SIZE);
                }
                if (buffer[i] == 32 && buffer[i - 3] > max20Index) max20Index = buffer[i - 3];
                else if (buffer[i] == 64 && buffer[i - 2] > max40Index) max40Index = buffer[i - 2];
            }
        } catch (IndexOutOfBoundsException ex) {
            System.out.println(mappedLayFile.getFileName() + " : wrong format of .lay file");
            if (mappedLayFileCompressed) ZlibUtils.deflateEntry(mappedLayFile);
            if (originalCompressed) ZlibUtils.deflateEntry(original);
            return;
        }
        int diff = originalSpriteCount - spriteCount;
        if (copycat != null) {
            int newElementsCount = diff % 4 == 0 ? diff / 4 : diff / 4 + 1;
            addDataToLayFile(mappedLayFile, copycat, newElementsCount, max20Index);
        } else if (copycat40 != null) {
            //System.out.println("Should add some 40s");
            int newElementsCount = diff % 3 == 0 ? diff / 3 : diff / 3 + 1;
            addDataToLayFile(mappedLayFile, copycat40, newElementsCount, max40Index);
        }
       // System.out.println("wasCompressed = " + wasCompressed);
        if (mappedLayFileCompressed) ZlibUtils.deflateEntry(mappedLayFile);
        if (originalCompressed) ZlibUtils.deflateEntry(original);
    }

    private void addDataToLayFile(Entry mappedLayFile, byte[] copycat, int newElementsCount, byte maxIndex) throws IOException {
        byte[] buffer = mappedLayFile.getRawData().get(0);
        mappedLayFile.getRawData().remove(0);
        byte[] header;
        if (buffer[11] == 0)
            header = Arrays.copyOfRange(buffer, 0, LAYFILE_HEADER_SIZE + LAYFILE_SPRITE_SIZE);
        else header = Arrays.copyOfRange(buffer, 0, LAYFILE_HEADER_SIZE);
        byte[] firstBuffer = Arrays.copyOfRange(buffer, header.length, buffer.length);
        header[0] = (byte) (buffer[0] + newElementsCount * (copycat.length / LAYFILE_SPRITE_SIZE));
        long sizeGain = (long) newElementsCount * copycat.length;
        byte type = copycat[3];

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.writeBytes(header);
        for (int i = 0; i < newElementsCount; i++) {
            byte[] newBuffer = Arrays.copyOf(copycat, copycat.length);
            if (type == 32) {
                newBuffer[0] = ++maxIndex;
                newBuffer[13] = maxIndex;
                newBuffer[25] = maxIndex;
                newBuffer[37] = maxIndex;
            } else if (type == 64) {
                newBuffer[1] = ++maxIndex;
                newBuffer[13] = maxIndex;
                newBuffer[25] = maxIndex;
            }
            //sizeGain += 48;
            baos.write(newBuffer);
        }
        baos.write(firstBuffer);

        mappedLayFile.getRawData().add(0, baos.toByteArray());
        mappedLayFile.setUncompressedSize(mappedLayFile.getUncompressedSize()+sizeGain);
        mappedLayFile.setSize(mappedLayFile.getSize() + sizeGain);
    }
    private void fillRawData(Entry entry, InputStream is) throws IOException {
        if (entry.getRawData() == null) entry.setRawData(new ArrayList<>());
        else entry.getRawData().clear();
        byte[] rawData = new byte[2048];
        long remaining = entry.getSize();
        long readBytes = 0;
        while (remaining != 0) {
            if (readBytes == -1)
                throw new IOException("EOF reached. Unable to fill " + entry.getFileName() + " with correct data");
            int len = remaining > rawData.length ? rawData.length : (int) remaining;
            readBytes = is.read(rawData, 0, len);
            entry.addRawData(rawData, len);
            remaining -= readBytes;
        }
    }

    private byte[] longToBytes(long l) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (l & 0xFF);
            l >>= 8;
        }
        return result;
    }
    private long bytesToLong(byte[] bytes) {
        long result = 0;
        for (int i = 0; i < bytes.length; i++) {
            result = (result << 8) + (bytes[i] & 0xFF);
        }
        return result;
    }
    private byte[] intToBytes(int l) {
        byte[] result = new byte[4];
        for (int i = 3; i >= 0; i--) {
            result[i] = (byte)(l & 0xFF);
            l >>= 8;
        }
        return result;
    }
    private int bytesToInt(byte[] bytes) {
        int result = 0;
        for (int i = 0; i < bytes.length; i++) {
            result = (result << 8) + (bytes[i] & 0xFF);
        }
        return result;
    }
}
