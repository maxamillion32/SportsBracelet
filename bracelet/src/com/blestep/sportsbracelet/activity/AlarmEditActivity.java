package com.blestep.sportsbracelet.activity;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
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
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

public class AlarmEditActivity extends BaseActivity implements OnClickListener,
        OnItemClickListener {
    private ListView lv_alarm_edit;
    private ArrayList<Alarm> mAlarms = new ArrayList<>();
    private AlarmEditAdapter mAdapter;
    private ArrayList<String> mDelIndexs;
    private String[] mAlarmTypes;
    private String[] mAlarmDates;

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
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
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
        mAlarmTypes = getResources().getStringArray(R.array.alarm_types);
        mAlarmDates = getResources().getStringArray(R.array.alarm_period);
        mAdapter = new AlarmEditAdapter();
        mDelIndexs = new ArrayList<>();
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
                AlertDialog.Builder builder = new Builder(this);
                builder.setMessage(R.string.alarm_del_dialog);
                builder.setPositiveButton(R.string.alarm_del_confirm,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (int i = 0; i < mDelIndexs.size(); i++) {
                                    DBTools.getInstance(AlarmEditActivity.this)
                                            .deleteAlarm(
                                                    Integer.parseInt(mDelIndexs
                                                            .get(i)));
                                }
                                mAlarms = DBTools.getInstance(
                                        AlarmEditActivity.this).selectAllAlarm();
                                mAdapter.notifyDataSetChanged();
                                dialog.dismiss();
                            }
                        });
                builder.setNegativeButton(R.string.alarm_del_cancel,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.show();

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
                convertView = AlarmEditActivity.this.getLayoutInflater()
                        .inflate(R.layout.alarm_edit_list_item, null);
                holder.tv_alarm_edit_item_name = (TextView) convertView
                        .findViewById(R.id.tv_alarm_edit_item_name);
                holder.tv_alarm_edit_item_time = (TextView) convertView
                        .findViewById(R.id.tv_alarm_edit_item_time);
                holder.tv_alarm_edit_item_period = (TextView) convertView
                        .findViewById(R.id.tv_alarm_edit_item_period);
                holder.tv_alarm_edit_item_type = (TextView) convertView
                        .findViewById(R.id.tv_alarm_edit_item_type);
                holder.cb_alarm_edit_item_switch = (CheckBox) convertView
                        .findViewById(R.id.cb_alarm_edit_item_switch);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv_alarm_edit_item_name.setText(alarm.name);
            holder.tv_alarm_edit_item_time.setText(alarm.time);
            // 拆分周期
            String state = alarm.state;
            StringBuilder sb = new StringBuilder();
            if ("1111111".equals(state.substring(1, state.length()))) {
                sb.append(getString(R.string.alarm_every_day));
            } else if ("11111".equals(state.substring(3, state.length()))
                    && "00".equals(state.substring(1, 3))) {
                sb.append(getString(R.string.alarm_weekdays));
            } else if ("00000".equals(state.substring(3, state.length()))
                    && "11".equals(state.substring(1, 3))) {
                sb.append(getString(R.string.alarm_weekends));
            } else {
                for (int i = 1; i < state.length(); i++) {
                    if ("1".equals(state.substring(i, i + 1))) {
                        sb.append(mAlarmDates[i - 1]);
                        if (i < state.length()) {
                            sb.append(" ");
                        }
                    }
                }
            }
            holder.tv_alarm_edit_item_period.setText(sb.toString());
            holder.tv_alarm_edit_item_type.setText(mAlarmTypes[Integer.parseInt(alarm.type)]);
            holder.cb_alarm_edit_item_switch.setChecked(false);
            holder.cb_alarm_edit_item_switch
                    .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(CompoundButton buttonView,
                                                     boolean isChecked) {
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
            TextView tv_alarm_edit_item_period;
            TextView tv_alarm_edit_item_type;
            CheckBox cb_alarm_edit_item_switch;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        Intent intent = new Intent(this, AlarmAddActivity.class);
        intent.putExtra(BTConstants.EXTRA_KEY_ALARM, mAlarms.get(position));
        startActivity(intent);
    }
}
