package com.gpl.rpg.AndorsTrail.model.actor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.gpl.rpg.AndorsTrail.context.WorldContext;
import com.gpl.rpg.AndorsTrail.model.ModelContainer;
import com.gpl.rpg.AndorsTrail.model.item.Loot;
import com.gpl.rpg.AndorsTrail.util.Coord;

public final class Monster extends Actor {
	public final MonsterType monsterType;
	
	public final int millisecondsPerMove;
	public Coord movementDestination = null;
	public long nextActionTime = 0;
	public boolean forceAggressive = false;
	
	public Monster(MonsterType monsterType, Coord position) {
		super(monsterType);
		this.monsterType = monsterType;
		this.position.set(position);
		this.millisecondsPerMove = ModelContainer.millisecondsPerTurn / monsterType.getMovesPerTurn();
	}
	
	public void createLoot(Loot container) {
		container.exp += monsterType.exp;
		if (monsterType.dropList == null) return;
		monsterType.dropList.createRandomLoot(container);
	}
	
	public boolean isAgressive() {
		return monsterType.phraseID == null || forceAggressive;
	}
	
	
	// ====== PARCELABLE ===================================================================

	public static Monster readFromParcel(DataInputStream src, WorldContext world, int fileversion) throws IOException {
		MonsterType monsterType = world.monsterTypes.getMonsterType(src.readUTF());
		Coord position = new Coord(src, fileversion);
		Monster m = new Monster(monsterType, position);
		m.ap.current = src.readInt();
		m.health.current = src.readInt();
		if (fileversion >= 12) {
			m.forceAggressive = src.readBoolean();
		}
		return m;
	}
	
	public void writeToParcel(DataOutputStream dest, int flags) throws IOException {
		dest.writeUTF(monsterType.name);
		position.writeToParcel(dest, flags);
		dest.writeInt(ap.current);
		dest.writeInt(health.current);
		dest.writeBoolean(forceAggressive);
	}
}
