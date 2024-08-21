package artlighter.model.repack;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface Repacker {

    Entry[] getFileData(String fileName) throws IOException;

    void writeMappedFiles(String source, String dest, Entry[] fileData) throws IOException;
    void setLowMemoryMode(boolean lowMemoryMode);
    //privatvoid fillRawData(Entry entry, InputStream is) throws IOException;

}
