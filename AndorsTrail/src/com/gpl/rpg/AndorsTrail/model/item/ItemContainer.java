package com.gpl.rpg.AndorsTrail.model.item;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.gpl.rpg.AndorsTrail.AndorsTrailApplication;
import com.gpl.rpg.AndorsTrail.context.WorldContext;
import com.gpl.rpg.AndorsTrail.util.L;

public class ItemContainer {
	public final ArrayList<ItemEntry> items = new ArrayList<ItemEntry>();
	
	public ItemContainer() {}
	
	public int countItems() {
		int result = 0;
		for (ItemEntry i : items) {
			result += i.quantity;
		}
		return result;
	}
	
	public static final class ItemEntry {
		public final ItemType itemType;
		public int quantity;
		public ItemEntry(ItemType itemType, int initialQuantity) {
			this.itemType = itemType;
			this.quantity = initialQuantity;
		}
		
		// ====== PARCELABLE ===================================================================

		public ItemEntry(DataInputStream src, WorldContext world, int fileversion) throws IOException {
			this.itemType = world.itemTypes.getItemType(src.readUTF()); 
			this.quantity = src.readInt();
		}
		
		public void writeToParcel(DataOutputStream dest, int flags) throws IOException {
			dest.writeUTF(itemType.id);
			dest.writeInt(quantity);
		}
	}
	
	public void addItem(ItemType itemType, int quantity) {
		ItemEntry e = findItem(itemType.id);
		if (e != null) {
			e.quantity += quantity;
		} else {
			items.add(new ItemEntry(itemType, quantity));
		}
	}
	public void addItem(ItemType itemType) { addItem(itemType, 1); }
	public void add(final ItemContainer items) {
		for (ItemEntry e : items.items) {
			addItem(e.itemType, e.quantity);
		}
	}
	public boolean isEmpty() { return items.isEmpty(); }
	
	public boolean removeItem(String itemTypeID) { return removeItem(itemTypeID, 1); }
	public boolean removeItem(String itemTypeID, int quantity) {
		int index = -1;
		ItemEntry e = null;
		for (int i = 0; i < items.size(); ++i) {
			e = items.get(i);
			if (e.itemType.id.equals(itemTypeID)) {
				index = i;
				break;
			}
		}
		if (index < 0) return false;
		if (e.quantity == quantity) {
			items.remove(index);
		} else if (e.quantity > quantity) {
			e.quantity -= quantity;
		} else {
			return false;
		}
		return true;
	}
	
	public ItemEntry findItem(String itemTypeID) {
		for (ItemEntry e : items) {
			if (e.itemType.id.equals(itemTypeID)) return e;
		}
		return null;
	}
	public boolean hasItem(String itemTypeID) { return findItem(itemTypeID) != null; }
	public boolean hasItem(String itemTypeID, int minimumQuantity) { 
		return getItemQuantity(itemTypeID) >= minimumQuantity;
	}
	
	public int getItemQuantity(String itemTypeID) { 
		ItemEntry e = findItem(itemTypeID);
		if (e == null) return 0;
		return e.quantity;
	}
	
	
	// ====== PARCELABLE ===================================================================

	public ItemContainer(DataInputStream src, WorldContext world, int fileversion) throws IOException {
		final int size = src.readInt();
		for(int i = 0; i < size; ++i) {
			ItemEntry entry = new ItemEntry(src, world, fileversion);
			if (entry.itemType != null) items.add(entry);
		}
	}
	
	public void writeToParcel(DataOutputStream dest, int flags) throws IOException {
		dest.writeInt(items.size());
		for (ItemEntry e : items) {
			e.writeToParcel(dest, flags);
		}
	}

	public static class SavegameUpdate {
		public static int refundUpgradedItems(ItemContainer container) {
			int removedCost = 0;
			for (ItemEntry e : container.items) {
				if (e.quantity >= 2 && isRefundableItem(e.itemType)) {
					if (AndorsTrailApplication.DEVELOPMENT_DEBUGMESSAGES) {
						L.log("INFO: Refunding " + (e.quantity-1) + " items of type \"" + e.itemType.id + "\" for a total of " + ((e.quantity-1) * e.itemType.fixedBaseMarketCost) + "gc.");
					}
					removedCost += (e.quantity-1) * e.itemType.fixedBaseMarketCost;
					e.quantity = 1;
				}
			}
			return removedCost;
		}

		private static boolean isRefundableItem(ItemType itemType) {
			if (itemType.hasManualPrice) return false;
			if (itemType.isQuestItem()) return false;
			if (itemType.displayType == ItemType.DISPLAYTYPE_EXTRAORDINARY) return false;
			if (itemType.displayType == ItemType.DISPLAYTYPE_LEGENDARY) return false;
			return itemType.baseMarketCost > itemType.fixedBaseMarketCost;
		}
	}
}
