package com.boredream.im.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.boredream.im.R;
import com.boredream.im.utils.TitleBuilder;

public class DetailActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_detail);

		new TitleBuilder(this)
				.setTitleText("fragment1")
				.setLeftImage(R.drawable.ic_launcher)
				.setLeftOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						finish();
					}
				});

	}

}
