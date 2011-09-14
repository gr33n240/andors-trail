package com.gpl.rpg.AndorsTrail.model.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import com.gpl.rpg.AndorsTrail.AndorsTrailApplication;
import com.gpl.rpg.AndorsTrail.conversation.ConversationCollection;
import com.gpl.rpg.AndorsTrail.model.actor.MonsterTypeCollection;
import com.gpl.rpg.AndorsTrail.model.item.DropList.DropItem;
import com.gpl.rpg.AndorsTrail.model.map.MapCollection;
import com.gpl.rpg.AndorsTrail.resource.ResourceFileParser;
import com.gpl.rpg.AndorsTrail.resource.ResourceFileParser.ResourceObjectArrayTokenizer;
import com.gpl.rpg.AndorsTrail.resource.ResourceFileParser.ResourceObjectFieldParser;
import com.gpl.rpg.AndorsTrail.resource.ResourceFileParser.ResourceObjectTokenizer;
import com.gpl.rpg.AndorsTrail.util.L;

public final class DropListCollection {
	public static final String DROPLIST_STARTITEMS = "startitems";
	
	private final HashMap<String, DropList> droplists = new HashMap<String, DropList>();
	
	public DropList getDropList(String droplistID) {
		if (droplistID == null || droplistID.length() <= 0) return null;
		
		if (AndorsTrailApplication.DEVELOPMENT_VALIDATEDATA) {
			if (!droplists.containsKey(droplistID)) {
				L.log("WARNING: Cannot find droplist \"" + droplistID + "\".");
			}
		}
		return droplists.get(droplistID);
	}
	
	private static final ResourceObjectTokenizer droplistResourceTokenizer = new ResourceObjectTokenizer(2);
	private static final ResourceObjectTokenizer droplistItemResourceTokenizer = new ResourceObjectTokenizer(4);
	public void initialize(final ItemTypeCollection itemTypes, String droplistString) {
		droplistResourceTokenizer.tokenizeRows(droplistString, new ResourceObjectFieldParser() {
			@Override
			public void matchedRow(String[] parts) {
				// [id|items[itemID|quantity_Min|quantity_Max|chance|]|];
				
				String droplistID = parts[0];
				
				final ArrayList<DropItem> items = new ArrayList<DropItem>();
				ResourceObjectArrayTokenizer.tokenize(parts[1], droplistItemResourceTokenizer, new ResourceObjectFieldParser() {
					@Override
					public void matchedRow(String[] parts) {
						items.add(new DropItem(
								itemTypes.getItemType(parts[0]) 						// Itemtype
								, ResourceFileParser.parseChance(parts[3]) 				// Chance
								, ResourceFileParser.parseQuantity(parts[1], parts[2]) 	// Quantity
							));
					}
				});
				
				DropItem[] items_ = items.toArray(new DropItem[items.size()]);
				final DropList droplist = new DropList(items_);
				if (AndorsTrailApplication.DEVELOPMENT_VALIDATEDATA) {
	    			if (droplistID.trim().length() <= 0) {
	    				L.log("WARNING: Droplist with empty id.");
	    			} else {
    					if (droplists.containsKey(droplistID)) {
    						L.log("OPTIMIZE: Droplist " + droplistID + " is duplicated.");
    					}
	    			}
	    			if (items.size() <= 0) {
	    				L.log("OPTIMIZE: Droplist \"" + droplistID + "\" has no dropped items.");
	    			}
	    		}
				droplists.put(droplistID, droplist);
			}
		});
	}
	
	// Selftest method. Not part of the game logic.
	public boolean verifyExistsDroplist(String itemTypeID) {
		if (AndorsTrailApplication.DEVELOPMENT_VALIDATEDATA) {
			for (DropList d : droplists.values()) {
				if (d.contains(itemTypeID)) return true;
			}
		}
		return false;
	}

	// Selftest method. Not part of the game logic.
	public void verifyData(MonsterTypeCollection monsterTypes, ConversationCollection conversations, MapCollection maps) {
		if (AndorsTrailApplication.DEVELOPMENT_VALIDATEDATA) {
			HashSet<DropList> usedDroplists = new HashSet<DropList>();
			monsterTypes.DEBUG_getUsedDroplists(usedDroplists);
			conversations.DEBUG_getUsedDroplists(usedDroplists, this);
			maps.DEBUG_getUsedDroplists(usedDroplists);
			usedDroplists.add(getDropList(DropListCollection.DROPLIST_STARTITEMS));
			
			for (Entry<String, DropList> e : droplists.entrySet()) {
				if (!usedDroplists.contains(e.getValue())) {
					L.log("OPTIMIZE: Droplist " + e.getKey() + " is not used by any monster or conversation phrase.");
				}
			}
		}	
	}
}
