package com.boredream.im.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.boredream.im.R;
import com.boredream.im.utils.TitleBuilder;

public class SearchFragment extends Fragment {

	private View view;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = View.inflate(getActivity(), R.layout.frag_search, null);

		new TitleBuilder(view)
			.setTitleText("Search")
			.setRightImage(R.drawable.ic_launcher)
			.setRightOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Toast.makeText(getActivity(), "right click~", Toast.LENGTH_SHORT).show();
				}
			});

		return view;
	}

}
