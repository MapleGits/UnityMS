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

package net.sf.odinms.net.channel.handler;


import java.util.ArrayList;
import java.util.List;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleDisease;
import odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class CancelDebuffHandler extends AbstractMaplePacketHandler {
    
	private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CancelDebuffHandler.class);
	
	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		log.info(slea.toString());
		List<MapleDisease> diseases = c.getPlayer().getDiseases();
		for (MapleDisease disease : diseases) {
			List<MapleDisease> disease_ = new ArrayList<MapleDisease>();
			disease_.add(disease);
			c.getSession().write(MaplePacketCreator.cancelDebuff(disease_));
			c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.cancelForeignDebuff(c.getPlayer().getId(), disease_), false);
			c.getPlayer().removeDisease(disease);
		}
	}

}
