package com.olsc.manorbrowser.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 已信任证书存储管理类
 */
public class TrustedCertificateStorage {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static class TrustedCertificate {
        public long id;
        public String host;
        public String fingerprint;
        public byte[] certificateData;
        public long addedTime;

        public TrustedCertificate(long id, String host, String fingerprint, byte[] certificateData, long addedTime) {
            this.id = id;
            this.host = host;
            this.fingerprint = fingerprint;
            this.certificateData = certificateData;
            this.addedTime = addedTime;
        }
    }

    /**
     * 添加信任证书
     */
    public static void addTrustedCertificate(Context context, String host, String fingerprint, byte[] certificateData) {
        if (host == null || fingerprint == null) return;
        executor.execute(() -> {
            CertificateDatabaseHelper dbHelper = new CertificateDatabaseHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(CertificateDatabaseHelper.COLUMN_HOST, host);
            values.put(CertificateDatabaseHelper.COLUMN_FINGERPRINT, fingerprint);
            values.put(CertificateDatabaseHelper.COLUMN_CERT_DATA, certificateData);
            values.put(CertificateDatabaseHelper.COLUMN_ADDED_TIME, System.currentTimeMillis());
            
            // 使用 replace 避免重复
            db.replace(CertificateDatabaseHelper.TABLE_CERTIFICATES, null, values);
            db.close();
        });
    }

    /**
     * 检查证书是否已信任
     */
    public static boolean isCertificateTrusted(Context context, String host, String fingerprint) {
        if (host == null || fingerprint == null) return false;
        CertificateDatabaseHelper dbHelper = new CertificateDatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(CertificateDatabaseHelper.TABLE_CERTIFICATES,
                new String[]{CertificateDatabaseHelper.COLUMN_ID},
                CertificateDatabaseHelper.COLUMN_HOST + " = ? AND " + CertificateDatabaseHelper.COLUMN_FINGERPRINT + " = ?",
                new String[]{host, fingerprint}, null, null, null);
        
        boolean trusted = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        db.close();
        return trusted;
    }

    /**
     * 加载所有已信任证书
     */
    public static List<TrustedCertificate> loadAll(Context context) {
        List<TrustedCertificate> list = new ArrayList<>();
        CertificateDatabaseHelper dbHelper = new CertificateDatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(CertificateDatabaseHelper.TABLE_CERTIFICATES, null, null, null, null, null,
                CertificateDatabaseHelper.COLUMN_ADDED_TIME + " DESC");
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(new TrustedCertificate(
                        cursor.getLong(cursor.getColumnIndexOrThrow(CertificateDatabaseHelper.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(CertificateDatabaseHelper.COLUMN_HOST)),
                        cursor.getString(cursor.getColumnIndexOrThrow(CertificateDatabaseHelper.COLUMN_FINGERPRINT)),
                        cursor.getBlob(cursor.getColumnIndexOrThrow(CertificateDatabaseHelper.COLUMN_CERT_DATA)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(CertificateDatabaseHelper.COLUMN_ADDED_TIME))
                ));
            }
            cursor.close();
        }
        db.close();
        return list;
    }

    /**
     * 删除信任证书
     */
    public static void delete(Context context, long id) {
        executor.execute(() -> {
            CertificateDatabaseHelper dbHelper = new CertificateDatabaseHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.delete(CertificateDatabaseHelper.TABLE_CERTIFICATES,
                    CertificateDatabaseHelper.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(id)});
            db.close();
        });
    }
}
