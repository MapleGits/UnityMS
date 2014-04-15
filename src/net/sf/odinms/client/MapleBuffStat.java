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

package net.sf.odinms.client;

import java.io.Serializable;
import odinms.net.LongValueHolder;

public enum MapleBuffStat implements LongValueHolder, Serializable {
	WATK(0x1),
	WDEF(0x2),
	MATK(0x4),
	MDEF(0x8),
	ACC(0x10),
	AVOID(0x20),
	HANDS(0x40),
	SPEED(0x80),
	JUMP(0x100),
	MAGIC_GUARD(0x200),
	DARKSIGHT(0x400), // also used by gm hide
	BOOSTER(0x800),
	POWERGUARD(0x1000),
	HYPERBODYHP(0x2000),
	HYPERBODYMP(0x4000),
	INVINCIBLE(0x8000),
	SOULARROW(0x10000),
	STUN(0x20000),
	POISON(0x40000),
	SEAL(0x80000),
	DARKNESS(0x100000),
	COMBO(0x200000),
	SUMMON(0x200000), //hack buffstat for summons ^.- (does/should not increase damage... hopefully <3)
	WK_CHARGE(0x400000),
	DRAGONBLOOD(0x800000), // another funny buffstat...
	HOLY_SYMBOL(0x1000000),
	MESOUP(0x2000000),
	SHADOWPARTNER(0x4000000),
	PICKPOCKET(0x8000000),
	PUPPET(0x8000000), // HACK - shares buffmask with pickpocket - odin special ^.-
	MESOGUARD(0x10000000),
	WEAKEN(0x40000000),
        TRANSFORM(0x200000000L),
	RECOVERY(0x400000000L),
	STANCE(0x1000000000L),
	SHARP_EYES(0x2000000000L),
	MANA_REFLECTION(0x4000000000L),
	MAPLE_WARRIOR(0x800000000L),
	SHADOW_CLAW(0x10000000000L),
	INFINITY(0x20000000000L), 
	HOLY_SHIELD(0x40000000000L),
	HAMSTRING(0x80000000000L),
	BLIND(0x100000000000L),
	CONCENTRATE(0x200000000000L), // another no op buff
	MONSTER_RIDING(0x400000000000L),
	ECHO_OF_HERO(0x1000000000000L)
	//SWITCH_CONTROLS(0x8000000000000L)
	;
	static final long serialVersionUID = 0L;
	private final long i;

	private MapleBuffStat(long i) {
		this.i = i;
	}

	@Override
	public long getValue() {
		return i;
	}
}
