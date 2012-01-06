package com.gpl.rpg.AndorsTrail.model.item;

import java.util.Collection;

import com.gpl.rpg.AndorsTrail.controller.Constants;
import com.gpl.rpg.AndorsTrail.controller.SkillController;
import com.gpl.rpg.AndorsTrail.model.actor.Player;
import com.gpl.rpg.AndorsTrail.util.ConstRange;

public final class DropList {
	private final DropItem[] items;
	
	public DropList(DropItem[] items) {
		this.items = items;
	}
	public DropList(Collection<DropItem> items) {
		this.items = items.toArray(new DropItem[items.size()]);
	}
	public void createRandomLoot(Loot loot, Player player) {
		for (DropItem item : items) {
			
			final int chanceRollBias = SkillController.getDropChanceRollBias(item, player);
			if (Constants.rollResult(item.chance, chanceRollBias)) {
				
				final int quantityRollBias = SkillController.getDropQuantityRollBias(item, player);
				int quantity = Constants.rollValue(item.quantity, quantityRollBias);
				
				if (quantity != 0) {
					if (ItemTypeCollection.isGoldItemType(item.itemType.id)) {
						loot.gold += quantity;
					} else {
						loot.items.addItem(item.itemType, quantity);
					}
				}
			}
		}
	}

	// Unit test method. Not part of the game logic.
	public DropItem[] UNITTEST_getAllDropItems() {
		return items;
	}
	
	public static class DropItem {
		public final ItemType itemType;
		public final ConstRange chance;
		public final ConstRange quantity;
		public DropItem(ItemType itemType, ConstRange chance, ConstRange quantity) {
			this.itemType = itemType;
			this.chance = chance;
			this.quantity = quantity;
		}
	}
}
