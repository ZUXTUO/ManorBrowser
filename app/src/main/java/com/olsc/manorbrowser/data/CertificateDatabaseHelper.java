package com.olsc.manorbrowser.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 已信任证书数据库助手类
 */
public class CertificateDatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "manor_certificates.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_CERTIFICATES = "trusted_certificates";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_HOST = "host";
    public static final String COLUMN_FINGERPRINT = "fingerprint";
    public static final String COLUMN_ADDED_TIME = "added_time";

    public CertificateDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_CERTIFICATES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_HOST + " TEXT,"
                + COLUMN_FINGERPRINT + " TEXT,"
                + COLUMN_ADDED_TIME + " INTEGER,"
                + "UNIQUE(" + COLUMN_HOST + ", " + COLUMN_FINGERPRINT + ")"
                + ")";
        db.execSQL(CREATE_TABLE);
        db.execSQL("CREATE INDEX idx_cert_host ON " + TABLE_CERTIFICATES + "(" + COLUMN_HOST + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CERTIFICATES);
        onCreate(db);
    }
}
