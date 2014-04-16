/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy  
 Matthias Butz 
 Jan Christian Meyer 

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
 along with this program.  If not, see .
 */
package net.channel.handler;

import java.awt.Point;
import java.util.Arrays;
import java.util.List;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventoryType;
import server.MapleInventoryManipulator;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.movement.LifeMovementFragment;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.data.input.StreamUtil;

public class MovePetHandler extends AbstractMovementPacketHandler {
	//private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MovePetHandler.class);
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int petId = slea.readInt();
        slea.readInt();
        @SuppressWarnings("Unused")
        Point startPos = StreamUtil.readShortPoint(slea);
        List<LifeMovementFragment> res = parseMovement(slea);
        MapleCharacter player = c.getPlayer();
        int slot = player.getPetIndex(petId);
        if (slot == -1) {
            slot = 0;
        }
        player.getMap().broadcastMessage(player, MaplePacketCreator.movePet(player.getId(), petId, slot, res), false);
        Boolean meso = false;
        Boolean item = false;
        if (c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).findById(1812001) != null) {
            item = true;
        }
        if (c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).findById(1812000) != null) {
            meso = true;
        }
        if (meso || item) {
            List<MapleMapObject> objects = player.getMap().getMapObjectsInRange(player.getPosition(), MapleCharacter.MAX_VIEW_RANGE_SQ, Arrays.asList(MapleMapObjectType.ITEM));
            for (LifeMovementFragment move : res) {
                Point petPos = move.getPosition();
                double petX = petPos.getX();
                double petY = petPos.getY();
                for (MapleMapObject map_object : objects) {
                    Point objectPos = map_object.getPosition();
                    double objectX = objectPos.getX();
                    double objectY = objectPos.getY();
                    if (Math.abs(petX - objectX) <= 30 || Math.abs(objectX - petX) <= 30) {
                        if (Math.abs(petY - objectY) <= 30 || Math.abs(objectY - petY) <= 30) {
                            if (map_object instanceof MapleMapItem) {
                                MapleMapItem mapitem = (MapleMapItem) map_object;
                                synchronized (mapitem) {
                                    if (mapitem.isPickedUp() || mapitem.getOwner().getId() != player.getId()) {
                                        continue;
                                    }
                                    if (mapitem.getMeso() > 0 && meso) {
                                        c.getPlayer().gainMeso(mapitem.getMeso(), true, true);
                                        c.getPlayer().getMap().broadcastMessage(
                                                MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 5, c.getPlayer().getId(), true, slot),
                                                mapitem.getPosition());
                                        c.getPlayer().getMap().removeMapObject(map_object);
                                        mapitem.setPickedUp(true);
                                    } else {
                                        if (item) {
                                            StringBuilder logInfo = new StringBuilder("Picked up by ");
                                            logInfo.append(c.getPlayer().getName());
                                            if (MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), logInfo.toString())) {
                                                c.getPlayer().getMap().broadcastMessage(
                                                        MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 5, c.getPlayer().getId(), true, slot),
                                                        mapitem.getPosition());
                                                c.getPlayer().getMap().removeMapObject(map_object);
                                                mapitem.setPickedUp(true);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
