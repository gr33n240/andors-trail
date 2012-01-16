package com.gpl.rpg.AndorsTrail.model.job;

import com.gpl.rpg.AndorsTrail.util.Range;

public abstract class Job {

    /**
     * The first argument passed to the Job subclasses is the jobId (used for Parcelable)
     * and *must* be unique.
     *
     * TODO: Figure a better way of generating a unique jobId; must be an int so it can be Parcelable.
     */
    public static final Job[] JOBS = {
        new Fighter(  0),
        new Theif(    1),
        new Mage(     2),
    };

    public final String name;
    public final int jobId;

    public int maxAP;
    public int maxHP;
    public int attackCost;
    public int attackChance;
    public int criticalChance;
    public int criticalMultiplier;
    public Range damagePotential;
    public int blockChance;
    public int damageResistance;
    public int moveCost;
    public int useItemCost;
    public int reequipCost;

    public Job(final String name, final int jobId) {
        this.name  = name;
        this.jobId = jobId;
    }

}
