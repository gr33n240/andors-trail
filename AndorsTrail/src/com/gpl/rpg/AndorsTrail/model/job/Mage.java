package com.gpl.rpg.AndorsTrail.model.job;

import com.gpl.rpg.AndorsTrail.util.Range;

public final class Mage extends Job {

    public Mage(final int jobId) {
        super("Mage", jobId);
        this.maxAP = 13;
        this.maxHP = 15;
        this.attackCost = 2;
        this.attackChance = 60;
        this.criticalChance = 0;
        this.criticalMultiplier = 1;
        this.damagePotential = new Range(1, 1);
        this.blockChance = 1;
        this.damageResistance = 0;
        this.moveCost = 5;
        this.useItemCost = 3;
        this.reequipCost = 3;
    }

}
