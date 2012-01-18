package com.gpl.rpg.AndorsTrail.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.gpl.rpg.AndorsTrail.AndorsTrailApplication;
import com.gpl.rpg.AndorsTrail.AndorsTrailPreferences;
import com.gpl.rpg.AndorsTrail.Dialogs;
import com.gpl.rpg.AndorsTrail.R;
import com.gpl.rpg.AndorsTrail.Savegames;
import com.gpl.rpg.AndorsTrail.WorldSetup;
import com.gpl.rpg.AndorsTrail.Savegames.FileHeader;
import com.gpl.rpg.AndorsTrail.controller.Constants;
import com.gpl.rpg.AndorsTrail.model.actor.Actor;
import com.gpl.rpg.AndorsTrail.model.job.Job;

public final class StartScreenActivity extends Activity {
    private static final String TAG = "StartScreenActivity";

    public static final int INTENTREQUEST_LOADGAME = 9;

    private boolean hasExistingGame = false;

    private Job job;
    private String gender;

    private Button startscreen_continue;
    private Button startscreen_newgame;
    private Button startscreen_load;

    private ScrollView startscreen_new_character;

    private TextView startscreen_currenthero;
    private EditText startscreen_enterheroname;

    private RadioButton startscreen_gender_male;
    private RadioButton startscreen_gender_female;

    private RadioGroup startscreen_selectjob;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndorsTrailApplication app = AndorsTrailApplication.getApplicationFromActivity(this);
        AndorsTrailPreferences.read(this, app.preferences);
        AndorsTrailApplication.setWindowParameters(this, app.preferences);

        setContentView(R.layout.startscreen);

        TextView tv = (TextView) findViewById(R.id.startscreen_version);
        tv.setText("v" + AndorsTrailApplication.CURRENT_VERSION_DISPLAY);

        startscreen_currenthero = (TextView) findViewById(R.id.startscreen_currenthero);
        startscreen_enterheroname = (EditText) findViewById(R.id.startscreen_enterheroname);
        //startscreen_enterheroname.setImeOptions(EditorInfo.IME_ACTION_DONE);

        startscreen_new_character = (ScrollView) findViewById(R.id.startscreen_new_character);

        // Setup the gender select buttons.
        startscreen_gender_male = (RadioButton) findViewById(R.id.startscreen_gender_male);
        startscreen_gender_female = (RadioButton) findViewById(R.id.startscreen_gender_female);

        startscreen_gender_male.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View view) {
                StartScreenActivity.this.gender = Actor.GENDER_MALE;
            }
        });
        startscreen_gender_female.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View view) {
                StartScreenActivity.this.gender = Actor.GENDER_FEMALE;
            }
        });

        // Build the Job select radio buttons.
        startscreen_selectjob = (RadioGroup) findViewById(R.id.startscreen_selectjob);
        for (int i = 0; i < Job.JOBS.length; i++) {
            final Job job = Job.JOBS[i];
            final RadioButton jobButton = new RadioButton(StartScreenActivity.this);

            jobButton.setId(job.jobId);
            jobButton.setText(job.name);
            jobButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(final View view) {
                    StartScreenActivity.this.job = job;
                }
            });

            startscreen_selectjob.addView(jobButton);
        }

        startscreen_continue = (Button) findViewById(R.id.startscreen_continue);
        startscreen_continue.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                continueGame(false, Savegames.SLOT_QUICKSAVE, null);
            }
        });

        startscreen_newgame = (Button) findViewById(R.id.startscreen_newgame);
        startscreen_newgame.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (hasExistingGame) {
                    comfirmNewGame();
                } else {
                    createNewGame();
                }
            }
        });

        final Button startscreen_about = (Button) findViewById(R.id.startscreen_about);
        startscreen_about.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                startActivity(new Intent(StartScreenActivity.this, AboutActivity.class));
            }
        });

        final Button startscreen_quit = (Button) findViewById(R.id.startscreen_quit);
        startscreen_quit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                //comfirmQuit();
                StartScreenActivity.this.finish();
            }
        });

        startscreen_load = (Button) findViewById(R.id.startscreen_load);
        startscreen_load.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                Dialogs.showLoad(StartScreenActivity.this);
            }
        });

        TextView development_version = (TextView) findViewById(R.id.startscreen_dev_version);
        if (AndorsTrailApplication.DEVELOPMENT_DEBUGRESOURCES) {
            development_version.setVisibility(View.VISIBLE);
        }

        final Resources res = getResources();
        app.world.tileManager.setDensity(res);
        app.world.tileManager.updatePreferences(app.preferences);
        app.setup.startResourceLoader(res, app.preferences);

        if (AndorsTrailApplication.DEVELOPMENT_FORCE_STARTNEWGAME) {
            if (AndorsTrailApplication.DEVELOPMENT_DEBUGRESOURCES) {
                continueGame(true, 0, "Debug player");
            } else {
                continueGame(true, 0, "Player");
            }
        } else if (AndorsTrailApplication.DEVELOPMENT_FORCE_CONTINUEGAME) {
            continueGame(false, Savegames.SLOT_QUICKSAVE, null);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        String playerName = null;
        String displayInfo = null;

        FileHeader header = Savegames.quickload(this, Savegames.SLOT_QUICKSAVE);
        if (header != null && header.playerName != null) {
            playerName = header.playerName;
            displayInfo = header.displayInfo;
        } else {
            // Before fileversion 14 (v0.6.7), quicksave was stored in Shared preferences
            SharedPreferences p = getSharedPreferences("quicksave", MODE_PRIVATE);
            playerName = p.getString("playername", null);
            if (playerName != null) {
                displayInfo = "level " + p.getInt("level", -1);
            }
        }
        hasExistingGame = (playerName != null);
        setButtonState(playerName, displayInfo);

        if (isNewVersion()) {
            Dialogs.showNewVersion(this);
        }

        boolean hasSavegames = !Savegames.getUsedSavegameSlots(this).isEmpty();
        startscreen_load.setEnabled(hasSavegames);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case INTENTREQUEST_LOADGAME:
                if (resultCode != Activity.RESULT_OK) break;
                final int slot = data.getIntExtra("slot", 1);
                continueGame(false, slot, null);
                break;
        }
    }

    private boolean isNewVersion() {
        final String v = "lastversion";
        SharedPreferences s = getSharedPreferences(Constants.PREFERENCE_MODEL_LASTRUNVERSION, MODE_PRIVATE);
        int lastversion = s.getInt(v, 0);
        if (lastversion >= AndorsTrailApplication.CURRENT_VERSION) return false;
        Editor e = s.edit();
        e.putInt(v, AndorsTrailApplication.CURRENT_VERSION);
        e.commit();
        return true;
    }


    private void setButtonState(final String playerName, final String displayInfo) {
        startscreen_continue.setEnabled(hasExistingGame);
        startscreen_newgame.setEnabled(true);
        if (hasExistingGame) {
            startscreen_currenthero.setText(playerName  + ", " + displayInfo);
            startscreen_enterheroname.setText(playerName);
            startscreen_enterheroname.setVisibility(View.GONE);
            startscreen_new_character.setVisibility(View.GONE);
        } else {
            startscreen_currenthero.setText(R.string.startscreen_enterheroname);
            startscreen_enterheroname.setVisibility(View.VISIBLE);
            startscreen_new_character.setVisibility(View.VISIBLE);
        }
    }

    private void continueGame(final boolean createNewCharacter, final int loadFromSlot, final String name) {
        continueGame(createNewCharacter, loadFromSlot, name, Job.JOBS[0]);
    }

    private void continueGame(final boolean createNewCharacter, final int loadFromSlot,
                              final String name, final Job job) {
        final WorldSetup setup = AndorsTrailApplication.getApplicationFromActivity(this).setup;
        setup.createNewCharacter = createNewCharacter;
        setup.loadFromSlot = loadFromSlot;
        setup.newHeroName = name;
        setup.newHeroJob = job;
        setup.newHeroGender = gender;
        startActivity(new Intent(this, LoadingActivity.class));
    }

    private void createNewGame() {
        final String name = startscreen_enterheroname.getText().toString().trim();
        if (name == null || name.length() <= 0) {
            Toast.makeText(this, R.string.startscreen_enterheroname, Toast.LENGTH_SHORT).show();
            return;
        }

        if (gender == null) {
            Toast.makeText(this, R.string.startscreen_no_gender_selected, Toast.LENGTH_SHORT).show();
            return;
        }

        if (job == null) {
            Toast.makeText(this, R.string.startscreen_nojobselected, Toast.LENGTH_SHORT).show();
            return;
        }

        continueGame(true, 0, name, job);
    }

    private void comfirmNewGame() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.startscreen_newgame)
            .setMessage(R.string.startscreen_newgame_confirm)
            .setIcon(android.R.drawable.ic_delete)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    //continueGame(true);
                    hasExistingGame = false;
                    setButtonState(null, null);
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .create().show();
    }

    /*
       private void comfirmQuit() {
       new AlertDialog.Builder(this)
       .setTitle(R.string.dialog_confirmexit_title)
       .setMessage(R.string.dialog_confirmexit_message)
       .setIcon(android.R.drawable.ic_dialog_alert)
       .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
       @Override
       public void onClick(DialogInterface dialog, int which) {
       StartScreenActivity.this.finish();
       }
       })
       .setNegativeButton(android.R.string.cancel, null)
       .create().show();
       }
       */
}
