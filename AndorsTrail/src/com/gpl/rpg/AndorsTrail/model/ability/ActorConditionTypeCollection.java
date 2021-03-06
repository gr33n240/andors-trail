package com.gpl.rpg.AndorsTrail.model.ability;

import java.util.HashMap;

import com.gpl.rpg.AndorsTrail.AndorsTrailApplication;
import com.gpl.rpg.AndorsTrail.resource.parsers.ActorConditionsTypeParser;
import com.gpl.rpg.AndorsTrail.util.L;

public class ActorConditionTypeCollection {
	private final HashMap<String, ActorConditionType> conditionTypes = new HashMap<String, ActorConditionType>();
	
	public ActorConditionType getActorConditionType(String conditionTypeID) {
		if (AndorsTrailApplication.DEVELOPMENT_VALIDATEDATA) {
			if (!conditionTypes.containsKey(conditionTypeID)) {
				L.log("WARNING: Cannot find ActorConditionType \"" + conditionTypeID + "\".");
			}
		}
		return conditionTypes.get(conditionTypeID);
	}
	
	public void initialize(final ActorConditionsTypeParser parser, String input) {
		parser.parseRows(input, conditionTypes);
	}
}
