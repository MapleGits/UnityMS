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

package net.sf.odinms.server.life;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleDisease;
import net.sf.odinms.client.status.MonsterStatus;
import net.sf.odinms.server.maps.MapleMapObject;
import net.sf.odinms.server.maps.MapleMapObjectType;

/**
 *
 * @author Danny (Leifde)
 */
public class MobSkill {
	
	private int skillId;
	private int skillLevel;
	private int mpCon;
	private MapleDisease diseaseType;
	private List<Integer> toSummon = new ArrayList<Integer>();
	private int spawnEffect;
	private int hp;
	private int x;
	private long duration;
	private long cooltime;
	private float prop;
	private Point lt, rb;
	private int limit;
	
	public MobSkill(int skillId, int level) {
		this.skillId = skillId;
		this.skillLevel = level;
	}
	
	public void setMpCon(int mpCon) {
		this.mpCon = mpCon;
	}
	
	public void setDiseaseType(MapleDisease diseaseType) {
		this.diseaseType = diseaseType;
	}
	
	public void addSummons(List<Integer> toSummon) {
		for (Integer summon : toSummon) {
			this.toSummon.add(summon);
		}
	}
	
	public void setSpawnEffect(int spawnEffect) {
		this.spawnEffect = spawnEffect;
	}
	
	public void setHp(int hp) {
		this.hp = hp;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public void setDuration(long duration) {
		this.duration = duration;
	}
	
	public void setCoolTime(long cooltime) {
		this.cooltime = cooltime;
	}
	
	public void setProp(float prop) {
		this.prop = prop;
	}
	
	public void setLtRb(Point lt, Point rb) {
		this.lt = lt;
		this.rb = rb;
	}
	
	public void setLimit(int limit) {
		this.limit = limit;
	}
	
	public void applyEffect(MapleCharacter player, MapleMonster monster) {
		Rectangle bounds;
		List<MapleMapObjectType> objectTypes;
		List<MapleMapObject> objects;
		switch (skillId) {
			case 100:
			case 110:
				if (lt != null && rb != null) {
					bounds = calculateBoundingBox(monster.getPosition(), monster.isFacingLeft());
					objectTypes = new ArrayList<MapleMapObjectType>();
					objectTypes.add(MapleMapObjectType.MONSTER);
					objects = monster.getMap().getMapObjectsInBox(bounds, objectTypes);
					for (MapleMapObject object : objects) {
						MapleMonster mons = (MapleMonster) object;
						if (makeChanceResult()) {
							mons.applyMonsterBuff(MonsterStatus.WEAPON_ATTACK_UP, getX(), getSkillId(), getDuration());
						}
					}
				} else {
					if (makeChanceResult()) {
						monster.applyMonsterBuff(MonsterStatus.WEAPON_ATTACK_UP, getX(), getSkillId(), getDuration());
					}
				}
				break;
			case 101:
			case 111:
				if (lt != null && rb != null) {
					bounds = calculateBoundingBox(monster.getPosition(), monster.isFacingLeft());
					objectTypes = new ArrayList<MapleMapObjectType>();
					objectTypes.add(MapleMapObjectType.MONSTER);
					objects = monster.getMap().getMapObjectsInBox(bounds, objectTypes);
					for (MapleMapObject object : objects) {
						MapleMonster mons = (MapleMonster) object;
						if (makeChanceResult()) {
							mons.applyMonsterBuff(MonsterStatus.MAGIC_ATTACK_UP, getX(), getSkillId(), getDuration());
						}
					}
				} else {
					if (makeChanceResult()) {
						monster.applyMonsterBuff(MonsterStatus.MAGIC_ATTACK_UP, getX(), getSkillId(), getDuration());
					}
				}
				break;
			case 102:
			case 112:
				if (lt != null && rb != null) {
					bounds = calculateBoundingBox(monster.getPosition(), monster.isFacingLeft());
					objectTypes = new ArrayList<MapleMapObjectType>();
					objectTypes.add(MapleMapObjectType.MONSTER);
					objects = monster.getMap().getMapObjectsInBox(bounds, objectTypes);
					for (MapleMapObject object : objects) {
						MapleMonster mons = (MapleMonster) object;
						if (makeChanceResult()) {
							mons.applyMonsterBuff(MonsterStatus.WEAPON_DEFENSE_UP, getX(), getSkillId(), getDuration());
						}
					}
				} else {
					if (makeChanceResult()) {
						monster.applyMonsterBuff(MonsterStatus.WEAPON_DEFENSE_UP, getX(), getSkillId(), getDuration());
					}
				}
				break;
			case 103:
			case 113:
				if (lt != null && rb != null) {
					bounds = calculateBoundingBox(monster.getPosition(), monster.isFacingLeft());
					objectTypes = new ArrayList<MapleMapObjectType>();
					objectTypes.add(MapleMapObjectType.MONSTER);
					objects = monster.getMap().getMapObjectsInBox(bounds, objectTypes);
					for (MapleMapObject object : objects) {
						MapleMonster mons = (MapleMonster) object;
						if (makeChanceResult()) {
							mons.applyMonsterBuff(MonsterStatus.MAGIC_DEFENSE_UP, getX(), getSkillId(), getDuration());
						}
					}
				} else {
					if (makeChanceResult()) {
						monster.applyMonsterBuff(MonsterStatus.MAGIC_DEFENSE_UP, getX(), getSkillId(), getDuration());
					}
				}
				break;
			case 114: // Heal
				// TODO
				break;
			case 120:
				if (lt != null && rb != null) {
					bounds = calculateBoundingBox(monster.getPosition(), monster.isFacingLeft());
					objectTypes = new ArrayList<MapleMapObjectType>();
					objectTypes.add(MapleMapObjectType.PLAYER);
					objects = monster.getMap().getMapObjectsInBox(bounds, objectTypes);
					for (MapleMapObject object : objects) {
						MapleCharacter chr = (MapleCharacter) object;
						if (makeChanceResult()) {
							chr.giveDebuff(MapleDisease.SEAL, this);
						}
					}
				} else {
					if (makeChanceResult()) {
						player.giveDebuff(MapleDisease.SEAL, this);
					}
				}
				break;
			case 121:
				if (lt != null && rb != null) {
					bounds = calculateBoundingBox(monster.getPosition(), monster.isFacingLeft());
					objectTypes = new ArrayList<MapleMapObjectType>();
					objectTypes.add(MapleMapObjectType.PLAYER);
					objects = monster.getMap().getMapObjectsInBox(bounds, objectTypes);
					for (MapleMapObject object : objects) {
						MapleCharacter chr = (MapleCharacter) object;
						if (makeChanceResult()) {
							chr.giveDebuff(MapleDisease.DARKNESS, this);
						}
					}
				} else {
					if (makeChanceResult()) {
						player.giveDebuff(MapleDisease.DARKNESS, this);
					}
				}
				break;
			case 122:
				if (lt != null && rb != null) {
					bounds = calculateBoundingBox(monster.getPosition(), monster.isFacingLeft());
					objectTypes = new ArrayList<MapleMapObjectType>();
					objectTypes.add(MapleMapObjectType.PLAYER);
					objects = monster.getMap().getMapObjectsInBox(bounds, objectTypes);
					for (MapleMapObject object : objects) {
						MapleCharacter chr = (MapleCharacter) object;
						if (makeChanceResult()) {
							chr.giveDebuff(MapleDisease.WEAKEN, this);
						}
					}
				} else {
					if (makeChanceResult()) {
						player.giveDebuff(MapleDisease.WEAKEN, this);
					}
				}
				break;
			case 123:
				if (lt != null && rb != null) {
					bounds = calculateBoundingBox(monster.getPosition(), monster.isFacingLeft());
					objectTypes = new ArrayList<MapleMapObjectType>();
					objectTypes.add(MapleMapObjectType.PLAYER);
					objects = monster.getMap().getMapObjectsInBox(bounds, objectTypes);
					for (MapleMapObject object : objects) {
						MapleCharacter chr = (MapleCharacter) object;
						if (makeChanceResult()) {
							chr.giveDebuff(MapleDisease.STUN, this);
						}
					}
				} else {
					if (makeChanceResult()) {
						player.giveDebuff(MapleDisease.STUN, this);
					}
				}
				break;
			case 124: // Curse
				// TODO
				break;
			case 125:
				if (lt != null && rb != null) {
					bounds = calculateBoundingBox(monster.getPosition(), monster.isFacingLeft());
					objectTypes = new ArrayList<MapleMapObjectType>();
					objectTypes.add(MapleMapObjectType.PLAYER);
					objects = monster.getMap().getMapObjectsInBox(bounds, objectTypes);
					for (MapleMapObject object : objects) {
						MapleCharacter chr = (MapleCharacter) object;
						if (makeChanceResult()) {
							chr.giveDebuff(MapleDisease.POISON, this);
						}
					}
				} else {
					if (makeChanceResult()) {
						player.giveDebuff(MapleDisease.POISON, this);
					}
				}
				break;
			case 126: // Slow
				// TODO
				break;
			case 127:
				if (lt != null && rb != null) {
					bounds = calculateBoundingBox(monster.getPosition(), monster.isFacingLeft());
					objectTypes = new ArrayList<MapleMapObjectType>();
					objectTypes.add(MapleMapObjectType.PLAYER);
					objects = monster.getMap().getMapObjectsInBox(bounds, objectTypes);
					for (MapleMapObject object : objects) {
						MapleCharacter chr = (MapleCharacter) object;
						if (makeChanceResult()) {
							chr.dispel();
						}
					}
				} else {
					if (makeChanceResult()) {
						player.dispel();
					}
				}
				break;
			case 128: // Seduce
				// TODO
				break;
			case 129: // Banish?
				// TODO
				break;
			case 140:
				if (makeChanceResult()) {
					if (!monster.isBuffed(MonsterStatus.MAGIC_IMMUNITY)) {
						monster.applyMonsterBuff(MonsterStatus.WEAPON_IMMUNITY, getX(), getSkillId(), getDuration());
					}
				}
				break;
			case 141:
				if (makeChanceResult()) {
					if (!monster.isBuffed(MonsterStatus.WEAPON_IMMUNITY)) {
						monster.applyMonsterBuff(MonsterStatus.MAGIC_IMMUNITY, getX(), getSkillId(), getDuration());
					}
				}
				break;
			case 200:
				for (Integer mobId : getSummons()) {
					MapleMonster toSpawn = MapleLifeFactory.getMonster(mobId);
					monster.getMap().spawnMonsterWithEffect(toSpawn, getSpawnEffect(), monster.getPosition());
				}
				break;
		}
		monster.usedSkill(skillId, skillLevel, cooltime);
		monster.setMp(monster.getHp() - getMpCon());
	}
	
	public int getSkillId() {
		return skillId;
	}
	
	public int getSkillLevel() {
		return skillLevel;
	}
	
	public int getMpCon() {
		return mpCon;
	}
	
	public MapleDisease getDiseaseType() {
		return diseaseType;
	}
	
	public List<Integer> getSummons() {
		return Collections.unmodifiableList(toSummon);
	}
	
	public int getSpawnEffect() {
		return spawnEffect;
	}
	
	public int getHP() {
		return hp;
	}
	
	public int getX() {
		return x;
	}
	
	public long getDuration() {
		return duration;
	}
	
	public long getCoolTime() {
		return cooltime;
	}
	
	public Point getLt() {
		return lt;
	}
	
	public Point getRb() {
		return rb;
	}
	
	public int getLimit() {
		return limit;
	}
	
	public boolean makeChanceResult() {
		return prop == 1.0 || Math.random() < prop;
	}
	
	private Rectangle calculateBoundingBox(Point posFrom, boolean facingLeft) {
		Point mylt;
		Point myrb;
		if (facingLeft) {
			mylt = new Point(lt.x + posFrom.x, lt.y + posFrom.y);
			myrb = new Point(rb.x + posFrom.x, rb.y + posFrom.y);
		} else {
			myrb = new Point(lt.x * -1 + posFrom.x, rb.y + posFrom.y);
			mylt = new Point(rb.x * -1 + posFrom.x, lt.y + posFrom.y);
		}
		Rectangle bounds = new Rectangle(mylt.x, mylt.y, myrb.x - mylt.x, myrb.y - mylt.y);
		return bounds;
	}
}
