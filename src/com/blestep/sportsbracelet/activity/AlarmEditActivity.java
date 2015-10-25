package com.blestep.sportsbracelet.activity;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;
import com.blestep.sportsbracelet.db.DBTools;
import com.blestep.sportsbracelet.entity.Alarm;
import com.blestep.sportsbracelet.utils.ToastUtils;

public class AlarmEditActivity extends BaseActivity implements OnClickListener, OnItemClickListener {
	private ListView lv_alarm_edit;
	private ArrayList<Alarm> mAlarms = new ArrayList<Alarm>();
	private AlarmEditAdapter mAdapter;
	private ArrayList<String> mDelIndexs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_edit_page);
		initView();
		initListener();
		initData();
	}

	@Override
	protected void onResume() {
		mAlarms = DBTools.getInstance(this).selectAllAlarm();
		mAdapter.notifyDataSetChanged();
		super.onResume();
	}

	private void initView() {
		lv_alarm_edit = (ListView) findViewById(R.id.lv_alarm_edit);
	}

	private void initListener() {
		findViewById(R.id.iv_back).setOnClickListener(this);
		findViewById(R.id.tv_alarm_add).setOnClickListener(this);
		findViewById(R.id.tv_alarm_del).setOnClickListener(this);
		lv_alarm_edit.setOnItemClickListener(this);
	}

	private void initData() {
		mAdapter = new AlarmEditAdapter();
		mDelIndexs = new ArrayList<String>();
		lv_alarm_edit.setAdapter(mAdapter);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
		case R.id.iv_back:
			finish();
			break;
		case R.id.tv_alarm_add:
			startActivity(new Intent(this, AlarmAddActivity.class));
			break;
		case R.id.tv_alarm_del:
			if (mDelIndexs.size() == 0) {
				ToastUtils.showToast(this, R.string.alarm_del_tips);
				return;
			}
			for (int i = 0; i < mDelIndexs.size(); i++) {
				DBTools.getInstance(this).deleteAlarm(Integer.valueOf(mDelIndexs.get(i)));
			}
			mAlarms = DBTools.getInstance(this).selectAllAlarm();
			mAdapter.notifyDataSetChanged();
			break;

		default:
			break;
		}

	}

	class AlarmEditAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mAlarms.size();
		}

		@Override
		public Object getItem(int position) {
			return mAlarms.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final Alarm alarm = mAlarms.get(position);
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = AlarmEditActivity.this.getLayoutInflater().inflate(R.layout.alarm_edit_list_item, null);
				holder.tv_alarm_edit_item_name = (TextView) convertView.findViewById(R.id.tv_alarm_edit_item_name);
				holder.tv_alarm_edit_item_time = (TextView) convertView.findViewById(R.id.tv_alarm_edit_item_time);
				holder.cb_alarm_edit_item_switch = (CheckBox) convertView.findViewById(R.id.cb_alarm_edit_item_switch);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tv_alarm_edit_item_name.setText(alarm.name);
			holder.tv_alarm_edit_item_time.setText(alarm.time);
			holder.cb_alarm_edit_item_switch.setChecked(false);
			holder.cb_alarm_edit_item_switch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {
						mDelIndexs.add(alarm.id);
					} else {
						mDelIndexs.remove(alarm.id);
					}
				}
			});
			return convertView;
		}

		class ViewHolder {
			TextView tv_alarm_edit_item_name;
			TextView tv_alarm_edit_item_time;
			CheckBox cb_alarm_edit_item_switch;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent(this, AlarmAddActivity.class);
		intent.putExtra(BTConstants.EXTRA_KEY_ALARM, mAlarms.get(position));
		startActivity(intent);
	}
}