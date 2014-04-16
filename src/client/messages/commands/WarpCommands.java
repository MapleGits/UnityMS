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
package client.messages.commands;

import client.MapleCharacter;
import client.MapleClient;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.IllegalCommandSyntaxException;
import client.messages.MessageCallback;
import java.net.InetAddress;
import java.rmi.RemoteException;
import net.MaplePacket;
import net.channel.ChannelServer;
import net.world.remote.WorldChannelInterface;
import net.world.remote.WorldLocation;
import server.MaplePortal;
import server.MapleTrade;
import server.maps.MapleMap;
import tools.MaplePacketCreator;
import tools.StringUtil;

public class WarpCommands implements Command {
    @Override
    public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception,
            IllegalCommandSyntaxException {
        ChannelServer cserv = c.getChannelServer();
        if (splitted[0].equals("!warp")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim != null) {
                if (splitted.length == 2) {
                    MapleMap target = victim.getMap();
                    c.getPlayer().changeMap(target, target.findClosestSpawnpoint(victim.getPosition()));
                    if (c.getPlayer().getMap().getId() == 1 || c.getPlayer().getMap().getId() == 2) {
                        c.getSession().write(MaplePacketCreator.showApple());
                    }
                } else {
                    int mapid = Integer.parseInt(splitted[2]);
                    MapleMap target = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(mapid);
                    victim.changeMap(target, target.getPortal(0));
                    if (victim.getMap().getId() == 1 || victim.getMap().getId() == 2) {
                        victim.getClient().getSession().write(MaplePacketCreator.showApple());
                    }
                }
            } else {
                try {
                    victim = c.getPlayer();
                    WorldLocation loc = c.getChannelServer().getWorldInterface().getLocation(splitted[1]);
                    if (loc != null) {
                        mc.dropMessage("You will be cross-channel warped. This may take a few seconds.");
                        // WorldLocation loc = new WorldLocation(40000, 2);
                        MapleMap target = c.getChannelServer().getMapFactory().getMap(loc.map);
                        String ip = c.getChannelServer().getIP(loc.channel);
                        c.getPlayer().getMap().removePlayer(c.getPlayer());
                        victim.setMap(target);
                        String[] socket = ip.split(":");
                        if (c.getPlayer().getTrade() != null) {
                            MapleTrade.cancelTrade(c.getPlayer());
                        }
                        try {
                            WorldChannelInterface wci = ChannelServer.getInstance(c.getChannel()).getWorldInterface();
                            wci.addBuffsToStorage(c.getPlayer().getId(), c.getPlayer().getAllBuffs());
                            wci.addCooldownsToStorage(c.getPlayer().getId(), c.getPlayer().getAllCooldowns());
                        } catch (RemoteException e) {
                            c.getChannelServer().reconnectWorld();
                        }
                        c.getPlayer().saveToDB(true);
                        if (c.getPlayer().getCheatTracker() != null) {
                            c.getPlayer().getCheatTracker().dispose();
                        }
                        ChannelServer.getInstance(c.getChannel()).removePlayer(c.getPlayer());
                        c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
                        try {
                            MaplePacket packet = MaplePacketCreator.getChannelChange(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]));
                            c.getSession().write(packet);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        int map = Integer.parseInt(splitted[1]);
                        MapleMap target = cserv.getMapFactory().getMap(map);
                        c.getPlayer().changeMap(target, target.getPortal(0));
                    }
                    if (c.getPlayer().getMap().getId() == 1 || c.getPlayer().getMap().getId() == 2) {
                        c.getSession().write(MaplePacketCreator.showApple());
                    }
                } catch (/* Remote */Exception e) {
                    mc.dropMessage("Something went wrong " + e.getMessage());
                }
            }
        } else if (splitted[0].equals("!warphere")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            victim.changeMap(c.getPlayer().getMap(), c.getPlayer().getMap().findClosestSpawnpoint(c.getPlayer().getPosition()));
            if (victim.getMap().getId() == 1 || victim.getMap().getId() == 2) {
                victim.getClient().getSession().write(MaplePacketCreator.showApple());
            }
        } else if (splitted[0].equals("!lolcastle")) {
            if (splitted.length != 2) {
                mc.dropMessage("Syntax: !lolcastle level (level = 1-5)");
            }
            MapleMap target = c.getChannelServer().getEventSM().getEventManager("lolcastle").getInstance("lolcastle"
                    + splitted[1]).getMapFactory().getMap(990000300, false, false);
            c.getPlayer().changeMap(target, target.getPortal(0));
        } else if (splitted[0].equals("!jail")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            int mapid = 200090300; // mulung ride
            if (splitted.length > 2 && splitted[1].equals("2")) {
                mapid = 980000404; // exit for CPQ; not used
                victim = cserv.getPlayerStorage().getCharacterByName(splitted[2]);
            }
            if (victim != null) {
                MapleMap target = cserv.getMapFactory().getMap(mapid);
                MaplePortal targetPortal = target.getPortal(0);
                victim.changeMap(target, targetPortal);
                mc.dropMessage(victim.getName() + " was jailed!");
            } else {
                mc.dropMessage(splitted[1] + " not found!");
            }
        } else if (splitted[0].equals("!map")) {
            int mapid = Integer.parseInt(splitted[1]);
            MapleMap target = cserv.getMapFactory().getMap(mapid);
            MaplePortal targetPortal = null;
            if (splitted.length > 2) {
                try {
                    targetPortal = target.getPortal(Integer.parseInt(splitted[2]));
                } catch (IndexOutOfBoundsException ioobe) {
                    // noop, assume the gm didn't know how many portals there are
                } catch (NumberFormatException nfe) {
                    // noop, assume that the gm is drunk
                }
            }
            if (targetPortal == null) {
                targetPortal = target.getPortal(0);
            }
            c.getPlayer().changeMap(target, targetPortal);
        } else if (splitted[0].equals("!exprate")) { // by Redline/2azn4u
            if (splitted.length > 1) {
                int exp = Integer.parseInt(splitted[1]);
                cserv.setExpRate(exp);
                MaplePacket packet = MaplePacketCreator.serverNotice(6, "Exprate has been changed to " + exp + "x");
                ChannelServer.getInstance(c.getChannel()).broadcastPacket(packet);
            } else {
                mc.dropMessage("Syntax: !exprate <number>");
            }
        } else if (splitted[0].equals("!say")) {
            if (splitted.length > 1) {
                MaplePacket packet = MaplePacketCreator.serverNotice(6, "[" + c.getPlayer().getName() + "] " + StringUtil.joinStringFrom(splitted, 1));
                try {
                    ChannelServer.getInstance(c.getChannel()).getWorldInterface().broadcastMessage(c.getPlayer().getName(), packet.getBytes());
                } catch (RemoteException e) {
                    c.getChannelServer().reconnectWorld();
                }
            } else {
                mc.dropMessage("Syntax: !say <message>");
            }
        } else if (splitted[0].equals("!droprate")) { // by doncare aka voice123
            if (splitted.length > 1) {
                int drop = Integer.parseInt(splitted[1]);
                cserv.setDropRate(drop);
                MaplePacket packet = MaplePacketCreator.serverNotice(6, "Drop Rate has been changed to " + drop + "x");
                ChannelServer.getInstance(c.getChannel()).broadcastPacket(packet);
            } else {
                mc.dropMessage("Syntax: !droprate <number>");
            }
        } else if (splitted[0].equals("!bossdroprate")) { // by doncare aka voice123
            if (splitted.length > 1) {
                int bossdrop = Integer.parseInt(splitted[1]);
                cserv.setBossDropRate(bossdrop);
                MaplePacket packet = MaplePacketCreator.serverNotice(6, "Boss Drop Rate has been changed to " + bossdrop + "x");
                ChannelServer.getInstance(c.getChannel()).broadcastPacket(packet);
            } else {
                mc.dropMessage("Syntax: !bossdroprate <number>");
            }
        } else {
            mc.dropMessage("GM Command " + splitted[0] + " does not exist");
        }
    }

    @Override
    public CommandDefinition[] getDefinition() {
        return new CommandDefinition[]{
            new CommandDefinition("warp", "playername [targetid]", "Warps yourself to the player with the given name. When targetid is specified warps the player to the given mapid", 100),
            new CommandDefinition("warphere", "playername", "Warps the player with the given name to yourself", 100),
            new CommandDefinition("lolcastle", "[1-5]", "Warps you into Field of Judgement with the given level", 100),
            new CommandDefinition("jail", "[2] playername", "Warps the player to a map that he can't leave", 100),
            new CommandDefinition("map", "mapid", "Warps you to the given mapid (use /m instead)", 100),
            new CommandDefinition("say", "message", "Talks to the whole world in the format: [Name] message", 100),
            new CommandDefinition("droprate", "rate", "Changes the drop rate", 100),
            new CommandDefinition("bossdroprate", "rate", "Changes the boss drop rate", 100),};
    }
}
