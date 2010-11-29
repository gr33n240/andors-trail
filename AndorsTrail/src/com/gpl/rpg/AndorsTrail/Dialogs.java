package com.gpl.rpg.AndorsTrail;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.gpl.rpg.AndorsTrail.R;
import com.gpl.rpg.AndorsTrail.activity.ConversationActivity;
import com.gpl.rpg.AndorsTrail.activity.MainActivity;
import com.gpl.rpg.AndorsTrail.activity.HeroinfoActivity;
import com.gpl.rpg.AndorsTrail.activity.ItemInfoActivity;
import com.gpl.rpg.AndorsTrail.activity.LevelUpActivity;
import com.gpl.rpg.AndorsTrail.activity.MonsterEncounterActivity;
import com.gpl.rpg.AndorsTrail.activity.MonsterInfoActivity;
import com.gpl.rpg.AndorsTrail.activity.Preferences;
import com.gpl.rpg.AndorsTrail.context.ViewContext;
import com.gpl.rpg.AndorsTrail.controller.Controller;
import com.gpl.rpg.AndorsTrail.controller.ItemController;
import com.gpl.rpg.AndorsTrail.model.InterfaceData;
import com.gpl.rpg.AndorsTrail.model.actor.Monster;
import com.gpl.rpg.AndorsTrail.model.item.ItemType;
import com.gpl.rpg.AndorsTrail.model.item.Loot;
import com.gpl.rpg.AndorsTrail.resource.TileStore;
import com.gpl.rpg.AndorsTrail.view.ItemContainerAdapter;

public final class Dialogs {
	
	private static void showDialogAndPause(Dialog d, final ViewContext context) { 
		showDialogAndPause(d, context, new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface arg0) {
				context.controller.resume();
			}
		});
	}
	private static void showDialogAndPause(Dialog d, ViewContext context, OnDismissListener onDismiss) {
		context.controller.pause();
    	d.setOnDismissListener(onDismiss);
    	//setBlurrywindow(d);
		d.show();
	}

	/*
	private static void setBlurrywindow(Dialog d) {
		d.getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
	}
	*/
	
	public static void showMapSign(final Context androidContext, final ViewContext context, String title, String text) {
		Dialog d = new AlertDialog.Builder(androidContext)
        .setTitle(title)
        .setMessage(text)
        .setIcon(new BitmapDrawable(context.tileStore.bitmaps[TileStore.iconID_mapsign]))
        .setNeutralButton(android.R.string.ok, null)
        .create();
		showDialogAndPause(d, context);
	}

	/*
	public static void showConfirmExit(final Activity activity, final ViewContext context) {
    	Dialog d = new AlertDialog.Builder(activity)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(R.string.dialog_confirmexit_title)
        .setMessage(R.string.dialog_confirmexit_message)
        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	activity.finish();    
            }
        })
        .setNegativeButton(android.R.string.no, null)
        .create();

    	showDialogAndPause(d, context);
	}
	*/

	public static void showPaused(final Context androidContext, final ViewContext context) {
		Dialog d = new AlertDialog.Builder(androidContext)
        .setIcon(android.R.drawable.ic_dialog_info)
        .setTitle(R.string.dialog_paused_title)
        .setMessage(R.string.dialog_paused_message)
        .setNeutralButton(R.string.dialog_paused_resume, null)
        .create();

		showDialogAndPause(d, context);
	}
	
	public static void showConversation(final MainActivity currentActivity, final ViewContext context, final String phraseID, final Monster npc) {
		context.controller.pause();
		Intent intent = new Intent(currentActivity, ConversationActivity.class);
		Uri.Builder b = Uri.parse("content://com.gpl.rpg.AndorsTrail/conversation/" + phraseID).buildUpon();
		b.appendQueryParameter("monsterTypeID", Integer.toString(npc.monsterType.id));
		intent.setData(b.build());
		currentActivity.startActivityForResult(intent, MainActivity.INTENTREQUEST_CONVERSATION);
	}
	
	public static void showMonsterEncounter(final MainActivity currentActivity, final ViewContext context, final Monster m) {
		context.controller.pause();
		Intent intent = new Intent(currentActivity, MonsterEncounterActivity.class);
		intent.setData(Uri.parse("content://com.gpl.rpg.AndorsTrail/monsterencounter/" + m.monsterType.id));
		currentActivity.startActivityForResult(intent, MainActivity.INTENTREQUEST_MONSTERENCOUNTER);
	}

	public static void showMonsterInfo(final Activity currentActivity, int monsterTypeID) {
		Intent intent = new Intent(currentActivity, MonsterInfoActivity.class);
		intent.setData(Uri.parse("content://com.gpl.rpg.AndorsTrail/monsterinfo/" + monsterTypeID));
		currentActivity.startActivity(intent);
	}
	
	public static void showMonsterLoot(final Context androidContext, final ViewContext context, final Loot loot) {
		showLoot(androidContext, context, loot, R.string.dialog_monsterloot_title, R.string.dialog_monsterloot_message);
	}

	public static void showGroundLoot(final Context androidContext, final ViewContext context, final Loot loot) {
		showLoot(androidContext, context, loot, R.string.dialog_groundloot_title, R.string.dialog_groundloot_message);
	}
	
	private static void showLoot(final Context androidContext, final ViewContext context, final Loot loot, final int title, final int message) {
		if (ItemController.removeEmptyLoot(context, loot)) return;

		String msg = androidContext.getString(message);
		if (loot.exp > 0) {
			msg += androidContext.getString(R.string.dialog_monsterloot_gainedexp, loot.exp);
		}
		if (loot.gold > 0) {
			msg += androidContext.getString(R.string.dialog_loot_foundgold, loot.gold);
		}
		
		if (context.model.uiSelections.displayLoot != InterfaceData.DISPLAYLOOT_DIALOG) {
			if (context.model.uiSelections.displayLoot == InterfaceData.DISPLAYLOOT_TOAST) {
				if (!loot.items.items.isEmpty()) {
					msg += androidContext.getString(R.string.dialog_loot_pickedupitems, loot.items.items.size());
				}
				Toast.makeText(androidContext, msg, Toast.LENGTH_LONG).show();
			}
			ItemController.pickupAll(loot, context.model);
        	ItemController.removeEmptyLoot(context, loot);
			context.controller.resume();
			return;
		}

		final ListView itemList = new ListView(androidContext);
		itemList.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		itemList.setPadding(20, 0, 20, 20);
		itemList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final int itemTypeID = (int) id;
				loot.items.removeItem(itemTypeID, 1);
				ItemType type = context.itemTypes.getItemType(itemTypeID);
				context.model.player.inventory.addItem(type);
				((ItemContainerAdapter) itemList.getAdapter()).notifyDataSetChanged();
			}
		});
		itemList.setAdapter(new ItemContainerAdapter(androidContext, context.tileStore, loot.items));
		
		AlertDialog.Builder db = new AlertDialog.Builder(androidContext)
        .setTitle(title)
        .setMessage(msg)
        .setIcon(new BitmapDrawable(context.tileStore.bitmaps[TileStore.iconID_groundbag]))
        .setNegativeButton(R.string.dialog_close, null)
        .setView(itemList);
		
		if (!loot.items.isEmpty()) {
			db.setPositiveButton(R.string.dialog_loot_pickall, new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	            	ItemController.pickupAll(loot, context.model);
	            }
	        });
		}
		
		final Dialog d = db.create();
		
		showDialogAndPause(d, context, new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface arg0) {
				ItemController.removeEmptyLoot(context, loot);
				context.controller.resume();
			}
		});
	}

	public static void showItemInfo(final Activity currentActivity, int itemTypeID, int actionType, String buttonText, boolean buttonEnabled, int inventorySlot) {
		Intent intent = new Intent(currentActivity, ItemInfoActivity.class);
		intent.putExtra("buttonText", buttonText);
		intent.putExtra("buttonEnabled", buttonEnabled);
		intent.putExtra("itemTypeID", itemTypeID);
		intent.putExtra("actionType", actionType);
		intent.putExtra("inventorySlot", inventorySlot);
		intent.setData(Uri.parse("content://com.gpl.rpg.AndorsTrail/iteminfo/" + itemTypeID));
		currentActivity.startActivityForResult(intent, MainActivity.INTENTREQUEST_ITEMINFO);
	}
	public static void showLevelUp(final HeroinfoActivity currentActivity) {
		Intent intent = new Intent(currentActivity, LevelUpActivity.class);
		intent.setData(Uri.parse("content://com.gpl.rpg.AndorsTrail/levelup"));
		currentActivity.startActivityForResult(intent, MainActivity.INTENTREQUEST_LEVELUP);
	}

	public static void showRest(final Activity currentActivity, final ViewContext viewContext) {
		if (!viewContext.model.uiSelections.confirmRest) {
			Controller.ui_playerRested(currentActivity, viewContext);
			return;
		}
		Dialog d = new AlertDialog.Builder(currentActivity)
        .setTitle(R.string.dialog_rest_title)
        .setMessage(R.string.dialog_rest_confirm_message)
        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
        		Controller.ui_playerRested(currentActivity, viewContext);
            }
        })
        .setNegativeButton(android.R.string.no, null)
        .create();

    	showDialogAndPause(d, viewContext);
	}
	public static void showRested(final Activity currentActivity, final ViewContext viewContext) {
		Dialog d = new AlertDialog.Builder(currentActivity)
        .setTitle(R.string.dialog_rest_title)
        .setMessage(R.string.dialog_rest_message)
        .setNeutralButton(android.R.string.ok, null)
        .create();

    	showDialogAndPause(d, viewContext);
	}

	public static void showNewVersion(final Activity currentActivity) {
		new AlertDialog.Builder(currentActivity)
        .setTitle(R.string.dialog_newversion_title)
        .setMessage(R.string.dialog_newversion_message)
        .setNeutralButton(android.R.string.ok, null)
        .show();
	}
	
	public static void showPreferences(final Activity currentActivity) {
		Intent intent = new Intent(currentActivity, Preferences.class);
		currentActivity.startActivityForResult(intent, MainActivity.INTENTREQUEST_PREFERENCES);
	}
}
