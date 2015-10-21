package com.blestep.sportsbracelet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.service.BTService;
import com.blestep.sportsbracelet.utils.SPUtiles;

public class MenuLeftFragment extends Fragment implements OnClickListener {
	private View mView;
	private MainActivity mainActivity;
	private TextView tv_bracelet_name;
	private ImageView iv_battery_state, iv_conn_state;
	private CheckBox cb_alert_low_battery;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mainActivity = (MainActivity) getActivity();
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (mView == null) {
			mView = inflater.inflate(R.layout.left_menu, container, false);
		}
		initView();
		initListener();
		initData();
		return mView;
	}

	private void initView() {
		tv_bracelet_name = (TextView) mView.findViewById(R.id.tv_bracelet_name);
		iv_battery_state = (ImageView) mView.findViewById(R.id.iv_battery_state);
		iv_conn_state = (ImageView) mView.findViewById(R.id.iv_conn_state);
		cb_alert_low_battery = (CheckBox) mView.findViewById(R.id.cb_alert_low_battery);
	}

	private void initListener() {
		mView.findViewById(R.id.rl_alert_coming_call).setOnClickListener(this);
		mView.findViewById(R.id.rl_alert_alarm).setOnClickListener(this);
		mView.findViewById(R.id.rl_bind_bracelet).setOnClickListener(this);
		mView.findViewById(R.id.rl_bracelet_reset).setOnClickListener(this);
		mView.findViewById(R.id.rl_about).setOnClickListener(this);
	}

	private void initData() {
		tv_bracelet_name.setText(SPUtiles.getStringValue(SPUtiles.SP_KEY_DEVICE_NAME, ""));
	}

	public void updateView(BTService mBtService) {
		if (mBtService.isConnDevice()) {
			iv_conn_state.setImageResource(R.drawable.conn_state_success);
		} else {
			iv_conn_state.setImageResource(R.drawable.conn_state_failure);
		}
		int battery = SPUtiles.getIntValue(SPUtiles.SP_KEY_BATTERY, 0);
		if (battery == 0) {
			iv_battery_state.setImageResource(R.drawable.battery_one);
		}
		if (battery == 25) {
			iv_battery_state.setImageResource(R.drawable.battery_one);
		}
		if (battery == 50) {
			iv_battery_state.setImageResource(R.drawable.battery_two);
		}
		if (battery == 75) {
			iv_battery_state.setImageResource(R.drawable.battery_three);
		}
		if (battery == 100) {
			iv_battery_state.setImageResource(R.drawable.battery_four);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_alert_coming_call:
			startActivity(new Intent(mainActivity, PhoneComingActivity.class));
			break;
		case R.id.rl_alert_alarm:

			break;
		case R.id.rl_bind_bracelet:

			break;
		case R.id.rl_bracelet_reset:

			break;
		case R.id.rl_about:

			break;

		default:
			break;
		}

	}

}