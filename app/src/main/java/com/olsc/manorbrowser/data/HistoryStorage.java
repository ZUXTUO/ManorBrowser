/**
 * 历史记录持久化存储类
 * 负责网页访问历史的记录、查询、单条删除以及全量清理。
 * 使用异步单线程池 (ExecutorService) 保证磁盘 IO 不阻塞主线程。
 */
package com.olsc.manorbrowser.data;

import com.olsc.manorbrowser.Config;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 历史记录持久化存储类
 * 负责网页访问历史的记录、查询、单条删除以及全量清理。
 * 已升级为 SQLite 存储，支持权重计算与全文建议搜索。
 */
public class HistoryStorage {
    /** 专用于处理历史记录磁盘操作的后台线程池 */
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * 历史记录项数据模型
     */
    public static class HistoryItem {
        public long id;
        public String title;
        public String url;
        public String content;
        public long timestamp;
        public int visitCount;
        public String category;

        public HistoryItem(String title, String url, long timestamp) {
            this.title = title;
            this.url = url;
            this.timestamp = timestamp;
            this.visitCount = 1;
        }

        public HistoryItem(long id, String title, String url, String content, long timestamp, int visitCount, String category) {
            this.id = id;
            this.title = title;
            this.url = url;
            this.content = content;
            this.timestamp = timestamp;
            this.visitCount = visitCount;
            this.category = category;
        }
    }

    /**
     * 添加一条历史记录（异步执行）
     */
    public static void addHistory(Context context, String title, String url) {
        addHistory(context, title, url, null);
    }

    /**
     * 添加或更新一条历史记录，支持录入网页摘要内容
     */
    public static void addHistory(Context context, String title, String url, String contentSnippet) {
        if (url == null || url.isEmpty() || url.equals(Config.URL_BLANK) || url.equals("about:blank")) return;
        
        executor.execute(() -> {
            HistoryDatabaseHelper dbHelper = new HistoryDatabaseHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            
            // 检查是否已存在该 URL
            long now = System.currentTimeMillis();
            Cursor cursor = db.query(HistoryDatabaseHelper.TABLE_HISTORY, 
                    new String[]{HistoryDatabaseHelper.COLUMN_VISIT_COUNT, HistoryDatabaseHelper.COLUMN_ID}, 
                    HistoryDatabaseHelper.COLUMN_URL + " = ?", 
                    new String[]{url}, null, null, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                // 更新已有的记录：访问次数 +1，更新时间
                int count = cursor.getInt(0);
                long id = cursor.getLong(1);
                ContentValues values = new ContentValues();
                values.put(HistoryDatabaseHelper.COLUMN_VISIT_COUNT, count + 1);
                values.put(HistoryDatabaseHelper.COLUMN_LAST_VISIT, now);
                if (title != null && !title.isEmpty()) values.put(HistoryDatabaseHelper.COLUMN_TITLE, title);
                if (contentSnippet != null) values.put(HistoryDatabaseHelper.COLUMN_CONTENT, contentSnippet);
                
                db.update(HistoryDatabaseHelper.TABLE_HISTORY, values, 
                        HistoryDatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
                cursor.close();
            } else {
                // 插入新记录
                ContentValues values = new ContentValues();
                values.put(HistoryDatabaseHelper.COLUMN_URL, url);
                values.put(HistoryDatabaseHelper.COLUMN_TITLE, title);
                values.put(HistoryDatabaseHelper.COLUMN_CONTENT, contentSnippet != null ? contentSnippet : "");
                values.put(HistoryDatabaseHelper.COLUMN_LAST_VISIT, now);
                values.put(HistoryDatabaseHelper.COLUMN_VISIT_COUNT, 1);
                db.insert(HistoryDatabaseHelper.TABLE_HISTORY, null, values);
                if (cursor != null) cursor.close();
            }
            db.close();
        });
    }

    /**
     * 仅更新已有历史记录的内容摘要，而不增加访问权重。
     */
    public static void updateHistoryContent(Context context, String url, String contentSnippet) {
        if (url == null || url.isEmpty() || contentSnippet == null) return;
        executor.execute(() -> {
            HistoryDatabaseHelper dbHelper = new HistoryDatabaseHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(HistoryDatabaseHelper.COLUMN_CONTENT, contentSnippet);
            db.update(HistoryDatabaseHelper.TABLE_HISTORY, values, 
                    HistoryDatabaseHelper.COLUMN_URL + " = ?", new String[]{url});
            db.close();
        });
    }

    /**
     * 同步加载所有历史记录
     */
    public static List<HistoryItem> loadHistory(Context context) {
        List<HistoryItem> history = new ArrayList<>();
        HistoryDatabaseHelper dbHelper = new HistoryDatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        Cursor cursor = db.query(HistoryDatabaseHelper.TABLE_HISTORY, null, null, null, null, null, 
                HistoryDatabaseHelper.COLUMN_LAST_VISIT + " DESC");
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                history.add(new HistoryItem(
                        cursor.getLong(cursor.getColumnIndexOrThrow(HistoryDatabaseHelper.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(HistoryDatabaseHelper.COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(HistoryDatabaseHelper.COLUMN_URL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(HistoryDatabaseHelper.COLUMN_CONTENT)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(HistoryDatabaseHelper.COLUMN_LAST_VISIT)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(HistoryDatabaseHelper.COLUMN_VISIT_COUNT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(HistoryDatabaseHelper.COLUMN_CATEGORY))
                ));
            }
            cursor.close();
        }
        db.close();
        return history;
    }

    /**
     * 获取搜索建议（基于关键词匹配和权重排序）
     */
    public static List<HistoryItem> getRecommendations(Context context, String query) {
        List<HistoryItem> suggestions = new ArrayList<>();
        HistoryDatabaseHelper dbHelper = new HistoryDatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String selection = null;
        String[] selectionArgs = null;
        
        if (query != null && !query.isEmpty()) {
            selection = HistoryDatabaseHelper.COLUMN_TITLE + " LIKE ? OR " + HistoryDatabaseHelper.COLUMN_URL + " LIKE ?";
            selectionArgs = new String[]{"%" + query + "%", "%" + query + "%"};
        }
        
        // 权重排序核心：访问次数越多越靠前，相同次数按时间排序
        String orderBy = HistoryDatabaseHelper.COLUMN_VISIT_COUNT + " DESC, " + HistoryDatabaseHelper.COLUMN_LAST_VISIT + " DESC";
        
        Cursor cursor = db.query(HistoryDatabaseHelper.TABLE_HISTORY, null, selection, selectionArgs, null, null, orderBy, "20");
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                suggestions.add(new HistoryItem(
                        cursor.getLong(cursor.getColumnIndexOrThrow(HistoryDatabaseHelper.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(HistoryDatabaseHelper.COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(HistoryDatabaseHelper.COLUMN_URL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(HistoryDatabaseHelper.COLUMN_CONTENT)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(HistoryDatabaseHelper.COLUMN_LAST_VISIT)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(HistoryDatabaseHelper.COLUMN_VISIT_COUNT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(HistoryDatabaseHelper.COLUMN_CATEGORY))
                ));
            }
            cursor.close();
        }
        db.close();
        return suggestions;
    }

    /**
     * 清空所有历史记录
     */
    public static void clearHistory(Context context) {
        executor.execute(() -> {
            HistoryDatabaseHelper dbHelper = new HistoryDatabaseHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.delete(HistoryDatabaseHelper.TABLE_HISTORY, null, null);
            db.close();
        });
    }

    /**
     * 闲时整理历史记录（异步执行）
     * 职责：
     * 1. 清理超过 30 天未访问的低权重历史。
     * 2. 对内容进行简单的分类处理（此处为预留逻辑，可后续接入 AI 标签）。
     * 3. 重新整理数据库索引以优化查询性能。
     */
    public static void runIdleOrganization(Context context) {
        executor.execute(() -> {
            HistoryDatabaseHelper dbHelper = new HistoryDatabaseHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            
            // 策略 1: 清理超过 30 天未访问且访问次数低于 3 次的记录
            long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
            db.delete(HistoryDatabaseHelper.TABLE_HISTORY, 
                    HistoryDatabaseHelper.COLUMN_LAST_VISIT + " < ? AND " + HistoryDatabaseHelper.COLUMN_VISIT_COUNT + " < ?", 
                    new String[]{String.valueOf(thirtyDaysAgo), "3"});
            
            // 策略 2: 保持库内总数不超过 2000 条（保留高权重及新鲜记录）
            db.execSQL("DELETE FROM " + HistoryDatabaseHelper.TABLE_HISTORY + " WHERE " + HistoryDatabaseHelper.COLUMN_ID + " NOT IN (" +
                    "SELECT " + HistoryDatabaseHelper.COLUMN_ID + " FROM " + HistoryDatabaseHelper.TABLE_HISTORY + 
                    " ORDER BY " + HistoryDatabaseHelper.COLUMN_VISIT_COUNT + " DESC, " + HistoryDatabaseHelper.COLUMN_LAST_VISIT + " DESC " +
                    " LIMIT 2000)");

            // 策略 3: 优化数据库
            db.execSQL("VACUUM");
            db.close();
        });
    }

    /**
     * 删除单条指定的历史记录
     */
    public static void deleteHistoryItem(Context context, HistoryItem itemToDelete) {
        executor.execute(() -> {
            HistoryDatabaseHelper dbHelper = new HistoryDatabaseHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.delete(HistoryDatabaseHelper.TABLE_HISTORY, 
                    HistoryDatabaseHelper.COLUMN_ID + " = ?", 
                    new String[]{String.valueOf(itemToDelete.id)});
            db.close();
        });
    }
}
