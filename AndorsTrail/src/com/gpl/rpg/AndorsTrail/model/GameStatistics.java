package com.gpl.rpg.AndorsTrail.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import com.gpl.rpg.AndorsTrail.context.WorldContext;
import com.gpl.rpg.AndorsTrail.model.actor.MonsterType;
import com.gpl.rpg.AndorsTrail.model.item.ItemType;

public final class GameStatistics {
	public int deaths = 0;
	public final HashMap<String, Integer> killedMonsters = new HashMap<String, Integer>();
	public final HashMap<String, Integer> usedItems = new HashMap<String, Integer>();
	public int spentGold = 0;
	
	public GameStatistics() { }
	public void addMonsterKill(MonsterType type) {
		final String n = type.name;
		if (!killedMonsters.containsKey(n)) killedMonsters.put(n, 1);
		else killedMonsters.put(n, killedMonsters.get(n) + 1);
	}
	public void addPlayerDeath(int lostExp) {
		++deaths;
	}
	public void addGoldSpent(int amount) {
		spentGold += amount;
	}
	public void addItemUsage(ItemType type) {
		final String n = type.searchTag;
		if (!usedItems.containsKey(n)) usedItems.put(n, 1);
		else usedItems.put(n, usedItems.get(n) + 1);
	}
	
	
	// ====== PARCELABLE ===================================================================

	public GameStatistics(DataInputStream src, WorldContext world, int fileversion) throws IOException {
		this.deaths = src.readInt();
		final int numMonsters = src.readInt();
		for(int i = 0; i < numMonsters; ++i) {
			final String name = src.readUTF();
			final int value = src.readInt();
			this.killedMonsters.put(name, value);
		}
		if (fileversion <= 17) return;
		
		final int numItems = src.readInt();
		for(int i = 0; i < numItems; ++i) {
			final String name = src.readUTF();
			final int value = src.readInt();
			this.usedItems.put(name, value);
		}
		this.spentGold = src.readInt();
	}
	
	public void writeToParcel(DataOutputStream dest, int flags) throws IOException {
		dest.writeInt(deaths);
		Set<Entry<String, Integer> > set = killedMonsters.entrySet();
		dest.writeInt(set.size());
		for (Entry<String, Integer> e : set) {
			dest.writeUTF(e.getKey());
			dest.writeInt(e.getValue());
		}
		set = usedItems.entrySet();
		dest.writeInt(set.size());
		for (Entry<String, Integer> e : set) {
			dest.writeUTF(e.getKey());
			dest.writeInt(e.getValue());
		}
		dest.writeInt(spentGold);
	}
}
