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
package net.login.handler;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.data.input.SeekableLittleEndianAccessor;

public class CharlistRequestHandler extends AbstractMaplePacketHandler {
    private static Logger log = LoggerFactory.getLogger(CharlistRequestHandler.class);

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int server = slea.readByte();
        int channel = slea.readByte() + 1;
        c.setWorld(server);
        log.info("Client is connecting to server {} channel {}", server, channel);
        c.setChannel(channel);
        c.sendCharList(server);
    }
}
