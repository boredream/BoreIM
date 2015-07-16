package com.boredream.im.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.boredream.im.R;
import com.boredream.im.fragment.FragmentController;

public class MainActivity extends FragmentActivity implements OnCheckedChangeListener {

	private FragmentController fc;
	private RadioGroup rg;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initView();
		
		init();
	}

	private void initView() {
		rg = (RadioGroup) findViewById(R.id.rg);
		rg.setOnCheckedChangeListener(this);
	}
	
	private void init() {
		fc = new FragmentController(this, R.id.fl_container);
		((RadioButton)rg.getChildAt(0)).setChecked(true);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.rb_home:
			fc.showFragment(0);
			break;
		case R.id.rb_message:
			fc.showFragment(1);
			break;
		case R.id.rb_search:
			fc.showFragment(2);
			break;

		default:
			break;
		}
	}
	
}
