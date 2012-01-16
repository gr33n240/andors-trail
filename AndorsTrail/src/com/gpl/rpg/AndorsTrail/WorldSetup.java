package com.gpl.rpg.AndorsTrail;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;

import com.gpl.rpg.AndorsTrail.context.WorldContext;
import com.gpl.rpg.AndorsTrail.controller.Controller;
import com.gpl.rpg.AndorsTrail.controller.MovementController;
import com.gpl.rpg.AndorsTrail.model.ModelContainer;
import com.gpl.rpg.AndorsTrail.model.job.Job;
import com.gpl.rpg.AndorsTrail.resource.ResourceLoader;

public final class WorldSetup {

    private final WorldContext world;
    private final WeakReference<Context> androidContext;
    private boolean isResourcesInitialized = false;
    private boolean isInitializingResources = false;
    private WeakReference<OnSceneLoadedListener> listener;

    public boolean createNewCharacter = false;
    public int loadFromSlot = Savegames.SLOT_QUICKSAVE;
    public boolean isSceneReady = false;
    public String newHeroName;
    public Job newHeroJob;
    private int loadResult;

    public WorldSetup(WorldContext world, Context androidContext) {
        this.world = world;
        this.androidContext = new WeakReference<Context>(androidContext);
    }

    public void startResourceLoader(final Resources r, final AndorsTrailPreferences preferences) {
        if (isResourcesInitialized) return;

        synchronized (this) {
            if (isInitializingResources) return;
            isInitializingResources = true;
        }

        (new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... arg0) {
                ResourceLoader.loadResources(world, r);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                synchronized (WorldSetup.this) {
                    isResourcesInitialized = true;
                    isInitializingResources = false;
                    if (listener == null) return; // sceneloader will be fired by next caller.
                }
                startSceneLoader();
            }
        }).execute();
    }

    public void startCharacterSetup(final OnSceneLoadedListener listener) {
        synchronized (WorldSetup.this) {
            this.listener = new WeakReference<OnSceneLoadedListener>(listener);
            if (!isResourcesInitialized) return; // sceneloader will be fired by the resourceloader.
        }
        startSceneLoader();
    }

    private void startSceneLoader() {
        isSceneReady = false;
        (new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... arg0) {
                if (world.model != null) world.reset();
                if (createNewCharacter) {
                    createNewWorld();
                    loadResult = Savegames.LOAD_RESULT_SUCCESS;
                } else {
                    loadResult = continueWorld();
                }
                createNewCharacter = false;
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                isSceneReady = true;
                OnSceneLoadedListener o;
                synchronized (WorldSetup.this) {
                    if (listener == null) return;
                    o = listener.get();
                    listener = null;
                }
                if (o == null) return;
                if (loadResult == Savegames.LOAD_RESULT_SUCCESS) {
                    o.onSceneLoaded();
                } else {
                    o.onSceneLoadFailed(loadResult);
                }
            }
        }).execute();
    }

    private int continueWorld() {
        Context ctx = androidContext.get();
        int result = Savegames.loadWorld(world, ctx, loadFromSlot);
        if (result == Savegames.LOAD_RESULT_SUCCESS) {
            MovementController.cacheCurrentMapData(ctx.getResources(), world, world.model.currentMap);
        }
        return result;
    }

    private void createNewWorld() {
        Context ctx = androidContext.get();
        world.model = new ModelContainer(this);
        world.model.player.initializeNewPlayer(world.itemTypes, world.dropLists, newHeroName, newHeroJob);
        Controller.playerRested(world, null);
        MovementController.respawnPlayer(ctx.getResources(), world);
    }


    public interface OnSceneLoadedListener {
        void onSceneLoaded();
        void onSceneLoadFailed(int loadResult);
    }

}
