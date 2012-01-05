package com.gpl.rpg.AndorsTrail.conversation;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import com.gpl.rpg.AndorsTrail.AndorsTrailApplication;
import com.gpl.rpg.AndorsTrail.conversation.Phrase.Reply;
import com.gpl.rpg.AndorsTrail.conversation.Phrase.Reward;
import com.gpl.rpg.AndorsTrail.model.actor.MonsterTypeCollection;
import com.gpl.rpg.AndorsTrail.model.item.DropList;
import com.gpl.rpg.AndorsTrail.model.item.DropListCollection;
import com.gpl.rpg.AndorsTrail.model.item.ItemType;
import com.gpl.rpg.AndorsTrail.model.item.ItemTypeCollection;
import com.gpl.rpg.AndorsTrail.model.map.MapCollection;
import com.gpl.rpg.AndorsTrail.model.quest.QuestCollection;
import com.gpl.rpg.AndorsTrail.model.quest.QuestProgress;
import com.gpl.rpg.AndorsTrail.resource.parsers.ConversationListParser;
import com.gpl.rpg.AndorsTrail.util.L;

public final class ConversationCollection {
	public static final String PHRASE_CLOSE = "X";
	public static final String PHRASE_SHOP = "S";
	public static final String PHRASE_ATTACK = "F";
	public static final String PHRASE_REMOVE = "R";
	public static final String REPLY_NEXT = "N";
	
	private final HashMap<String, Phrase> phrases = new HashMap<String, Phrase>();
	
	public boolean isValidPhraseID(String id) {
		if (id.equals(PHRASE_CLOSE)) return true;
		else if (id.equals(PHRASE_SHOP)) return true;
		else if (id.equals(PHRASE_ATTACK)) return true;
		else if (id.equals(PHRASE_REMOVE)) return true;
		else if (phrases.containsKey(id)) return true;
		else return false;
	}
	
	public Phrase getPhrase(String id) {
		if (AndorsTrailApplication.DEVELOPMENT_VALIDATEDATA) {
			if (!phrases.containsKey(id)) {
				L.log("WARNING: Cannot find requested conversation phrase id \"" + id + "\".");
				return null;
			}
		}
		return phrases.get(id);
	}
	
	public Collection<String> initialize(ConversationListParser parser, String input) {
		return parser.parseRows(input, phrases);
	}
	
	// Selftest method. Not part of the game logic.
	public void verifyData() {
		if (AndorsTrailApplication.DEVELOPMENT_VALIDATEDATA) {
			for (Entry<String, Phrase> e : phrases.entrySet()) {
				final String phraseID = e.getKey();
				for (Reply r : e.getValue().replies) {
					if (!isValidPhraseID(r.nextPhrase)) {
						L.log("WARNING: Phrase \"" + phraseID + "\" has reply to non-existing phrase \"" + r.nextPhrase + "\".");
					} else if (r.nextPhrase == null || r.nextPhrase.length() <= 0) {
						L.log("WARNING: Phrase \"" + phraseID + "\" has a reply that has no nextPhrase.");
					} else if (r.nextPhrase.equals(e.getKey())) {
						L.log("WARNING: Phrase \"" + phraseID + "\" has a reply that points to itself.");
					}
				}
				
				boolean hasNextReply = false;
    			boolean hasOtherReply = false;
    			for (Reply r : e.getValue().replies) {
    				if (r.text.equalsIgnoreCase(REPLY_NEXT)) hasNextReply = true;
    				else hasOtherReply = true;
    			}
    			if (hasNextReply && hasOtherReply) {
    				L.log("WARNING: Phrase \"" + phraseID + "\" has both a \"" + REPLY_NEXT + "\" reply and some other reply.");
    			}
    		}
		}
	}
	
	// Selftest method. Not part of the game logic.
	public void verifyData(MapCollection maps) {
    	if (AndorsTrailApplication.DEVELOPMENT_VALIDATEDATA) {
    		HashSet<String> requiredQuestStages = new HashSet<String>();
    		HashSet<String> suppliedQuestStages = new HashSet<String>();
    		this.DEBUG_getSuppliedQuestStages(suppliedQuestStages);
    		maps.DEBUG_getRequiredQuestStages(requiredQuestStages);
    		this.DEBUG_getRequiredQuestStages(requiredQuestStages);
    		
			for (String s : requiredQuestStages) {
				if (!suppliedQuestStages.contains(s)) {
					L.log("WARNING: Queststage \"" + s + "\" is required but never supplied by any phrases.");
				}
			}
		}
    }
	
	// Selftest method. Not part of the game logic.
	public void verifyData(DropListCollection droplists) {
    	if (AndorsTrailApplication.DEVELOPMENT_VALIDATEDATA) {
    		for (Entry<String, Phrase> e : phrases.entrySet()) {
				for (Reply r : e.getValue().replies) {
					if (r.requiresItem()) {
						if (!droplists.verifyExistsDroplistForItem(r.requiresItemTypeID)) {
							L.log("WARNING: Phrase \"" + e.getKey() + "\" has reply that requires \"" + r.requiresItemTypeID + "\", which is not dropped by any droplist.");
						}
					}
				}
    		}
    	}
    }
	
	// Selftest method. Not part of the game logic.
	public void verifyData(MonsterTypeCollection monsterTypes, MapCollection maps) {
    	if (AndorsTrailApplication.DEVELOPMENT_VALIDATEDATA) {
    		HashSet<String> requiredPhrases = monsterTypes.DEBUG_getRequiredPhrases();
    		maps.DEBUG_getUsedPhrases(requiredPhrases);
    		for (Entry<String, Phrase> e : phrases.entrySet()) {
				for (Reply r : e.getValue().replies) {
					requiredPhrases.add(r.nextPhrase);
				}
    		}
    		requiredPhrases.remove(PHRASE_ATTACK);
    		requiredPhrases.remove(PHRASE_CLOSE);
    		requiredPhrases.remove(PHRASE_SHOP);
    		requiredPhrases.remove(PHRASE_REMOVE);
    		
    		// Verify that all supplied phrases are required.
    		for (Entry<String, Phrase> e : phrases.entrySet()) {
    			if (!requiredPhrases.contains(e.getKey())) {
    				L.log("OPTIMIZE: Phrase \"" + e.getKey() + "\" cannot be reached by any monster or other phrase reply.");
    			}
    		}
    	}
    }
	
	// Selftest method. Not part of the game logic.
	public void verifyData(QuestCollection quests) {
    	if (AndorsTrailApplication.DEVELOPMENT_VALIDATEDATA) {
    		for (Phrase p : phrases.values()) {
    			if (p.rewards == null) continue;
				for (Reward r : p.rewards) {
					if (r.rewardType != Reward.REWARD_TYPE_QUEST_PROGRESS) continue;
					quests.getQuestLogEntry(new QuestProgress(r.rewardID, r.value)); // Will warn inside if invalid.
				}
    		}
    	}
    }
	
	// Selftest method. Not part of the game logic.
	public void verifyData(ItemTypeCollection itemTypes) {
		if (AndorsTrailApplication.DEVELOPMENT_VALIDATEDATA) {
			for (Entry<String, Phrase> e : phrases.entrySet()) {
				for (Reply r : e.getValue().replies) {
					if (!r.requiresItem()) continue;
					ItemType itemType = itemTypes.getItemType(r.requiresItemTypeID);
					
					if (r.itemRequirementType == Reply.ITEM_REQUIREMENT_TYPE_WEAR_KEEP) {
						if (!itemType.isEquippable()) L.log("WARNING: Phrase \"" + e.getKey() + "\" has a reply that requires a worn \"" + itemType + "\", but the item is not wearable.");
					}
					
					if (!itemType.isQuestItem()) continue;
					
					Phrase nextPhrase = getPhrase(r.nextPhrase);
					if (!hasQuestProgressReward(nextPhrase)) {
						L.log("WARNING: Phrase \"" + e.getKey() + "\" has a reply that requires a questitem, but the next phrase does not add quest progress.");
					}
				}
    		}
		}	
    }

	private boolean hasQuestProgressReward(Phrase nextPhrase) {
		if (nextPhrase.rewards == null) return false;
		for (Reward r : nextPhrase.rewards) {
			if (r.rewardType == Reward.REWARD_TYPE_QUEST_PROGRESS) return true;
		}
		return false;
	}

	// Selftest method. Not part of the game logic.
	public void DEBUG_getSuppliedQuestStages(HashSet<String> suppliedStages) {
		if (AndorsTrailApplication.DEVELOPMENT_VALIDATEDATA) {
			for (Phrase p : phrases.values()) {
				if (p.rewards == null) continue;
				for (Reward r : p.rewards) {
					if (r.rewardType != Reward.REWARD_TYPE_QUEST_PROGRESS) continue;
					QuestProgress progressQuest = new QuestProgress(r.rewardID, r.value);
					suppliedStages.add(progressQuest.toString());
				}
			}
		}
	}
	
	// Selftest method. Not part of the game logic.
	public void DEBUG_getRequiredQuestStages(HashSet<String> requiredStages) {
		if (AndorsTrailApplication.DEVELOPMENT_VALIDATEDATA) {
			for (Phrase p : phrases.values()) {
				for (Reply r : p.replies) {
					if (r.requiresProgress != null) {
						requiredStages.add(r.requiresProgress.toString());
					}
				}
			}
		}
	}

	// Selftest method. Not part of the game logic.
	public boolean DEBUG_leadsToTradeReply(String phraseID) {
		if (AndorsTrailApplication.DEVELOPMENT_VALIDATEDATA) {
			HashSet<String> visited = new HashSet<String>();
			return DEBUG_leadsToTradeReply(phraseID, visited);
		} else {
			return false;
		}
	}
	private boolean DEBUG_leadsToTradeReply(String phraseID, HashSet<String> visited) {
		if (AndorsTrailApplication.DEVELOPMENT_VALIDATEDATA) {
			if (phraseID.equals(PHRASE_SHOP)) return true;
			if (phraseID.equals(PHRASE_ATTACK)) return false;
			if (phraseID.equals(PHRASE_CLOSE)) return false;
			if (phraseID.equals(PHRASE_REMOVE)) return false;
			if (visited.contains(phraseID)) return false;
			visited.add(phraseID);
			
			Phrase p = getPhrase(phraseID);
			if (p == null) return false;
			for (Reply r : p.replies) {
				if (DEBUG_leadsToTradeReply(r.nextPhrase, visited)) return true;
			}
		}
		return false;
	}

	public void DEBUG_getUsedDroplists(HashSet<DropList> usedDropLists, final DropListCollection dropListCollection) {
		if (AndorsTrailApplication.DEVELOPMENT_VALIDATEDATA) {
			for (Phrase p : phrases.values()) {
				if (p.rewards == null) continue;
				for (Reward r : p.rewards) {
					if (r.rewardType != Reward.REWARD_TYPE_DROPLIST) continue;
					
					DropList d = dropListCollection.getDropList(r.rewardID);
					if (d != null) usedDropLists.add(d);
				}
			}
		}
	}
}
