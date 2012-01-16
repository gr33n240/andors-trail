package com.gpl.rpg.AndorsTrail.model.job;

import com.gpl.rpg.AndorsTrail.util.Range;

public final class Fighter extends Job {

    public Fighter(final int jobId) {
        super("Fighter", jobId);
        this.maxAP = 10;
        this.maxHP = 25;
        this.attackCost = 3;
        this.attackChance = 60;
        this.criticalChance = 0;
        this.criticalMultiplier = 1;
        this.damagePotential = new Range(2, 1);
        this.blockChance = 0;
        this.damageResistance = 0;
        this.moveCost = 6;
        this.useItemCost = 5;
        this.reequipCost = 5;
    }

}
