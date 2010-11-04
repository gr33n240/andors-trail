package com.gpl.rpg.AndorsTrail.view;

import com.gpl.rpg.AndorsTrail.R;
import com.gpl.rpg.AndorsTrail.util.Range;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public final class RangeBar extends RelativeLayout {
	private final ProgressBar progressBar;
	private final TextView progressBarText;
	private final TextView labelText;
	
	public RangeBar(Context context, AttributeSet attr) {
		super(context, attr);
        setFocusable(false);
        inflate(context, R.layout.rangebar, this);
        
        progressBarText = (TextView) findViewById(R.id.rangebar_text);
        progressBar = (ProgressBar) findViewById(R.id.rangebar_progress);
        labelText = (TextView) findViewById(R.id.rangebar_label);
    }

	public void init(int drawableID, int labelTextID) {
		// Wow, you actually need to call this twice (!), or the progressbar won't show the progress image, just the background.
		// TODO: investigate strangeness of setProgressDrawable
		progressBar.setProgressDrawable(getResources().getDrawable(drawableID));
		progressBar.setProgressDrawable(getResources().getDrawable(drawableID));
		labelText.setText(labelTextID);
	}
	
	public void update(final Range range) {
		progressBar.setMax(range.max);
		progressBar.setProgress(Math.min(range.current, range.max));
		progressBarText.setText(range.toString());
		invalidate();
	}
}
