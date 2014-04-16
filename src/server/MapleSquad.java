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
package server;

import client.MapleCharacter;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Danny
 */
public class MapleSquad {
    private MapleCharacter leader;
    private List<MapleCharacter> members = new LinkedList<MapleCharacter>();
    private List<MapleCharacter> bannedMembers = new LinkedList<MapleCharacter>();
    private int ch;

    public MapleSquad(int ch, MapleCharacter leader) {
        this.leader = leader;
        this.members.add(leader);
        this.ch = ch;
    }

    public MapleCharacter getLeader() {
        return leader;
    }

    public boolean containsMember(MapleCharacter member) {
        return members.contains(member);
    }

    public boolean isBanned(MapleCharacter member) {
        return bannedMembers.contains(member);
    }

    public List<MapleCharacter> getMembers() {
        return members;
    }

    public int getSquadSize() {
        return members.size();
    }

    public boolean addMember(MapleCharacter member) {
        if (bannedMembers.contains(member)) {
            return false;
        } else {
            members.add(member);
            return true;
        }
    }

    public void banMember(MapleCharacter member) {
        members.remove(member);
    }
}
