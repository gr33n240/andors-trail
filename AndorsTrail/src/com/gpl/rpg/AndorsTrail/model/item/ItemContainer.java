package com.gpl.rpg.AndorsTrail.model.item;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.gpl.rpg.AndorsTrail.context.WorldContext;

public class ItemContainer {
	public final ArrayList<ItemEntry> items = new ArrayList<ItemEntry>();
	
	public ItemContainer() {}
	
	public static final class ItemEntry {
		public final ItemType itemType;
		public int quantity;
		public ItemEntry(ItemType itemType, int initialQuantity) {
			this.itemType = itemType;
			this.quantity = initialQuantity;
		}
		
		
		// ====== PARCELABLE ===================================================================

		public ItemEntry(DataInputStream src, WorldContext world) throws IOException {
			this.itemType = world.itemTypes.getItemTypeByTag(src.readUTF()); 
			this.quantity = src.readInt();
		}
		
		public void writeToParcel(DataOutputStream dest, int flags) throws IOException {
			dest.writeUTF(itemType.searchTag);
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
	
	public boolean removeItem(int itemTypeID) { return removeItem(itemTypeID, 1); }
	public boolean removeItem(int itemTypeID, int quantity) {
		int index = -1;
		ItemEntry e = null;
		for (int i = 0; i < items.size(); ++i) {
			e = items.get(i);
			if (e.itemType.id == itemTypeID) {
				index = i;
				break;
			}
		}
		if (index < 0) return false;
		if (e.quantity <= quantity) {
			items.remove(index);
		} else {
			e.quantity -= quantity;
		}
		return true;
	}
	
	public ItemEntry findItem(int itemTypeID) {
		for (ItemEntry e : items) {
			if (e.itemType.id == itemTypeID) return e;
		}
		return null;
	}
	public boolean hasItem(int itemTypeID) { return findItem(itemTypeID) != null; }
	public boolean hasItem(int itemTypeID, int minimumQuantity) { 
		ItemEntry e = findItem(itemTypeID);
		if (e == null) return false;
		return e.quantity >= minimumQuantity;
	}
	
	
	// ====== PARCELABLE ===================================================================

	public ItemContainer(DataInputStream src, WorldContext world) throws IOException {
		final int size = src.readInt();
		for(int i = 0; i < size; ++i) {
			items.add(new ItemEntry(src, world));
		}
	}
	
	public void writeToParcel(DataOutputStream dest, int flags) throws IOException {
		dest.writeInt(items.size());
		for (ItemEntry e : items) {
			e.writeToParcel(dest, flags);
		}
	}
}
