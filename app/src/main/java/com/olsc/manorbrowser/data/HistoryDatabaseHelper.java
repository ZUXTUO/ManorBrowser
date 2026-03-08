package com.olsc.manorbrowser.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 历史记录数据库助手类
 * 采用 SQLite 存储以便于进行权重计算、全文搜索及闲时整理。
 */
public class HistoryDatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "manor_history.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_HISTORY = "history";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_VISIT_COUNT = "visit_count";
    public static final String COLUMN_LAST_VISIT = "last_visit";
    public static final String COLUMN_CATEGORY = "category";

    public HistoryDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_HISTORY_TABLE = "CREATE TABLE " + TABLE_HISTORY + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_URL + " TEXT UNIQUE,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_CONTENT + " TEXT,"
                + COLUMN_VISIT_COUNT + " INTEGER DEFAULT 1,"
                + COLUMN_LAST_VISIT + " INTEGER,"
                + COLUMN_CATEGORY + " TEXT"
                + ")";
        db.execSQL(CREATE_HISTORY_TABLE);
        
        // 为搜索和推荐优化建立索引
        db.execSQL("CREATE INDEX idx_history_url ON " + TABLE_HISTORY + "(" + COLUMN_URL + ")");
        db.execSQL("CREATE INDEX idx_history_last_visit ON " + TABLE_HISTORY + "(" + COLUMN_LAST_VISIT + " DESC)");
        db.execSQL("CREATE INDEX idx_history_weight ON " + TABLE_HISTORY + "(" + COLUMN_VISIT_COUNT + " DESC, " + COLUMN_LAST_VISIT + " DESC)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 目前仅有版本 1，暂不处理升级逻辑
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        onCreate(db);
    }
}
