package com.gpl.rpg.AndorsTrail.model.actor;

import com.gpl.rpg.AndorsTrail.model.CombatTraits;
import com.gpl.rpg.AndorsTrail.model.item.DropList;
import com.gpl.rpg.AndorsTrail.model.item.ItemTraits_OnUse;
import com.gpl.rpg.AndorsTrail.util.Size;

public final class MonsterType extends ActorTraits {
	public final String id;
	public final String spawnGroup;
	public final int exp;
	public final DropList dropList;
	public final String phraseID;
	public boolean isRespawnable = true;
	public final String faction;

	public MonsterType(
			String id, 
			String name, 
			String spawnGroup, 
			int iconID, 
			Size tileSize, 
			int maxHP, 
			int maxAP, 
			int moveCost, 
			CombatTraits baseCombatTraits, 
			ItemTraits_OnUse onHitEffects,
			int exp, 
			DropList dropList, 
			String phraseID,
			String faction) {
		super(iconID, tileSize, baseCombatTraits, moveCost, onHitEffects == null ? null : new ItemTraits_OnUse[] { onHitEffects });
		this.id = id;
		this.spawnGroup = spawnGroup;
		this.exp = exp;
		this.name = name;
		this.maxHP = maxHP;
		this.maxAP = maxAP;
		this.moveCost = moveCost;
		this.dropList = dropList;
		this.phraseID = phraseID;
		this.faction = faction;
	}
}
