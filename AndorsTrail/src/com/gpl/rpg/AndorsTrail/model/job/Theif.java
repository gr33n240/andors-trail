package com.gpl.rpg.AndorsTrail.model.job;

import com.gpl.rpg.AndorsTrail.util.Range;

public final class Theif extends Job {

    public Theif(final int jobId) {
        super("Thief", jobId);
        this.maxAP = 15;
        this.maxHP = 20;
        this.attackCost = 2;
        this.attackChance = 70;
        this.criticalChance = 1;
        this.criticalMultiplier = 2;
        this.damagePotential = new Range(1, 1);
        this.blockChance = 1;
        this.damageResistance = 0;
        this.moveCost = 5;
        this.useItemCost = 4;
        this.reequipCost = 4;
    }

}
