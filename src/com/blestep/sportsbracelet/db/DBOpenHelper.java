package com.blestep.sportsbracelet.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.blestep.sportsbracelet.module.LogModule;

public class DBOpenHelper extends SQLiteOpenHelper {
	private static final String DB_NAME = "db_name";
	// 数据库版本号
	private static final int DB_VERSION = 1;

	public DBOpenHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_STEP);
		LogModule.i("创建数据库");
	}

	/**
	 * 升级时数据库时调用
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	/**
	 * 删除数据库
	 * 
	 * @param context
	 * @return
	 */
	public boolean deleteDatabase(Context context) {
		return context.deleteDatabase(DB_NAME);
	}

	// 步数表
	private static final String CREATE_TABLE_STEP = "CREATE TABLE " + DBConstants.TABLE_NAME_STEP
	// id
			+ " (" + DBConstants.STEP_FIELD_ID + " INTEGER primary key autoincrement, "
			// 步数日期
			+ DBConstants.STEP_FIELD_DATE + " TEXT,"
			// 步数总数
			+ DBConstants.STEP_FIELD_COUNT + " TEXT,"
			// 步数距离
			+ DBConstants.STEP_FIELD_DISTANCE + "TEXT,"
			// 步数卡路里
			+ DBConstants.STEP_FIELD_CALORIES + " TEXT);";

	// // 睡眠表
	// private static final String sleep_creat_sql = "CREATE TABLE "
	// + "sleep_table" + " (sleep_id"
	// + " INTEGER primary key autoincrement, " + "sleep_date" + " text,"
	// + "sleep_allsleeptime" + " text," + "sleep_lightsleeptime"
	// + " text," + "sleep_deepsleeptime" + " text," + "sleep_index"
	// + " text," + "fragment_index" + " text);";
	// // 闹钟表
	// private static final String alarm_creat_sql = "CREATE TABLE "
	// + "alarm_table" + " (alarm_id"
	// + " INTEGER primary key autoincrement, " + "alarm_num" + " text,"
	// + "alarm_date1" + " text," + "alarm_date2" + " text, "
	// + "alarm_state" + " text);";

}