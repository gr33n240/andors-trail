package com.gpl.rpg.AndorsTrail.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.content.Context;

import com.gpl.rpg.AndorsTrail.AndorsTrailApplication;
import com.gpl.rpg.AndorsTrail.WorldSetup;
import com.gpl.rpg.AndorsTrail.context.WorldContext;
import com.gpl.rpg.AndorsTrail.model.actor.Player;
import com.gpl.rpg.AndorsTrail.model.map.LayeredTileMap;
import com.gpl.rpg.AndorsTrail.model.map.PredefinedMap;

public final class ModelContainer {

    public final WorldSetup worldSetup;

    public Player player;
    public InterfaceData uiSelections;
    public GameStatistics statistics;
    public PredefinedMap currentMap;
    public LayeredTileMap currentTileMap;

    public ModelContainer(final WorldSetup worldSetup) {
        this.worldSetup = worldSetup;

        this.player = new Player(worldSetup.newHeroJob);
        this.uiSelections = new InterfaceData();
        this.statistics = new GameStatistics();
    }

    // ====== PARCELABLE ===================================================================

    public ModelContainer(final DataInputStream src, final WorldContext world,
                          final int fileversion, final Context androidContext) throws IOException {
        this(AndorsTrailApplication.getApplicationFromActivityContext(androidContext).setup);

        this.player = new Player(src, world, fileversion);
        this.currentMap = world.maps.findPredefinedMap(src.readUTF());
        this.uiSelections = new InterfaceData(src, world, fileversion);
        if (uiSelections.selectedPosition != null) {
            this.uiSelections.selectedMonster = currentMap.getMonsterAt(uiSelections.selectedPosition);
        }
        this.statistics = new GameStatistics(src, world, fileversion);
        this.currentTileMap = null;
    }

    public void writeToParcel(DataOutputStream dest, int flags) throws IOException {
        player.writeToParcel(dest, flags);
        dest.writeUTF(currentMap.name);
        uiSelections.writeToParcel(dest, flags);
        statistics.writeToParcel(dest, flags);
    }

}
