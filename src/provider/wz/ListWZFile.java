package provider.wz;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import provider.MapleDataProviderFactory;
import tools.data.input.GenericLittleEndianAccessor;
import tools.data.input.InputStreamByteStream;
import tools.data.input.LittleEndianAccessor;

public class ListWZFile {
    private LittleEndianAccessor lea;
    // will be decorated as Unmodifiable after loading is done
    private List<String> entries = new ArrayList<String>();
    private static Collection<String> modernImgs = new HashSet<String>();
    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ListWZFile.class);

    public static byte[] xorBytes(byte[] a, byte[] b) {
        byte[] wusched = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            wusched[i] = (byte) (a[i] ^ b[i]);
        }
        return wusched;
    }

    public ListWZFile(File listwz) throws FileNotFoundException {
        lea = new GenericLittleEndianAccessor(new InputStreamByteStream(new BufferedInputStream(new FileInputStream(listwz))));
        while (lea.available() > 0) {
            int l = lea.readInt();
            char[] chunk = new char[l];
            for (int i = 0; i < chunk.length; i++) {
                chunk[i] = lea.readChar();
            }
            lea.readChar();
            final String value = String.valueOf(WZTool.xorCharArray(chunk, WZTool.MODERN_UNI_KEY));
            entries.add(value);
        }
        entries = Collections.unmodifiableList(entries);
    }

    public List<String> getEntries() {
        return entries;
    }

    public static void init() {
        final String listWz = System.getProperty("listwz");
        if (listWz != null) {
            ListWZFile listwz;
            try {
                listwz = new ListWZFile(MapleDataProviderFactory.fileInWZPath("List.wz"));
                modernImgs = new HashSet<String>(listwz.getEntries());
            } catch (FileNotFoundException e) {
                log.info("listwz is set but the List.wz could not be found", e);
            }
        }
    }

    public static boolean isModernImgFile(String path) {
        return modernImgs.contains(path);
    }
}
