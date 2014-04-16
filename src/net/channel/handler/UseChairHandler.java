package net.channel.handler;

import client.IItem;
import client.MapleClient;
import client.MapleInventoryType;
import client.anticheat.CheatingOffense;
import net.AbstractMaplePacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class UseChairHandler extends AbstractMaplePacketHandler {
    private static Logger log = LoggerFactory.getLogger(UseItemHandler.class);

    public UseChairHandler() {
    }

    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int itemId = slea.readInt();
        IItem toUse = c.getPlayer().getInventory(MapleInventoryType.SETUP).findById(itemId);
        if (toUse == null) {
            log.info("[h4x] Player {} is using an item he does not have: {}", c.getPlayer().getName(), Integer.valueOf(itemId));
            c.getPlayer().getCheatTracker().registerOffense(CheatingOffense.USING_UNAVAILABLE_ITEM, Integer.toString(itemId));
        } else {
            c.getPlayer().setChair(itemId);
            c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.showChair(c.getPlayer().getId(), itemId), false);
        }
        c.getSession().write(MaplePacketCreator.enableActions());
    }
}
