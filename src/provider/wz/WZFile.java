/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License version 3
 as published by the Free Software Foundation. You may not use, modify
 or distribute this program under any other version of the
 GNU Affero General Public License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package provider.wz;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import provider.MapleData;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataFileEntry;
import provider.MapleDataProvider;
import tools.data.input.GenericLittleEndianAccessor;
import tools.data.input.GenericSeekableLittleEndianAccessor;
import tools.data.input.InputStreamByteStream;
import tools.data.input.LittleEndianAccessor;
import tools.data.input.RandomAccessByteStream;
import tools.data.input.SeekableLittleEndianAccessor;

/*
 * This is a rather straightforward port from Maplext xentax.com/uploads/author/mrmouse/Maplext.zip unfortunately I do
 * not know who the original author is. In any case: Thanks, your rock.
 */
public class WZFile implements MapleDataProvider {
    static {
        ListWZFile.init();
    }
    private File wzfile;
    private LittleEndianAccessor lea;
    private SeekableLittleEndianAccessor slea;
    // private LittleEndianOutputStream leo;
    private Logger log = LoggerFactory.getLogger(WZFile.class);
    private int headerSize;
    private WZDirectoryEntry root;
    private boolean provideImages;
    private int cOffset;

    public WZFile(File wzfile, boolean provideImages) throws IOException {
        this.wzfile = wzfile;
        lea = new GenericLittleEndianAccessor(new InputStreamByteStream(new BufferedInputStream(new FileInputStream(wzfile))));
        RandomAccessFile raf = new RandomAccessFile(wzfile, "r");
        slea = new GenericSeekableLittleEndianAccessor(new RandomAccessByteStream(raf));
        root = new WZDirectoryEntry(wzfile.getName(), 0, 0, null);
        this.provideImages = provideImages;
        load();
    }

    @SuppressWarnings("unused")
    private void load() throws IOException {
        String sPKG = lea.readAsciiString(4);
        int size1 = lea.readInt();
        int size2 = lea.readInt();
        headerSize = lea.readInt();
        String copyright = lea.readNullTerminatedAsciiString();
        short version = lea.readShort();
        parseDirectory(root);
        cOffset = (int) lea.getBytesRead();
        getOffsets(root);
    }

	// private void writeHeader(short version) throws IOException { // pseudo header leo.writeBytes("PKG1");
    // leo.writeInt(0);
    // leo.writeInt(0);
    // leo.writeInt(0);
    // leo.writeBytes("Package file v1.0 Copyright OdinMS, Mtz");
    // leo.writeByte(0);
    // leo.writeShort(version);
    // writeDirectory(root);
    // cOffset = leo.size();
    // writeOffsets(root);
    // }
    private void getOffsets(MapleDataDirectoryEntry dir) {
        for (MapleDataFileEntry file : dir.getFiles()) {
            file.setOffset(cOffset);
            cOffset += file.getSize();
        }
        for (MapleDataDirectoryEntry sdir : dir.getSubdirectories()) {
            getOffsets(sdir);
        }
    }

    private void parseDirectory(WZDirectoryEntry dir) {
        int entries = WZTool.readValue(lea);
        for (int i = 0; i < entries; i++) {
            byte marker = lea.readByte();
            String name = null;
            @SuppressWarnings("unused")
            int dummyInt;
            int size, checksum;
            switch (marker) {
                case 0x02:
                    name = WZTool.readDecodedStringAtOffset(slea, lea.readInt() + this.headerSize + 1, false);
                    size = WZTool.readValue(lea);
                    checksum = WZTool.readValue(lea);
                    dummyInt = lea.readInt();
                    dir.addFile(new WZFileEntry(name, size, checksum, dir));
                    break;
                case 0x03:
                case 0x04:
                    name = WZTool.readDecodedString(lea, false);
                    size = WZTool.readValue(lea);
                    checksum = WZTool.readValue(lea);
                    dummyInt = lea.readInt();
                    if (marker == 3) {
                        dir.addDirectory(new WZDirectoryEntry(name, size, checksum, dir));
                    } else {
                        dir.addFile(new WZFileEntry(name, size, checksum, dir));
                    }
                    break;
                default:
                    log.error("Default case in marker ({}):/", marker);
            }
        }
        for (MapleDataDirectoryEntry idir : dir.getSubdirectories()) {
            parseDirectory((WZDirectoryEntry) idir);
        }
    }

	// private void writeDirectory(MapleDataDirectoryEntry dir) {
    // // leo.writeInt(dir.getSize());
    //
    // for (int i = 0; i < dir.getSize(); i++) {
    // byte marker = lea.readByte();
    //
    // // if ()
    //
    // String name = null;
    // @SuppressWarnings("unused")
    // int dummyInt;
    // int size, checksum;
    //
    // switch (marker) {
    // case 0x02:
    // name = WZTool.readDecodedStringAtOffset(slea, lea.readInt() + this.headerSize + 1);
    // size = WZTool.readValue(lea);
    // checksum = WZTool.readValue(lea);
    // dummyInt = lea.readInt();
    // dir.addFile(new WZFileEntry(name, size, checksum));
    // break;
    //
    // case 0x03:
    // case 0x04:
    // name = WZTool.readDecodedString(lea);
    // size = WZTool.readValue(lea);
    // checksum = WZTool.readValue(lea);
    // dummyInt = lea.readInt();
    // if (marker == 3) {
    // dir.addDirectory(new WZDirectoryEntry(name, size, checksum));
    // } else {
    // dir.addFile(new WZFileEntry(name, size, checksum));
    // }
    // break;
    // default:
    // log.error("Default case in marker ({}):/", marker);
    // }
    // }
    //
    // for (MapleDataDirectoryEntry idir : dir.getSubdirectories()) {
    // parseDirectory(idir);
    // }
    // }
    public WZIMGFile getImgFile(String path) throws IOException {
        String segments[] = path.split("/");
        WZDirectoryEntry dir = root;
        for (int x = 0; x < segments.length - 1; x++) {
            dir = (WZDirectoryEntry) dir.getEntry(segments[x]);
            if (dir == null) {
                // throw new IllegalArgumentException("File " + path + " not found in " + root.getName());
                return null;
            }
        }
        WZFileEntry entry = (WZFileEntry) dir.getEntry(segments[segments.length - 1]);
        if (entry == null) {
            return null;
        }
        String fullPath = wzfile.getName().substring(0, wzfile.getName().length() - 3).toLowerCase() + "/" + path;
        return new WZIMGFile(this.wzfile, entry, provideImages, ListWZFile.isModernImgFile(fullPath));
    }

    // XXX see if we can prevent locking here without keeping multiple handles :/
    public synchronized MapleData getData(String path) {
        try {
            WZIMGFile imgFile = getImgFile(path);
            if (imgFile == null) {
                // throw new IllegalArgumentException("File " + path + " not found in " + root.getName());
                return null;
            }
            MapleData ret = imgFile.getRoot();
            return ret;
        } catch (IOException e) {
            log.error("THROW", e);
        }
        return null;
    }

    public MapleDataDirectoryEntry getRoot() {
        return root;
    }
}
