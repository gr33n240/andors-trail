package com.gpl.rpg.AndorsTrail.activity;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.gpl.rpg.AndorsTrail.AndorsTrailApplication;
import com.gpl.rpg.AndorsTrail.R;

public final class AboutActivity extends Activity {
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        AndorsTrailApplication app = AndorsTrailApplication.getApplicationFromActivity(this);
        AndorsTrailApplication.setWindowParameters(this, app.preferences);
        
        setContentView(R.layout.about);
    	final Resources res = getResources();
    	
        final TextView tv = (TextView) findViewById(R.id.about_contents);
        tv.setText(Html.fromHtml(res.getString(R.string.about_contents1)));
        
        Button b = (Button) findViewById(R.id.about_button1);
        b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				tv.setText(Html.fromHtml(res.getString(R.string.about_contents1)));
			}
		});
        
        
        b = (Button) findViewById(R.id.about_button2);
        b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				tv.setText(Html.fromHtml(res.getString(R.string.about_authors)));
			}
		});
        
        b = (Button) findViewById(R.id.about_button3);
        b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				tv.setText(Html.fromHtml(res.getString(R.string.about_contents3)));
			}
		});
        
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        
        TextView t = (TextView) findViewById(R.id.about_version);
        t.setText("v" + AndorsTrailApplication.CURRENT_VERSION_DISPLAY);
    }
}
