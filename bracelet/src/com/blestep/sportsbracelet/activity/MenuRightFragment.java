package com.blestep.sportsbracelet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.utils.SPUtiles;

public class MenuRightFragment extends Fragment implements OnClickListener {
    private View mView;
    private MainActivity mainActivity;
    private TextView tv_user_name;
    private ImageView iv_user_pic;
    private RelativeLayout rl_center_userinfo, rl_center_target,
            rl_center_clear;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mainActivity = (MainActivity) getActivity();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.right_menu, container, false);
        }
        tv_user_name = (TextView) mView.findViewById(R.id.tv_user_name);
        iv_user_pic = (ImageView) mView.findViewById(R.id.iv_user_pic);
        mView.findViewById(R.id.rl_center_userinfo).setOnClickListener(this);
        mView.findViewById(R.id.rl_center_target).setOnClickListener(this);
        mView.findViewById(R.id.rl_center_clear).setOnClickListener(this);

        return mView;
    }

    @Override
    public void onResume() {
        iniData();
        super.onResume();
    }

    private void iniData() {
        int gender = SPUtiles.getIntValue(BTConstants.SP_KEY_USER_GENDER, 0);
        String name = SPUtiles.getStringValue(BTConstants.SP_KEY_USER_NAME, "");
        switch (gender) {
            case 0:
                iv_user_pic.setImageResource(R.drawable.user_head_male);
                break;
            case 1:
                iv_user_pic.setImageResource(R.drawable.user_head_female);
                break;

            default:
                break;
        }
        tv_user_name.setText(name);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_center_userinfo:
                startActivityForResult(new Intent(mainActivity, UserInfoLayoutActivity.class), BTConstants.REQUEST_CODE_USERINFO);
                break;
            case R.id.rl_center_target:
                Intent i = new Intent(mainActivity, TargetLayoutActivity.class);
                i.putExtra("is_setting", true);
                startActivityForResult(i, BTConstants.REQUEST_CODE_TARGET);
                break;
            case R.id.rl_center_clear:
                startActivityForResult(new Intent(mainActivity, ClearDataActivity.class), BTConstants.REQUEST_CODE_CLEAR);
                break;

            default:
                break;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BTConstants.REQUEST_CODE_USERINFO) {
            if (resultCode == mainActivity.RESULT_CANCELED) {
                mainActivity.mNeedRefreshData = false;
            } else if (resultCode == mainActivity.RESULT_OK) {
                mainActivity.mNeedRefreshData = true;
            }
        } else if (requestCode == BTConstants.REQUEST_CODE_TARGET) {
            if (resultCode == mainActivity.RESULT_CANCELED) {
                mainActivity.mNeedRefreshData = false;
            } else if (resultCode == mainActivity.RESULT_OK) {
                mainActivity.mNeedRefreshData = true;
            }
        } else if (requestCode == BTConstants.REQUEST_CODE_CLEAR) {
            if (resultCode == mainActivity.RESULT_CANCELED) {
                mainActivity.mNeedRefreshData = false;
            } else if (resultCode == mainActivity.RESULT_OK) {
                mainActivity.mNeedRefreshData = true;
            }
        }
    }
}
