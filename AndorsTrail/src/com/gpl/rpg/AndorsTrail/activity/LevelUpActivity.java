package com.gpl.rpg.AndorsTrail.activity;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.gpl.rpg.AndorsTrail.AndorsTrailApplication;
import com.gpl.rpg.AndorsTrail.R;
import com.gpl.rpg.AndorsTrail.context.WorldContext;
import com.gpl.rpg.AndorsTrail.controller.ActorStatsController;
import com.gpl.rpg.AndorsTrail.controller.Constants;
import com.gpl.rpg.AndorsTrail.model.ability.SkillCollection;
import com.gpl.rpg.AndorsTrail.model.actor.Player;

public final class LevelUpActivity extends Activity {
	private WorldContext world;
	private Player player;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndorsTrailApplication app = AndorsTrailApplication.getApplicationFromActivity(this);
        this.world = app.world;
        this.player = world.model.player;
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.levelup);
    	final Resources res = getResources();
    	
        ImageView img = (ImageView) findViewById(R.id.levelup_image);
        world.tileManager.setImageViewTile(img, player);
        
        TextView tv = (TextView) findViewById(R.id.levelup_description);
        tv.setText(res.getString(R.string.levelup_description, player.level+1));

        Button b;
        
        b = (Button) findViewById(R.id.levelup_add_health);
        b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				levelup(SELECT_HEALTH);
			}
		});
        b.setText(getString(R.string.levelup_add_health, Constants.LEVELUP_EFFECT_HEALTH));
        
        b = (Button) findViewById(R.id.levelup_add_attackchance);
        b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				levelup(SELECT_ATK_CH);
			}
		});
        b.setText(getString(R.string.levelup_add_attackchance, Constants.LEVELUP_EFFECT_ATK_CH));
        
        b = (Button) findViewById(R.id.levelup_add_attackdamage);
        b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				levelup(SELECT_ATK_DMG);
			}
		});
        b.setText(getString(R.string.levelup_add_attackdamage, Constants.LEVELUP_EFFECT_ATK_DMG));
        
        b = (Button) findViewById(R.id.levelup_add_blockchance);
        b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				levelup(SELECT_DEF_CH);
			}
		});
        b.setText(getString(R.string.levelup_add_blockchance, Constants.LEVELUP_EFFECT_DEF_CH));
        
        View v = findViewById(R.id.levelup_adds_new_skillpoint);
        if (player.nextLevelAddsNewSkillpoint()) {
        	v.setVisibility(View.VISIBLE);
        } else {
        	v.setVisibility(View.GONE);
        }
    }

    private static final int SELECT_HEALTH = 0;
    private static final int SELECT_ATK_CH = 1;
    private static final int SELECT_ATK_DMG = 2;
    private static final int SELECT_DEF_CH = 3;
    
    public void levelup(int selectionID) {
    	if (LevelUpActivity.this.isFinishing()) return;
    	
    	addLevelupEffect(player, selectionID);
    	LevelUpActivity.this.finish();
    }
    
    public static void addLevelupEffect(Player player, int selectionID) {
    	int hpIncrease = 0;
    	switch (selectionID) {
    	case SELECT_HEALTH:
    		hpIncrease = Constants.LEVELUP_EFFECT_HEALTH;
    		break;
    	case SELECT_ATK_CH:
    		player.actorTraits.baseCombatTraits.attackChance += Constants.LEVELUP_EFFECT_ATK_CH;
    		break;
    	case SELECT_ATK_DMG:
    		player.actorTraits.baseCombatTraits.damagePotential.max += Constants.LEVELUP_EFFECT_ATK_DMG;
    		player.actorTraits.baseCombatTraits.damagePotential.current += Constants.LEVELUP_EFFECT_ATK_DMG;
    		break;
    	case SELECT_DEF_CH:
    		player.actorTraits.baseCombatTraits.blockChance += Constants.LEVELUP_EFFECT_DEF_CH;
    		break;
    	}
    	if (player.nextLevelAddsNewSkillpoint()) {
    		player.availableSkillIncreases++;
    	}
    	player.level++;
    	
    	hpIncrease += player.getSkillLevel(SkillCollection.SKILL_FORTITUDE) * SkillCollection.PER_SKILLPOINT_INCREASE_FORTITUDE_HEALTH;
		player.health.max += hpIncrease;
		player.actorTraits.maxHP += hpIncrease;
		player.health.current += hpIncrease;
    	
    	player.recalculateLevelExperience();
    	ActorStatsController.recalculatePlayerCombatTraits(player);
    }
}
