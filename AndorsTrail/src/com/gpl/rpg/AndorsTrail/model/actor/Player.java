package com.gpl.rpg.AndorsTrail.model.actor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import com.gpl.rpg.AndorsTrail.AndorsTrailApplication;
import com.gpl.rpg.AndorsTrail.context.WorldContext;
import com.gpl.rpg.AndorsTrail.controller.ActorStatsController;
import com.gpl.rpg.AndorsTrail.controller.Constants;
import com.gpl.rpg.AndorsTrail.model.CombatTraits;
import com.gpl.rpg.AndorsTrail.model.item.DropListCollection;
import com.gpl.rpg.AndorsTrail.model.item.Inventory;
import com.gpl.rpg.AndorsTrail.model.item.ItemTypeCollection;
import com.gpl.rpg.AndorsTrail.model.item.Loot;
import com.gpl.rpg.AndorsTrail.model.job.Job;
import com.gpl.rpg.AndorsTrail.model.quest.QuestProgress;
import com.gpl.rpg.AndorsTrail.resource.tiles.TileManager;
import com.gpl.rpg.AndorsTrail.util.Coord;
import com.gpl.rpg.AndorsTrail.util.Range;
import com.gpl.rpg.AndorsTrail.util.Size;

public final class Player extends Actor {

    public static final int DEFAULT_PLAYER_MOVECOST = 6;

    public Job job;

    public final Coord lastPosition;
    public final Coord nextPosition;
    public int level;
    public int totalExperience;
    public final Range levelExperience; // ranges from 0 to the delta-amount of exp required for next level
    public final Inventory inventory;
    private final HashMap<String, HashSet<Integer> > questProgress = new HashMap<String, HashSet<Integer> >();
    public int useItemCost;
    public int reequipCost;
    private final HashMap<Integer, Integer> skillLevels = new HashMap<Integer, Integer>();
    public String spawnMap;
    public String spawnPlace;
    public int availableSkillIncreases = 0;
    private final HashMap<String, Integer> alignments = new HashMap<String, Integer>();

    public Player() {
        super(new ActorTraits(TileManager.CHAR_HERO, new Size(1, 1), new CombatTraits(), DEFAULT_PLAYER_MOVECOST, null), true);
        this.lastPosition = new Coord();
        this.nextPosition = new Coord();
        this.levelExperience = new Range();
        this.inventory = new Inventory();
    }

    public void initializeNewPlayer(final ItemTypeCollection types, final DropListCollection dropLists, final String name) {
        initializeNewPlayer(types, dropLists, name, Job.JOBS[0]);
    }

    // TODO: Set attributes based on Job.
    public void initializeNewPlayer(final ItemTypeCollection types, final DropListCollection dropLists,
                                    final String name, final Job job) {
        CombatTraits combat = new CombatTraits();
        combat.attackCost = 3;
        combat.attackChance = 60;
        combat.criticalChance = 0;
        combat.criticalMultiplier = 1;
        combat.damagePotential.set(1, 1);
        combat.blockChance = 0;
        combat.damageResistance = 0;

        actorTraits.baseCombatTraits.set(combat);

        actorTraits.maxAP = 10;
        actorTraits.maxHP = 25;

        actorTraits.name = name;
        actorTraits.job  = job;
        actorTraits.moveCost = DEFAULT_PLAYER_MOVECOST;
        useItemCost = 5;
        reequipCost = 5;

        level = 1;
        totalExperience = 1;
        availableSkillIncreases = 0;
        skillLevels.clear();
        alignments.clear();
        recalculateLevelExperience();

        Loot startItems = new Loot();
        dropLists.getDropList(DropListCollection.DROPLIST_STARTITEMS).createRandomLoot(startItems, this);
        inventory.add(startItems);

        if (AndorsTrailApplication.DEVELOPMENT_DEBUGRESOURCES) {
            spawnMap = "debugmap";
            spawnPlace = "start";
        } else {
            spawnMap = "home";
            spawnPlace = "rest";
        }

        ActorStatsController.recalculatePlayerCombatTraits(this);
    }

    public boolean hasExactQuestProgress(final QuestProgress progress) { return hasExactQuestProgress(progress.questID, progress.progress); }
    public boolean hasExactQuestProgress(final String questID, final int progress) {
        if (!questProgress.containsKey(questID)) return false;
        return questProgress.get(questID).contains(progress);
    }
    public boolean hasAnyQuestProgress(final String questID) {
        return questProgress.containsKey(questID);
    }
    public boolean addQuestProgress(final QuestProgress progress) {
        if (hasExactQuestProgress(progress.questID, progress.progress)) return false;
        if (!questProgress.containsKey(progress.questID)) questProgress.put(progress.questID, new HashSet<Integer>());
        questProgress.get(progress.questID).add(progress.progress);
        return true; //Progress was added.
    }

    public void recalculateLevelExperience() {
        int experienceRequiredToReachThisLevel = getRequiredExperience(level);
        levelExperience.set(getRequiredExperienceForNextLevel(level), totalExperience - experienceRequiredToReachThisLevel);
    }
    public void addExperience(final int v) {
        totalExperience += v;
        levelExperience.add(v, true);
    }

    private static int getRequiredExperience(final int currentLevel) {
        int v = 0;
        for(int i = 1; i < currentLevel; ++i) {
            v += getRequiredExperienceForNextLevel(i);
        }
        return v;
    }
    private static final int EXP_base = 55;
    private static final int EXP_D = 400;
    private static final int EXP_powbase = 2;
    private static int getRequiredExperienceForNextLevel(final int currentLevel) {
        return (int) (EXP_base * Math.pow(currentLevel, EXP_powbase + currentLevel/EXP_D));
    }

    public boolean canLevelup() {
        return levelExperience.isMax();
    }

    public int getSkillLevel(final int skillID) {
        if (!skillLevels.containsKey(skillID)) return 0;
        else return skillLevels.get(skillID);
    }
    public boolean hasSkill(final int skillID) {
        return getSkillLevel(skillID) > 0;
    }
    public void addSkillLevel(final int skillID, final boolean requireAvailableSkillpoint) {
        if (requireAvailableSkillpoint) {
            if (!hasAvailableSkillpoints()) return;
            --availableSkillIncreases;
        }
        if (!skillLevels.containsKey(skillID)) skillLevels.put(skillID, 1);
        else skillLevels.put(skillID, skillLevels.get(skillID) + 1);
        ActorStatsController.recalculatePlayerCombatTraits(this);
    }
    public boolean nextLevelAddsNewSkillpoint() {
        return thisLevelAddsNewSkillpoint(level + 1);
    }
    public static boolean thisLevelAddsNewSkillpoint(final int level) {
        return ((level - Constants.FIRST_SKILL_POINT_IS_GIVEN_AT_LEVEL) % Constants.NEW_SKILL_POINT_EVERY_N_LEVELS == 0);
    }
    public static int getExpectedNumberOfSkillpointsForLevel(int level) {
        level -= Constants.FIRST_SKILL_POINT_IS_GIVEN_AT_LEVEL;
        if (level < 0) return 0;
        return 1 + (int) Math.floor((float) level / Constants.NEW_SKILL_POINT_EVERY_N_LEVELS);
    }
    public boolean hasAvailableSkillpoints() {
        return availableSkillIncreases > 0;
    }

    public int getAlignment(final String faction) {
        Integer v = alignments.get(faction);
        if (v == null) return 0;
        return v;
    }
    public void addAlignment(final String faction, final int delta) {
        int newValue = getAlignment(faction) + delta;
        alignments.put(faction, newValue);
    }



    // ====== PARCELABLE ===================================================================

    public Player(final DataInputStream src, final WorldContext world, final int fileversion) throws IOException {
        super(src, world, fileversion, true, null);
        this.lastPosition = new Coord(src, fileversion);
        this.nextPosition = new Coord(src, fileversion);
        this.job = Job.JOBS[src.readInt()];
        this.level = src.readInt();
        this.totalExperience = src.readInt();
        this.levelExperience = new Range();
        this.recalculateLevelExperience();
        this.inventory = new Inventory(src, world, fileversion);

        if (fileversion <= 13) {
            final int size1 = src.readInt();
            for(int i = 0; i < size1; ++i) {
                String keyName = src.readUTF();
                if ("mikhail_visited".equals(keyName)) addQuestProgress(new QuestProgress("andor", 1));
                else if ("qmikhail_bread_complete".equals(keyName)) addQuestProgress(new QuestProgress("mikhail_bread", 100));
                else if ("qmikhail_bread".equals(keyName)) addQuestProgress(new QuestProgress("mikhail_bread", 10));
                else if ("qmikhail_rats_complete".equals(keyName)) addQuestProgress(new QuestProgress("mikhail_rats", 100));
                else if ("qmikhail_rats".equals(keyName)) addQuestProgress(new QuestProgress("mikhail_rats", 10));
                else if ("oromir".equals(keyName)) addQuestProgress(new QuestProgress("leta", 20));
                else if ("qleta_complete".equals(keyName)) addQuestProgress(new QuestProgress("leta", 100));
                else if ("qodair".equals(keyName)) addQuestProgress(new QuestProgress("odair", 10));
                else if ("qodair_complete".equals(keyName)) addQuestProgress(new QuestProgress("odair", 100));
                else if ("qleonid_bonemeal".equals(keyName)) {
                    addQuestProgress(new QuestProgress("bonemeal", 10));
                    addQuestProgress(new QuestProgress("bonemeal", 20));
                }
                else if ("qtharal_complete".equals(keyName)) addQuestProgress(new QuestProgress("bonemeal", 30));
                else if ("qthoronir_complete".equals(keyName)) addQuestProgress(new QuestProgress("bonemeal", 100));
                else if ("qleonid_andor".equals(keyName)) addQuestProgress(new QuestProgress("andor", 10));
                else if ("qgruil_andor".equals(keyName)) addQuestProgress(new QuestProgress("andor", 20));
                else if ("qgruil_andor_complete".equals(keyName)) addQuestProgress(new QuestProgress("andor", 30));
                else if ("qleonid_crossglen".equals(keyName)) addQuestProgress(new QuestProgress("crossglen", 1));
                else if ("qjan".equals(keyName)) addQuestProgress(new QuestProgress("jan", 10));
                else if ("qjan_complete".equals(keyName)) addQuestProgress(new QuestProgress("jan", 100));
                else if ("qbucus_thieves".equals(keyName)) addQuestProgress(new QuestProgress("andor", 40));
                else if ("qfallhaven_derelict".equals(keyName)) addQuestProgress(new QuestProgress("andor", 50));
                else if ("qfallhaven_drunk".equals(keyName)) addQuestProgress(new QuestProgress("fallhavendrunk", 10));
                else if ("qfallhaven_drunk_complete".equals(keyName)) addQuestProgress(new QuestProgress("fallhavendrunk", 100));
                else if ("qnocmar_unnmir".equals(keyName)) addQuestProgress(new QuestProgress("nocmar", 10));
                else if ("qnocmar".equals(keyName)) addQuestProgress(new QuestProgress("nocmar", 20));
                else if ("qnocmar_complete".equals(keyName)) addQuestProgress(new QuestProgress("nocmar", 200));
                else if ("qfallhaven_tavern_room2".equals(keyName)) addQuestProgress(new QuestProgress("fallhaventavern", 10));
                else if ("qarcir".equals(keyName)) addQuestProgress(new QuestProgress("arcir", 10));
                else if ("qfallhaven_oldman".equals(keyName)) addQuestProgress(new QuestProgress("calomyran", 10));
                else if ("qcalomyran_tornpage".equals(keyName)) addQuestProgress(new QuestProgress("calomyran", 20));
                else if ("qfallhaven_oldman_complete".equals(keyName)) addQuestProgress(new QuestProgress("calomyran", 100));
                else if ("qbucus".equals(keyName)) addQuestProgress(new QuestProgress("bucus", 10));
                else if ("qthoronir_catacombs".equals(keyName)) addQuestProgress(new QuestProgress("bucus", 20));
                else if ("qathamyr_complete".equals(keyName)) addQuestProgress(new QuestProgress("bucus", 40));
                else if ("qfallhaven_church".equals(keyName)) addQuestProgress(new QuestProgress("bucus", 50));
                else if ("qbucus_complete".equals(keyName)) addQuestProgress(new QuestProgress("bucus", 100));
            }
        }
        this.useItemCost = src.readInt();
        this.reequipCost = src.readInt();
        final int size2 = src.readInt();
        for(int i = 0; i < size2; ++i) {
            if (fileversion <= 21) {
                this.skillLevels.put(i, src.readInt());
            } else {
                final int skillID = src.readInt();
                this.skillLevels.put(skillID, src.readInt());
            }
        }
        this.spawnMap = src.readUTF();
        this.spawnPlace = src.readUTF();

        if (fileversion <= 12) {
            useItemCost = 5;
            health.max += 5;
            health.current += 5;
            actorTraits.maxHP += 5;
        }

        if (fileversion <= 13) return;

        final int numquests = src.readInt();
        for(int i = 0; i < numquests; ++i) {
            final String questID = src.readUTF();
            questProgress.put(questID, new HashSet<Integer>());
            final int numprogress = src.readInt();
            for(int j = 0; j < numprogress; ++j) {
                int progress = src.readInt();
                questProgress.get(questID).add(progress);
            }
        }

        if (fileversion <= 21) {
            int assignedSkillpoints = 0;
            for (int v : skillLevels.values()) assignedSkillpoints += v;
            this.availableSkillIncreases = getExpectedNumberOfSkillpointsForLevel(this.level) - assignedSkillpoints;
        } else {
            this.availableSkillIncreases = src.readInt();
        }

        if (fileversion <= 21) {
            if (hasExactQuestProgress("prim_hunt", 240)) addQuestProgress(new QuestProgress("bwm_agent", 250));
            if (hasExactQuestProgress("bwm_agent", 240)) addQuestProgress(new QuestProgress("prim_hunt", 250));
        }

        if (fileversion < 26) return;

        final int size3 = src.readInt();
        for(int i = 0; i < size3; ++i) {
            final String faction = src.readUTF();
            final int alignment = src.readInt();
            alignments.put(faction, alignment);
        }
    }

    @Override
    public void writeToParcel(final DataOutputStream dest, final int flags) throws IOException {
        super.writeToParcel(dest, flags);
        lastPosition.writeToParcel(dest, flags);
        nextPosition.writeToParcel(dest, flags);
        dest.writeInt(level);
        dest.writeInt(totalExperience);
        inventory.writeToParcel(dest, flags);
        dest.writeInt(useItemCost);
        dest.writeInt(reequipCost);
        dest.writeInt(skillLevels.size());
        for (Entry<Integer, Integer> skillLevel: skillLevels.entrySet()) {
            dest.writeInt(skillLevel.getKey());
            dest.writeInt(skillLevel.getValue());
        }
        dest.writeUTF(spawnMap);
        dest.writeUTF(spawnPlace);
        dest.writeInt(questProgress.size());
        for(Entry<String, HashSet<Integer> > e : questProgress.entrySet()) {
            dest.writeUTF(e.getKey());
            dest.writeInt(e.getValue().size());
            for(int progress : e.getValue()) {
                dest.writeInt(progress);
            }
        }
        dest.writeInt(availableSkillIncreases);
        dest.writeInt(alignments.size());
        for(Entry<String, Integer> e : alignments.entrySet()) {
            dest.writeUTF(e.getKey());
            dest.writeInt(e.getValue());
        }
    }

}
