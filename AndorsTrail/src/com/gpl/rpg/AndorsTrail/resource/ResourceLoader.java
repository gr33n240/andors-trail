package com.gpl.rpg.AndorsTrail.resource;

import java.util.Collection;

import com.gpl.rpg.AndorsTrail.AndorsTrailApplication;
import com.gpl.rpg.AndorsTrail.R;
import com.gpl.rpg.AndorsTrail.context.WorldContext;
import com.gpl.rpg.AndorsTrail.conversation.ConversationCollection;
import com.gpl.rpg.AndorsTrail.model.map.TMXMapTranslator;
import com.gpl.rpg.AndorsTrail.resource.parsers.ActorConditionsTypeParser;
import com.gpl.rpg.AndorsTrail.resource.parsers.ConversationListParser;
import com.gpl.rpg.AndorsTrail.resource.parsers.DropListParser;
import com.gpl.rpg.AndorsTrail.resource.parsers.ItemTypeParser;
import com.gpl.rpg.AndorsTrail.resource.parsers.MonsterTypeParser;
import com.gpl.rpg.AndorsTrail.resource.parsers.QuestParser;
import com.gpl.rpg.AndorsTrail.util.L;
import com.gpl.rpg.AndorsTrail.util.Size;

import android.content.res.Resources;
import android.content.res.TypedArray;

public final class ResourceLoader {

	private static final int actorConditionsResourceId = R.array.loadresource_actorconditions;
	private static final int itemsResourceId = AndorsTrailApplication.DEVELOPMENT_DEBUGRESOURCES ? R.array.loadresource_items_debug : R.array.loadresource_items;
	private static final int droplistsResourceId = AndorsTrailApplication.DEVELOPMENT_DEBUGRESOURCES ? R.array.loadresource_droplists_debug : R.array.loadresource_droplists;
	private static final int questsResourceId = AndorsTrailApplication.DEVELOPMENT_DEBUGRESOURCES ? R.array.loadresource_quests_debug : R.array.loadresource_quests;
	private static final int conversationsListsResourceId = AndorsTrailApplication.DEVELOPMENT_DEBUGRESOURCES ? R.array.loadresource_conversationlists_debug : R.array.loadresource_conversationlists;
	private static final int monstersResourceId = AndorsTrailApplication.DEVELOPMENT_DEBUGRESOURCES ? R.array.loadresource_monsters_debug : R.array.loadresource_monsters;
	private static final int mapsResourceId = AndorsTrailApplication.DEVELOPMENT_DEBUGRESOURCES ? R.array.loadresource_maps_debug : R.array.loadresource_maps;
    
	private static long taskStart;
	private static void timingCheckpoint(String loaderName) {
		long now = System.currentTimeMillis();
		long duration = now - taskStart;
    	L.log(loaderName + " ran for " + duration + " ms.");
    	taskStart = now;
    }
    
	public static void loadResources(WorldContext world, Resources r) {
    	long start = System.currentTimeMillis();
    	taskStart = start;
    	
        final int mTileSize = world.tileManager.tileSize;
        
        DynamicTileLoader loader = new DynamicTileLoader(world.tileManager.tileCache);
        prepareTilesets(loader, mTileSize);
        if (AndorsTrailApplication.DEVELOPMENT_DEBUGMESSAGES) timingCheckpoint("prepareTilesets");
        
        // ========================================================================
        // Load various ui icons
        /*TileManager.iconID_CHAR_HERO = */loader.prepareTileID(R.drawable.char_hero, 0);
        /*TileManager.iconID_selection_red = */loader.prepareTileID(R.drawable.ui_selections, 0);
        /*TileManager.iconID_selection_yellow = */loader.prepareTileID(R.drawable.ui_selections, 1);
        /*TileManager.iconID_groundbag = */loader.prepareTileID(R.drawable.ui_icon_equipment, 0);
    	/*TileManager.iconID_boxopened = */loader.prepareTileID(R.drawable.ui_quickslots, 1);
        /*TileManager.iconID_boxclosed = */loader.prepareTileID(R.drawable.ui_quickslots, 0);
        /*TileManager.iconID_selection_blue = */loader.prepareTileID(R.drawable.ui_selections, 2);
        /*TileManager.iconID_selection_purple = */loader.prepareTileID(R.drawable.ui_selections, 3);
        /*TileManager.iconID_selection_green = */loader.prepareTileID(R.drawable.ui_selections, 4);
        for(int i = 0; i < 5; ++i) {
        	loader.prepareTileID(R.drawable.ui_splatters1, i);
            loader.prepareTileID(R.drawable.ui_splatters1, i+8);
        }
        
        
        // ========================================================================
        // Load effects
        world.visualEffectTypes.initialize(loader);
        if (AndorsTrailApplication.DEVELOPMENT_DEBUGMESSAGES) timingCheckpoint("VisualEffectLoader");
        
        // ========================================================================
        // Load skills
        world.skills.initialize();
        if (AndorsTrailApplication.DEVELOPMENT_DEBUGMESSAGES) timingCheckpoint("SkillLoader");
        
    	// ========================================================================
        // Load condition types
        final ActorConditionsTypeParser actorConditionsTypeParser = new ActorConditionsTypeParser(loader);
        final TypedArray conditionsToLoad = r.obtainTypedArray(actorConditionsResourceId);
        for (int i = 0; i < conditionsToLoad.length(); ++i) {
        	world.actorConditionsTypes.initialize(actorConditionsTypeParser, conditionsToLoad.getString(i));	
        }
        if (AndorsTrailApplication.DEVELOPMENT_DEBUGMESSAGES) timingCheckpoint("ActorConditionsTypeParser");
        
        // ========================================================================
        // Load preloaded tiles
        loader.flush();
        world.tileManager.loadPreloadedTiles(r);
        
        
        // ========================================================================
        // Load items
        final ItemTypeParser itemTypeParser = new ItemTypeParser(loader, world.actorConditionsTypes);
        final TypedArray itemsToLoad = r.obtainTypedArray(itemsResourceId);
        for (int i = 0; i < itemsToLoad.length(); ++i) {
        	world.itemTypes.initialize(itemTypeParser, itemsToLoad.getString(i));	
        }
        if (AndorsTrailApplication.DEVELOPMENT_DEBUGMESSAGES) timingCheckpoint("ItemTypeParser");
        
        
        // ========================================================================
        // Load droplists
        final DropListParser dropListParser = new DropListParser(world.itemTypes);
        final TypedArray droplistsToLoad = r.obtainTypedArray(droplistsResourceId);
        for (int i = 0; i < droplistsToLoad.length(); ++i) {
        	world.dropLists.initialize(dropListParser, droplistsToLoad.getString(i));
        }
        if (AndorsTrailApplication.DEVELOPMENT_DEBUGMESSAGES) timingCheckpoint("DropListParser");
        
        
        // ========================================================================
        // Load quests
        final QuestParser questParser = new QuestParser();
        final TypedArray questsToLoad = r.obtainTypedArray(questsResourceId);
        for (int i = 0; i < questsToLoad.length(); ++i) {
        	world.quests.initialize(questParser, questsToLoad.getString(i));
        }
        if (AndorsTrailApplication.DEVELOPMENT_DEBUGMESSAGES) timingCheckpoint("QuestParser");
    	

        // ========================================================================
        // Load conversations
        final ConversationListParser conversationListParser = new ConversationListParser();
        final TypedArray conversationsListsToLoad = r.obtainTypedArray(conversationsListsResourceId);
        for (int i = 0; i < conversationsListsToLoad.length(); ++i) {
        	ConversationCollection conversations = new ConversationCollection();
        	Collection<String> ids = conversations.initialize(conversationListParser, conversationsListsToLoad.getString(i));
        	world.conversationLoader.addIDs(conversationsListsToLoad.getResourceId(i, -1), ids);
        }
        if (AndorsTrailApplication.DEVELOPMENT_DEBUGMESSAGES) timingCheckpoint("ConversationListParser");
        
        
        // ========================================================================
        // Load monsters
        final MonsterTypeParser monsterTypeParser = new MonsterTypeParser(world.dropLists, world.actorConditionsTypes, loader);
        final TypedArray monstersToLoad = r.obtainTypedArray(monstersResourceId);
        for (int i = 0; i < monstersToLoad.length(); ++i) {
        	world.monsterTypes.initialize(monsterTypeParser, monstersToLoad.getString(i));
        }
        if (AndorsTrailApplication.DEVELOPMENT_DEBUGMESSAGES) timingCheckpoint("MonsterTypeParser");
        
        
        // ========================================================================
        // Load maps
        TMXMapTranslator mapReader = new TMXMapTranslator();
        final TypedArray mapsToLoad = r.obtainTypedArray(mapsResourceId);
        for (int i = 0; i < mapsToLoad.length(); ++i) {
        	final int mapResourceId = mapsToLoad.getResourceId(i, -1);
        	final String mapName = r.getResourceEntryName(mapResourceId);
        	mapReader.read(r, mapResourceId, mapName);
        }
        if (AndorsTrailApplication.DEVELOPMENT_DEBUGMESSAGES) timingCheckpoint("TMXMapReader");
        world.maps.predefinedMaps.addAll(mapReader.transformMaps(loader, world.monsterTypes, world.dropLists));
        mapReader = null;
        if (AndorsTrailApplication.DEVELOPMENT_DEBUGMESSAGES) timingCheckpoint("mapReader.transformMaps");
        
        
        // ========================================================================
        // Load graphics resources (icons and tiles)
        loader.flush();
        loader = null;
        if (AndorsTrailApplication.DEVELOPMENT_DEBUGMESSAGES) timingCheckpoint("DynamicTileLoader");
        // ========================================================================
        

        if (AndorsTrailApplication.DEVELOPMENT_DEBUGMESSAGES) {
        	long duration = System.currentTimeMillis() - start;
        	L.log("ResourceLoader ran for " + duration + " ms.");
        }
    }

	private static void prepareTilesets(DynamicTileLoader loader, int mTileSize) {
		final Size dst_sz1x1 = new Size(mTileSize, mTileSize);
        final Size dst_sz2x2 = new Size(mTileSize*2, mTileSize*2);
        final Size defaultTileSize = dst_sz1x1;
        final Size src_sz1x1 = new Size(1, 1);
        final Size src_sz6x1 = new Size(6, 1);
        final Size src_sz7x1 = new Size(7, 1);
        final Size src_mapTileSize = new Size(16, 8);
        final Size src_mapTileSize7 = new Size(16, 7);
        
        loader.prepareTileset(R.drawable.char_hero, "char_hero", src_sz1x1, defaultTileSize);
        
        loader.prepareTileset(R.drawable.ui_selections, "ui_selections", new Size(5, 1), defaultTileSize);
        loader.prepareTileset(R.drawable.ui_quickslots, "ui_quickslots", new Size(2, 1), defaultTileSize);
        loader.prepareTileset(R.drawable.ui_icon_equipment, "ui_icon_equipment", src_sz1x1, defaultTileSize);
        loader.prepareTileset(R.drawable.ui_splatters1, "ui_splatters1", new Size(8, 2), defaultTileSize);
        
        loader.prepareTileset(R.drawable.actorconditions_1, "actorconditions_1", new Size(14, 8), defaultTileSize);
        loader.prepareTileset(R.drawable.actorconditions_2, "actorconditions_2", new Size(3, 1), defaultTileSize);
        
        loader.prepareTileset(R.drawable.items_armours, "items_armours", new Size(14, 3), defaultTileSize);
        loader.prepareTileset(R.drawable.items_weapons, "items_weapons", new Size(14, 6), defaultTileSize);
        loader.prepareTileset(R.drawable.items_jewelry, "items_jewelry", new Size(14, 1), defaultTileSize);
        loader.prepareTileset(R.drawable.items_consumables, "items_consumables", new Size(14, 5), defaultTileSize);
        loader.prepareTileset(R.drawable.items_books, "items_books", new Size(11, 1), defaultTileSize);
        loader.prepareTileset(R.drawable.items_misc, "items_misc", new Size(14, 4), defaultTileSize);
        loader.prepareTileset(R.drawable.items_necklaces_1, "items_necklaces_1", new Size(10, 3), defaultTileSize);
        loader.prepareTileset(R.drawable.items_weapons_3, "items_weapons_3", new Size(13, 5), defaultTileSize);
        loader.prepareTileset(R.drawable.items_armours_2, "items_armours_2", new Size(7, 1), defaultTileSize);
        loader.prepareTileset(R.drawable.items_armours_3, "items_armours_3", new Size(10, 4), defaultTileSize);
        
        loader.prepareTileset(R.drawable.monsters_demon1, "monsters_demon1", src_sz1x1, dst_sz2x2);
        loader.prepareTileset(R.drawable.monsters_dogs, "monsters_dogs", src_sz7x1, defaultTileSize);
	    loader.prepareTileset(R.drawable.monsters_ghost1, "monsters_ghost1", src_sz1x1, defaultTileSize);
        loader.prepareTileset(R.drawable.monsters_insects, "monsters_insects", src_sz6x1, defaultTileSize);
        loader.prepareTileset(R.drawable.monsters_liches, "monsters_liches", new Size(4, 1), defaultTileSize);
	    loader.prepareTileset(R.drawable.monsters_mage2, "monsters_mage2", src_sz1x1, defaultTileSize);
	    loader.prepareTileset(R.drawable.monsters_mage, "monsters_mage", src_sz1x1, defaultTileSize);
        loader.prepareTileset(R.drawable.monsters_man1, "monsters_man1", src_sz1x1, defaultTileSize);
        loader.prepareTileset(R.drawable.monsters_men, "monsters_men", new Size(9, 1), defaultTileSize);
        loader.prepareTileset(R.drawable.monsters_men2, "monsters_men2", new Size(10, 1), defaultTileSize);
	    loader.prepareTileset(R.drawable.monsters_misc, "monsters_misc", new Size(12, 1), defaultTileSize);
    	loader.prepareTileset(R.drawable.monsters_rats, "monsters_rats", new Size(5, 1), defaultTileSize);
        loader.prepareTileset(R.drawable.monsters_rogue1, "monsters_rogue1", src_sz1x1, defaultTileSize);
        loader.prepareTileset(R.drawable.monsters_skeleton1, "monsters_skeleton1", src_sz1x1, defaultTileSize);
        loader.prepareTileset(R.drawable.monsters_skeleton2, "monsters_skeleton2", src_sz1x1, defaultTileSize);
        loader.prepareTileset(R.drawable.monsters_snakes, "monsters_snakes", src_sz6x1, defaultTileSize);
        loader.prepareTileset(R.drawable.monsters_zombie1, "monsters_zombie1", src_sz1x1, defaultTileSize);
        loader.prepareTileset(R.drawable.monsters_rltiles1, "monsters_rltiles1", new Size(20, 8), defaultTileSize);
        loader.prepareTileset(R.drawable.monsters_rltiles2, "monsters_rltiles2", new Size(20, 9), defaultTileSize);
        loader.prepareTileset(R.drawable.monsters_rltiles3, "monsters_rltiles3", new Size(10, 3), defaultTileSize);
        loader.prepareTileset(R.drawable.monsters_karvis2, "monsters_karvis2", new Size(9, 1), defaultTileSize);

        loader.prepareTileset(R.drawable.map_tiles_1_1, "map_tiles_1_1.png", src_mapTileSize, defaultTileSize);
        loader.prepareTileset(R.drawable.map_tiles_1_2, "map_tiles_1_2.png", src_mapTileSize, defaultTileSize);
        loader.prepareTileset(R.drawable.map_tiles_1_3, "map_tiles_1_3.png", src_mapTileSize, defaultTileSize);
        loader.prepareTileset(R.drawable.map_tiles_1_4, "map_tiles_1_4.png", src_mapTileSize, defaultTileSize);
        loader.prepareTileset(R.drawable.map_tiles_1_5, "map_tiles_1_5.png", src_mapTileSize, defaultTileSize);
        loader.prepareTileset(R.drawable.map_tiles_1_6, "map_tiles_1_6.png", src_mapTileSize, defaultTileSize);
        loader.prepareTileset(R.drawable.map_tiles_1_7, "map_tiles_1_7.png", src_mapTileSize, defaultTileSize);
        loader.prepareTileset(R.drawable.map_tiles_1_8, "map_tiles_1_8.png", src_mapTileSize7, defaultTileSize);
        loader.prepareTileset(R.drawable.map_tiles_2_1, "map_tiles_2_1.png", src_mapTileSize, defaultTileSize);
        loader.prepareTileset(R.drawable.map_tiles_2_2, "map_tiles_2_2.png", src_mapTileSize, defaultTileSize);
        loader.prepareTileset(R.drawable.map_tiles_2_3, "map_tiles_2_3.png", src_mapTileSize, defaultTileSize);
        loader.prepareTileset(R.drawable.map_tiles_2_4, "map_tiles_2_4.png", src_mapTileSize, defaultTileSize);
        loader.prepareTileset(R.drawable.map_tiles_2_5, "map_tiles_2_5.png", src_mapTileSize, defaultTileSize);
        loader.prepareTileset(R.drawable.map_tiles_2_6, "map_tiles_2_6.png", src_mapTileSize, defaultTileSize);
        loader.prepareTileset(R.drawable.map_tiles_2_7, "map_tiles_2_7.png", src_mapTileSize, defaultTileSize);
        loader.prepareTileset(R.drawable.map_tiles_2_8, "map_tiles_2_8.png", src_mapTileSize7, defaultTileSize);

        loader.prepareTileset(R.drawable.effect_blood3, "effect_blood3", new Size(8, 2), defaultTileSize);
        loader.prepareTileset(R.drawable.effect_heal2, "effect_heal2", new Size(8, 2), defaultTileSize);
        loader.prepareTileset(R.drawable.effect_poison1, "effect_poison1", new Size(8, 2), defaultTileSize);
	}
}
