package server.life;

import client.MapleDisease;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.Pair;

public class MobSkillFactory {
    private static Map<Pair<Integer, Integer>, MobSkill> mobSkills = new HashMap<Pair<Integer, Integer>, MobSkill>();
    private static MapleDataProvider dataSource = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Data.wz" + "/Skill"));
    private static MapleData skillRoot = dataSource.getData("MobSkill.img");

    public static MobSkill getMobSkill(int skillId, int level) {
        MobSkill ret = mobSkills.get(new Pair<Integer, Integer>(Integer.valueOf(skillId), Integer.valueOf(level)));
        if (ret != null) {
            return ret;
        }
        synchronized (mobSkills) {
            // see if someone else that's also synchronized has loaded the skill by now
            ret = mobSkills.get(new Pair<Integer, Integer>(Integer.valueOf(skillId), Integer.valueOf(level)));
            if (ret == null) {
                MapleData skillData = skillRoot.getChildByPath(skillId + "/level/" + level);
                if (skillData != null) {
                    int mpCon = MapleDataTool.getInt(skillData.getChildByPath("mpCon"), 0);
                    List<Integer> toSummon = new ArrayList<Integer>();
                    for (int i = 0; i > -1; i++) {
                        if (skillData.getChildByPath(String.valueOf(i)) == null) {
                            break;
                        }
                        toSummon.add(Integer.valueOf(MapleDataTool.getInt(skillData.getChildByPath(String.valueOf(i)), 0)));
                    }
                    int effect = MapleDataTool.getInt(skillData.getChildByPath("summonEffect"), 0);
                    int hp = MapleDataTool.getInt(skillData.getChildByPath("hp"), 100);
                    int x = MapleDataTool.getInt(skillData.getChildByPath("x"), 0);
                    long duration = MapleDataTool.getInt(skillData.getChildByPath("time"), 0) * 1000;
                    long cooltime = MapleDataTool.getInt(skillData.getChildByPath("interval"), 0) * 1000;
                    float prop = MapleDataTool.getInt(skillData.getChildByPath("prop"), 100) / 100;
                    int limit = MapleDataTool.getInt(skillData.getChildByPath("limit"), 0);
                    MapleData ltd = skillData.getChildByPath("lt");
                    Point lt = null;
                    Point rb = null;
                    if (ltd != null) {
                        lt = (Point) ltd.getData();
                        rb = (Point) skillData.getChildByPath("rb").getData();
                    }
                    ret = new MobSkill(skillId, level);
                    ret.addSummons(toSummon);
                    ret.setCoolTime(cooltime);
                    ret.setDiseaseType(MapleDisease.getType(skillId));
                    ret.setDuration(duration);
                    ret.setHp(hp);
                    ret.setMpCon(mpCon);
                    ret.setSpawnEffect(effect);
                    ret.setX(x);
                    ret.setProp(prop);
                    ret.setLimit(limit);
                }
                mobSkills.put(new Pair<Integer, Integer>(Integer.valueOf(skillId), Integer.valueOf(level)), ret);
            }
            return ret;
        }
    }
}
