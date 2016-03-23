package com.blestep.sportsbracelet.fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.activity.HistoryActivity;
import com.blestep.sportsbracelet.entity.Step;
import com.blestep.sportsbracelet.event.HistoryChangeUnitClick;
import com.blestep.sportsbracelet.module.LogModule;
import com.blestep.sportsbracelet.utils.DataRetriever;
import com.blestep.sportsbracelet.utils.Utils;
import com.db.chart.Tools;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.Bar;
import com.db.chart.model.BarSet;
import com.db.chart.view.BarChartView;
import com.db.chart.view.XController;
import com.db.chart.view.YController;

import de.greenrobot.event.EventBus;

public class HistoryStepCalorie extends Fragment implements
		OnEntryClickListener, OnCheckedChangeListener {
	private static final String TAG = HistoryStepCalorie.class.getSimpleName();
	private String mLabels[];
	private String mValues[];
	private SimpleDateFormat mSdf;
	private Calendar mCalendar;
	private TextView history_calorie_daily, tv_history_calorie_daily,
			tv_history_calorie_sum;
	private View mView;
	private HistoryActivity mActivity;
	private BarChartView bcv_calorie;
	private TextView mBarTooltip;
	private RelativeLayout rl_pre_and_next;
	private RadioGroup rg_history_bottom_tab_parent;
	private boolean mIsPreDayShow;
	private boolean mIsPreWeekShow;
	private boolean mIsPreYearShow;
	private boolean mIsNextDayShow;
	private boolean mIsNextWeekShow;
	private boolean mIsNextYearShow;

	public Calendar mLastDayCalendar;// 上一周的最后一天
	public Calendar mLastWeekCalendar;// 7周前的周一
	public Calendar mLastMonthCalendar;// 一年前的今天
	// 手势
	private GestureDetectorCompat mDetector;
	/**
	 * 绘图完成后的操作
	 */
	private Runnable mEndAction = new Runnable() {
		@Override
		public void run() {
			mHandler.sendEmptyMessage(0);
		}
	};
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				// 将按钮都释放开
				mView.findViewById(R.id.rb_history_unit_day).setEnabled(true);
				mView.findViewById(R.id.rb_history_unit_week).setEnabled(true);
				mView.findViewById(R.id.rb_history_unit_month).setEnabled(true);
				mView.findViewById(R.id.rb_history_unit_year).setEnabled(true);
				if (mIsPreDayShow || mIsPreWeekShow || mIsPreYearShow) {
					mView.findViewById(R.id.btn_pre).setEnabled(true);
				} else {
					mView.findViewById(R.id.btn_pre).setEnabled(false);
				}
				if (mIsNextDayShow || mIsNextWeekShow || mIsNextYearShow) {
					mView.findViewById(R.id.btn_next).setEnabled(true);
				} else {
					mView.findViewById(R.id.btn_next).setEnabled(false);
				}
				break;

			default:
				break;
			}
		};
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		LogModule.i(TAG + "onActivityCreated");
		rg_history_bottom_tab_parent.setOnCheckedChangeListener(this);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onResume() {
		LogModule.i(TAG + "onResume");
		super.onResume();
	}

	@Override
	public void onPause() {
		LogModule.i(TAG + "onPause");
		super.onPause();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LogModule.i(TAG + "onCreateView");
		EventBus.getDefault().register(this);
		mActivity = (HistoryActivity) getActivity();
		LogModule.i(TAG + "onCreateView-->" + mActivity.selectHistoryUnit);
		mView = inflater.inflate(R.layout.history_step_calorie, container,
				false);
		initView();
		mIsPreDayShow = false;
		mIsNextDayShow = false;
		mIsPreWeekShow = false;
		mIsNextWeekShow = false;
		mIsPreYearShow = false;
		mIsNextYearShow = false;
		rg_history_bottom_tab_parent.setOnCheckedChangeListener(null);

		initData(HistoryActivity.COUNT_NUMBER_DAY, mActivity.selectHistoryUnit,
				mLastDayCalendar, null);
		rg_history_bottom_tab_parent.check(R.id.rb_history_unit_day);

		mDetector = new GestureDetectorCompat(mActivity,
				new GestureDetector.SimpleOnGestureListener() {
					@Override
					public boolean onFling(MotionEvent e1, MotionEvent e2,
							float velocityX, float velocityY) {
						if (Math.abs(e1.getX() - e2.getX()) > 100
								&& velocityX > 200) {
							// 前
							onFlingPre();

						}
						if (Math.abs(e1.getX() - e2.getX()) > 100
								&& velocityX < -200) {
							// 后
							onFlingNext();
						}
						return true;
					}
				});
		bcv_calorie.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mDetector.onTouchEvent(event);
				return false;
			}
		});
		return mView;
	}

	@Override
	public void onDestroyView() {
		LogModule.i(TAG + "onDestroyView");
		EventBus.getDefault().unregister(this);
		super.onDestroyView();
	}

	public void onEvent(HistoryChangeUnitClick event) {
		LogModule.i(TAG + "onEvent-->" + event.selectHistoryUnit);
		mIsPreDayShow = false;
		mIsNextDayShow = false;
		mIsPreWeekShow = false;
		mIsNextWeekShow = false;
		mIsPreYearShow = false;
		mIsNextYearShow = false;
		rg_history_bottom_tab_parent.setOnCheckedChangeListener(null);
		switch (event.selectHistoryUnit) {
		case HistoryActivity.DATA_UNIT_DAY:
			initData(HistoryActivity.COUNT_NUMBER_DAY, event.selectHistoryUnit,
					null, null);
			rg_history_bottom_tab_parent.check(R.id.rb_history_unit_day);
			break;
		case HistoryActivity.DATA_UNIT_WEEK:
			initData(HistoryActivity.COUNT_NUMBER_WEEK,
					event.selectHistoryUnit, null, null);
			rg_history_bottom_tab_parent.check(R.id.rb_history_unit_week);
			break;
		case HistoryActivity.DATA_UNIT_MONTH:
			initData(HistoryActivity.COUNT_NUMBER_MONTH,
					event.selectHistoryUnit, null, event);
			rg_history_bottom_tab_parent.check(R.id.rb_history_unit_month);
			break;
		case HistoryActivity.DATA_UNIT_YEAR:
			initData(HistoryActivity.COUNT_NUMBER_YEAR,
					event.selectHistoryUnit, null, event);
			rg_history_bottom_tab_parent.check(R.id.rb_history_unit_year);
			break;
		}
		rg_history_bottom_tab_parent.setOnCheckedChangeListener(this);
	}

	private void initData(int labelsCount, int unit, Calendar calendar,
			HistoryChangeUnitClick event) {
		mLabels = new String[labelsCount];
		mValues = new String[labelsCount];
		mSdf = new SimpleDateFormat(BTConstants.PATTERN_MM_DD);
		if (calendar == null) {
			mCalendar = (Calendar) mActivity.mTodayCalendar.clone();
		} else {
			mCalendar = (Calendar) calendar.clone();
		}
		// 日
		if (unit == HistoryActivity.DATA_UNIT_DAY) {
			for (int i = labelsCount - 1; i >= 0; i--) {
				if (mCalendar.getTime().compareTo(
						mActivity.mTodayCalendar.getTime()) >= 0) {
					mLabels[i] = getString(R.string.history_today);
					mCalendar.add(Calendar.DAY_OF_MONTH, -1);
					continue;
				}
				mLabels[i] = mSdf.format(mCalendar.getTime());
				mCalendar.add(Calendar.DAY_OF_MONTH, -1);
			}
			updateBarChartByDay(labelsCount, calendar == null ? null
					: (Calendar) calendar.clone());
		}
		// 周
		if (unit == HistoryActivity.DATA_UNIT_WEEK) {
			Calendar monday = (Calendar) mCalendar.clone();
			if (monday.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				monday.add(Calendar.DAY_OF_MONTH, -1);
			}
			monday.setFirstDayOfWeek(Calendar.MONDAY);
			monday.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			for (int i = labelsCount - 1; i >= 0; i--) {
				if (monday.get(Calendar.WEEK_OF_YEAR) == mActivity.mTodayCalendar
						.get(Calendar.WEEK_OF_YEAR)) {
					mLabels[i] = getString(R.string.history_this_week);
					monday.add(Calendar.WEEK_OF_MONTH, -1);
					continue;
				}
				mLabels[i] = getString(R.string.history_week_number,
						monday.get(Calendar.WEEK_OF_YEAR));
				monday.add(Calendar.WEEK_OF_MONTH, -1);
			}
			updateBarChartByWeek(labelsCount, calendar == null ? null
					: (Calendar) calendar.clone());
		}
		// 月
		if (unit == HistoryActivity.DATA_UNIT_MONTH) {
			if (event == null) {
				event = new HistoryChangeUnitClick(
						HistoryActivity.DATA_UNIT_MONTH);
				event = mActivity.getMonthData(mCalendar, event);
			}
			updateBarChartByMonth(event);
			Calendar preOrNextCalendar;
			if (calendar == null) {
				preOrNextCalendar = (Calendar) mActivity.mTodayCalendar.clone();
			} else {
				preOrNextCalendar = (Calendar) calendar.clone();
			}
			isPreMonthEnable(preOrNextCalendar);
			isNextMonthEnable(preOrNextCalendar);
			mValues = event.valuesCalorie;
		}
		// 年
		if (unit == HistoryActivity.DATA_UNIT_YEAR) {
			mValues = event.valuesCalorie;
			updateBarChartByYear(event);
		}
		int stepSum = 0;
		for (int i = 0; i < mValues.length; i++) {
			if (Utils.isEmpty(mValues[i])) {
				stepSum += 0;
			} else {
				stepSum += Integer.valueOf(mValues[i]);
			}
		}
		tv_history_calorie_sum.setText(stepSum + "");
		tv_history_calorie_daily.setText((int) (stepSum / mValues.length) + "");
		if (unit == HistoryActivity.DATA_UNIT_DAY) {
			history_calorie_daily
					.setText(getString(R.string.history_calorie_daily));
		}
		if (unit == HistoryActivity.DATA_UNIT_WEEK) {
			history_calorie_daily
					.setText(getString(R.string.history_calorie_week));
		}
		if (unit == HistoryActivity.DATA_UNIT_MONTH) {
			history_calorie_daily
					.setText(getString(R.string.history_calorie_month));
		}
		if (unit == HistoryActivity.DATA_UNIT_YEAR) {
			history_calorie_daily
					.setText(getString(R.string.history_calorie_year));
		}
	}

	/**
	 * 判断是否可点击前一天
	 * 
	 * @param labelsCount
	 */
	private void isPreDayEnable(Calendar calendar) {
		// 前一天是否可点击
		mLastDayCalendar = (Calendar) calendar.clone();
		mLastDayCalendar.add(Calendar.DAY_OF_MONTH,
				-HistoryActivity.COUNT_NUMBER_DAY);
		if (mActivity.mStepsMap.get(Utils.calendar2strDate(mLastDayCalendar,
				BTConstants.PATTERN_YYYY_MM_DD)) != null
				&& mLastDayCalendar.getTime().compareTo(
						mActivity.m7YearAgoCalendar.getTime()) >= 0) {
			mIsPreDayShow = true;
		} else {
			mIsPreDayShow = false;
		}
	}

	/**
	 * 判断是否可点击后一天
	 * 
	 * @param calendar
	 * 
	 * @param labelsCount
	 */
	private void isNextDayEnable(Calendar calendar) {
		mLastDayCalendar.add(Calendar.DAY_OF_MONTH,
				HistoryActivity.COUNT_NUMBER_DAY);
		if (mLastDayCalendar.getTime().compareTo(
				mActivity.mTodayCalendar.getTime()) >= 0) {
			mIsNextDayShow = false;
		} else {
			mIsNextDayShow = true;
		}
	}

	/**
	 * 计算以日为单位的运动量
	 * 
	 * @param labelsCount
	 */
	private void updateBarChartByDay(int labelsCount, Calendar calendar) {
		// 构建柱状图
		bcv_calorie.reset();
		BarSet data = new BarSet();
		rl_pre_and_next.setVisibility(View.VISIBLE);
		if (calendar == null) {
			calendar = (Calendar) mActivity.mTodayCalendar.clone();
		}
		calendar.add(Calendar.DAY_OF_MONTH, 1 - labelsCount);
		ArrayList<Step> stepsSort = new ArrayList<Step>();
		for (int i = 0; i < labelsCount; i++) {
			int dayStep = 0;
			if (mActivity.mStepsMap.get(Utils.calendar2strDate(calendar,
					BTConstants.PATTERN_YYYY_MM_DD)) != null) {
				dayStep = Integer.valueOf(mActivity.mStepsMap.get(Utils
						.calendar2strDate(calendar,
								BTConstants.PATTERN_YYYY_MM_DD)).calories);
				stepsSort.add(mActivity.mStepsMap.get(Utils.calendar2strDate(
						calendar, BTConstants.PATTERN_YYYY_MM_DD)));
			} else {
				stepsSort.add(new Step("0", "0", "0"));
			}
			mValues[i] = dayStep + "";
			Bar bar = new Bar(mLabels[i], dayStep);
			data.addBar(bar);
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		}
		// 找到最大的，与目标值对比
		int barStepMax = 100;
		Collections.sort(stepsSort, new StepCompare());
		if (Integer.valueOf(stepsSort.get(0).calories) >= barStepMax) {
			barStepMax = Integer.valueOf(stepsSort.get(0).calories);
		}
		data.setColor(getResources().getColor(R.color.blue_b4efff));
		bcv_calorie.addData(data);
		bcv_calorie.setBarSpacing((int) Tools.fromDpToPx(50));
		bcv_calorie.setSetSpacing(0);
		bcv_calorie.setBarBackground(false);
		bcv_calorie.setRoundCorners(0);
		bcv_calorie.setBorderSpacing(0).setGrid(null).setHorizontalGrid(null)
				.setVerticalGrid(null)
				.setYLabels(YController.LabelPosition.NONE).setYAxis(false)
				.setXLabels(XController.LabelPosition.OUTSIDE).setXAxis(true)
				.setMaxAxisValue(barStepMax, 1)
				.animate(DataRetriever.randAnimation(mEndAction, labelsCount));
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		isPreDayEnable(calendar);
		isNextDayEnable(calendar);
	}

	/**
	 * 是否可点击后一周
	 * 
	 * @param calendar
	 */
	private void isNextWeekEnable(Calendar calendar) {
		mLastWeekCalendar.setFirstDayOfWeek(Calendar.MONDAY);
		mLastWeekCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		mLastWeekCalendar.add(Calendar.WEEK_OF_MONTH,
				HistoryActivity.COUNT_NUMBER_WEEK);
		Calendar todayWeek = (Calendar) mActivity.mTodayCalendar.clone();
		todayWeek.setFirstDayOfWeek(Calendar.MONDAY);
		todayWeek.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		if (mLastWeekCalendar.getTime().compareTo(todayWeek.getTime()) >= 0) {
			mIsNextDayShow = false;
		} else {
			mIsNextDayShow = true;
		}
	}

	/**
	 * 是否可点击前一周
	 * 
	 * @param calendar
	 */
	private void isPreWeekEnable(Calendar calendar) {
		// 前一周是否可点击
		mLastWeekCalendar = (Calendar) calendar.clone();
		// 拿到当天所在周的周一
		mLastWeekCalendar.setFirstDayOfWeek(Calendar.MONDAY);
		mLastWeekCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		mLastWeekCalendar.add(Calendar.WEEK_OF_MONTH,
				-HistoryActivity.COUNT_NUMBER_WEEK);
		Calendar agoWeek = (Calendar) mActivity.m7YearAgoCalendar.clone();
		agoWeek.setFirstDayOfWeek(Calendar.MONDAY);
		agoWeek.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		// int weekCount = 0;
		// // 一周7天
		// for (int i = 0; i < 7; i++) {
		// if (mActivity.mStepsMap.get(Utils.calendar2strDate(
		// mLastWeekCalendar, BTConstants.PATTERN_YYYY_MM_DD)) != null) {
		// weekCount += Integer.valueOf(mActivity.mStepsMap.get(Utils
		// .calendar2strDate(mLastWeekCalendar,
		// BTConstants.PATTERN_YYYY_MM_DD)).count);
		// }
		// mLastWeekCalendar.add(Calendar.DAY_OF_MONTH, 1);
		// }
		// mLastWeekCalendar.add(Calendar.WEEK_OF_MONTH, -1);
		if (/*
			 * weekCount > 0 &&
			 */mLastWeekCalendar.getTime().compareTo(agoWeek.getTime()) >= 0) {
			mIsPreWeekShow = true;
		} else {
			mIsPreWeekShow = false;
		}
	}

	/**
	 * 计算以周为单位的运动量
	 * 
	 * @param labelsCount
	 * @param calendar
	 */
	private void updateBarChartByWeek(int labelsCount, Calendar calendar) {
		bcv_calorie.reset();
		BarSet data = new BarSet();
		rl_pre_and_next.setVisibility(View.VISIBLE);
		// 拿到最新的数据开始计算日期
		if (calendar == null) {
			calendar = (Calendar) mActivity.mTodayCalendar.clone();
		}
		if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
			calendar.add(Calendar.DAY_OF_MONTH, -1);
		}
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		// 拿到当天所在周的周一
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		calendar.add(Calendar.WEEK_OF_MONTH, 1 - labelsCount);
		int[] sortData = new int[labelsCount];
		for (int i = 0; i < labelsCount; i++) {
			int weekCount = 0;
			// 一周7天
			for (int j = 0; j < 7; j++) {
				if (mActivity.mStepsMap.get(Utils.calendar2strDate(calendar,
						BTConstants.PATTERN_YYYY_MM_DD)) != null) {
					weekCount += Integer.valueOf(mActivity.mStepsMap.get(Utils
							.calendar2strDate(calendar,
									BTConstants.PATTERN_YYYY_MM_DD)).calories);
				}
				calendar.add(Calendar.DAY_OF_MONTH, 1);
			}
			Bar bar = new Bar(mLabels[i], weekCount);
			mValues[i] = weekCount + "";
			sortData[i] = weekCount;
			data.addBar(bar);
		}
		Arrays.sort(sortData);
		int barStepMax = 100;
		if (sortData[labelsCount - 1] >= barStepMax) {
			barStepMax = sortData[labelsCount - 1];
		}
		data.setColor(getResources().getColor(R.color.blue_b4efff));
		bcv_calorie.addData(data);

		bcv_calorie.setBarSpacing((int) Tools.fromDpToPx(50));
		bcv_calorie.setSetSpacing(0);
		bcv_calorie.setBarBackground(false);
		bcv_calorie.setRoundCorners(0);
		bcv_calorie.setBorderSpacing(0).setGrid(null).setHorizontalGrid(null)
				.setVerticalGrid(null)
				.setYLabels(YController.LabelPosition.NONE).setYAxis(false)
				.setXLabels(XController.LabelPosition.OUTSIDE).setXAxis(true)
				.setMaxAxisValue(barStepMax, 1)
				.animate(DataRetriever.randAnimation(mEndAction, labelsCount));
		calendar.add(Calendar.WEEK_OF_MONTH, -1);
		isPreWeekEnable(calendar);
		isNextWeekEnable(calendar);
	}

	/**
	 * 是否可点击后一年
	 * 
	 * @param calendar
	 */
	private void isNextMonthEnable(Calendar calendar) {
		mLastMonthCalendar.set(Calendar.DAY_OF_MONTH, 1);
		mLastMonthCalendar.add(Calendar.MONTH,
				HistoryActivity.COUNT_NUMBER_MONTH);
		Calendar todayMonth = (Calendar) mActivity.mTodayCalendar.clone();
		todayMonth.set(Calendar.DAY_OF_MONTH, 1);
		if (mLastMonthCalendar.getTime().compareTo(todayMonth.getTime()) >= 0) {
			mIsNextYearShow = false;
		} else {
			mIsNextYearShow = true;
		}
	}

	/**
	 * 是否可点击前一年
	 * 
	 * @param calendar
	 */
	private void isPreMonthEnable(Calendar calendar) {
		// 前一周是否可点击
		mLastMonthCalendar = (Calendar) calendar.clone();
		// 拿到当天所在周的周一
		mLastMonthCalendar.set(Calendar.DAY_OF_MONTH, 1);
		mLastMonthCalendar.add(Calendar.MONTH,
				-HistoryActivity.COUNT_NUMBER_MONTH);
		Calendar agoMonth = (Calendar) mActivity.m7YearAgoCalendar.clone();
		agoMonth.set(Calendar.DAY_OF_MONTH, 1);
		/*
		 * int monthCount = 0; // 计算当月有多少天 int daysInMonth =
		 * calendar.getActualMaximum(Calendar.DATE); for (int i = 0; i <
		 * daysInMonth; i++) { if
		 * (mActivity.mStepsMap.get(Utils.calendar2strDate( mLastMonthCalendar,
		 * BTConstants.PATTERN_YYYY_MM_DD)) != null) { monthCount +=
		 * Integer.valueOf(mActivity.mStepsMap.get(Utils
		 * .calendar2strDate(mLastMonthCalendar,
		 * BTConstants.PATTERN_YYYY_MM_DD)).count); }
		 * mLastMonthCalendar.add(Calendar.DAY_OF_MONTH, 1); }
		 * mLastMonthCalendar.add(Calendar.MONTH, -1);
		 */
		if (/*
			 * monthCount > 0 &&
			 */mLastMonthCalendar.getTime().compareTo(agoMonth.getTime()) >= 0) {
			mIsPreYearShow = true;
		} else {
			mIsPreYearShow = false;
		}

	}

	/**
	 * 计算以月为单位的运动量
	 * 
	 * @param labelsCount
	 * @param calendar
	 */
	private void updateBarChartByMonth(HistoryChangeUnitClick event) {
		bcv_calorie.reset();
		bcv_calorie.addData(event.dataCalorie);
		bcv_calorie.setBarSpacing((int) Tools.fromDpToPx(20));
		bcv_calorie.setSetSpacing(0);
		bcv_calorie.setBarBackground(false);
		bcv_calorie.setRoundCorners(0);
		bcv_calorie
				.setBorderSpacing(0)
				.setGrid(null)
				.setHorizontalGrid(null)
				.setVerticalGrid(null)
				.setYLabels(YController.LabelPosition.NONE)
				.setYAxis(false)
				.setXLabels(XController.LabelPosition.OUTSIDE)
				.setXAxis(true)
				.setMaxAxisValue(event.barCalorieMax, 1)
				.animate(
						DataRetriever.randAnimation(mEndAction,
								mActivity.COUNT_NUMBER_MONTH));
	}

	/**
	 * 计算以年为单位的运动量
	 * 
	 * @param labelsCount
	 */
	private void updateBarChartByYear(HistoryChangeUnitClick event) {
		bcv_calorie.reset();
		bcv_calorie.addData(event.dataCalorie);
		bcv_calorie.setBarSpacing((int) Tools.fromDpToPx(50));
		bcv_calorie.setSetSpacing(0);
		bcv_calorie.setBarBackground(false);
		bcv_calorie.setRoundCorners(0);
		bcv_calorie
				.setBorderSpacing(0)
				.setGrid(null)
				.setHorizontalGrid(null)
				.setVerticalGrid(null)
				.setYLabels(YController.LabelPosition.NONE)
				.setYAxis(false)
				.setXLabels(XController.LabelPosition.OUTSIDE)
				.setXAxis(true)
				.setMaxAxisValue(event.barCalorieMax, 1)
				.animate(
						DataRetriever.randAnimation(mEndAction,
								mActivity.COUNT_NUMBER_YEAR));
	}

	private void initView() {
		bcv_calorie = (BarChartView) mView.findViewById(R.id.bcv_calorie);
		history_calorie_daily = (TextView) mView
				.findViewById(R.id.history_calorie_daily);
		tv_history_calorie_daily = (TextView) mView
				.findViewById(R.id.tv_history_calorie_daily);
		tv_history_calorie_sum = (TextView) mView
				.findViewById(R.id.tv_history_calorie_sum);
		bcv_calorie.setOnEntryClickListener(this);
		rg_history_bottom_tab_parent = (RadioGroup) mView
				.findViewById(R.id.rg_history_bottom_tab_parent);
		rl_pre_and_next = (RelativeLayout) mView
				.findViewById(R.id.rl_pre_and_next);
		mView.findViewById(R.id.btn_pre).setEnabled(false);
		mView.findViewById(R.id.btn_next).setEnabled(false);
	}

	@Override
	public void onClick(int setIndex, int entryIndex, Rect rect) {
		if (mBarTooltip == null)
			showBarTooltip(entryIndex, rect);
		else
			dismissBarTooltip(entryIndex, rect);

	}

	private void showBarTooltip(int index, Rect rect) {

		mBarTooltip = (TextView) mActivity.getLayoutInflater().inflate(
				R.layout.tooltip, null);
		mBarTooltip.setText("" + mValues[index]);

		LayoutParams layoutParams = new LayoutParams(rect.width()
				+ (int) Tools.fromDpToPx(40), LayoutParams.WRAP_CONTENT);
		layoutParams.leftMargin = rect.left - (int) Tools.fromDpToPx(20);
		layoutParams.topMargin = rect.top - (int) Tools.fromDpToPx(20);
		mBarTooltip.setLayoutParams(layoutParams);

		bcv_calorie.showTooltip(mBarTooltip);
		bcv_calorie.invalidate();
	}

	private void dismissBarTooltip(final int index, final Rect rect) {

		bcv_calorie.dismissTooltip(mBarTooltip);

		mBarTooltip = null;
		if (index != -1)
			showBarTooltip(index, rect);
	}

	private class StepCompare implements Comparator<Step> {

		@Override
		public int compare(Step lhs, Step rhs) {
			if (Integer.valueOf(lhs.calories) > Integer.valueOf(rhs.calories)) {
				return -1;
			} else if (Integer.valueOf(lhs.calories) < Integer
					.valueOf(rhs.calories)) {
				return 1;
			}
			return 0;
		}

	}

	private void onFlingPre() {
		// 前一天/周/年
		if (mIsPreDayShow
				&& mActivity.selectHistoryUnit == HistoryActivity.DATA_UNIT_DAY) {
			mLastDayCalendar.add(Calendar.DAY_OF_MONTH,
					-HistoryActivity.COUNT_NUMBER_DAY);
			initData(HistoryActivity.COUNT_NUMBER_DAY,
					mActivity.selectHistoryUnit, mLastDayCalendar, null);
		}
		if (mIsPreWeekShow
				&& mActivity.selectHistoryUnit == HistoryActivity.DATA_UNIT_WEEK) {
			mLastWeekCalendar.add(Calendar.WEEK_OF_MONTH,
					-HistoryActivity.COUNT_NUMBER_WEEK);
			initData(HistoryActivity.COUNT_NUMBER_WEEK,
					mActivity.selectHistoryUnit, mLastWeekCalendar, null);
		}
		if (mIsPreYearShow
				&& mActivity.selectHistoryUnit == HistoryActivity.DATA_UNIT_MONTH) {
			mLastMonthCalendar.add(Calendar.MONTH,
					-HistoryActivity.COUNT_NUMBER_MONTH);
			LogModule.i(TAG
					+ "--"
					+ new SimpleDateFormat(BTConstants.PATTERN_YYYY_MM_DD)
							.format(mLastMonthCalendar.getTime()));
			initData(HistoryActivity.COUNT_NUMBER_MONTH,
					mActivity.selectHistoryUnit, mLastMonthCalendar, null);
		}
	}

	private void onFlingNext() {
		// 后一天/周/年
		if (mIsNextDayShow
				&& mActivity.selectHistoryUnit == HistoryActivity.DATA_UNIT_DAY) {
			mLastDayCalendar.add(Calendar.DAY_OF_MONTH,
					HistoryActivity.COUNT_NUMBER_DAY);
			initData(HistoryActivity.COUNT_NUMBER_DAY,
					mActivity.selectHistoryUnit, mLastDayCalendar, null);
		}
		if (mIsNextWeekShow
				&& mActivity.selectHistoryUnit == HistoryActivity.DATA_UNIT_WEEK) {
			mLastWeekCalendar.add(Calendar.WEEK_OF_MONTH,
					HistoryActivity.COUNT_NUMBER_WEEK);
			initData(HistoryActivity.COUNT_NUMBER_WEEK,
					mActivity.selectHistoryUnit, mLastWeekCalendar, null);
		}
		if (mIsNextYearShow
				&& mActivity.selectHistoryUnit == HistoryActivity.DATA_UNIT_MONTH) {
			mLastMonthCalendar.add(Calendar.MONTH,
					HistoryActivity.COUNT_NUMBER_MONTH);
			LogModule.i(TAG
					+ "--"
					+ new SimpleDateFormat(BTConstants.PATTERN_YYYY_MM_DD)
							.format(mLastMonthCalendar.getTime()));
			initData(HistoryActivity.COUNT_NUMBER_MONTH,
					mActivity.selectHistoryUnit, mLastMonthCalendar, null);

		}

	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		mView.findViewById(R.id.rb_history_unit_day).setEnabled(false);
		mView.findViewById(R.id.rb_history_unit_week).setEnabled(false);
		mView.findViewById(R.id.rb_history_unit_month).setEnabled(false);
		mView.findViewById(R.id.rb_history_unit_year).setEnabled(false);
		switch (checkedId) {
		case R.id.rb_history_unit_day:
			EventBus.getDefault().postSticky(
					new HistoryChangeUnitClick(HistoryActivity.DATA_UNIT_DAY));
			break;
		case R.id.rb_history_unit_week:
			EventBus.getDefault().postSticky(
					new HistoryChangeUnitClick(HistoryActivity.DATA_UNIT_WEEK));
			break;
		case R.id.rb_history_unit_month:
			EventBus.getDefault()
					.postSticky(
							new HistoryChangeUnitClick(
									HistoryActivity.DATA_UNIT_MONTH));
			break;
		case R.id.rb_history_unit_year:
			EventBus.getDefault().postSticky(
					new HistoryChangeUnitClick(HistoryActivity.DATA_UNIT_YEAR));
			break;
		}
	}
}
