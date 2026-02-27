/**
 * 书签持久化存储类
 * 负责书签的加载、保存、添加、删除、移动以及目录结构的管理。
 * 数据以 JSON 格式存储在应用的私有文件目录中。
 */
package com.olsc.manorbrowser.data;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class BookmarkStorage {
    /** 书签存储的文件名 */
    private static final String FILE_NAME = "bookmarks.json";

    /**
     * 从本地文件加载所有书签
     */
    public static List<BookmarkItem> loadBookmarks(Context context) {
        List<BookmarkItem> bookmarks = new ArrayList<>();
        try {
            File file = new File(context.getFilesDir(), FILE_NAME);
            if (!file.exists()) return bookmarks;
            
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            
            JSONArray arr = new JSONArray(new String(data));
            for (int i = 0; i < arr.length(); i++) {
                bookmarks.add(BookmarkItem.fromJson(arr.getJSONObject(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bookmarks;
    }

    /**
     * 将书签列表全量保存到本地文件
     */
    public static void saveBookmarks(Context context, List<BookmarkItem> bookmarks) {
        try {
            JSONArray arr = new JSONArray();
            for (BookmarkItem item : bookmarks) {
                arr.put(item.toJson());
            }
            File file = new File(context.getFilesDir(), FILE_NAME);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(arr.toString().getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加书签到根目录
     */
    public static void addBookmark(Context context, BookmarkItem item) {
        List<BookmarkItem> bookmarks = loadBookmarks(context);
        bookmarks.add(item);
        saveBookmarks(context, bookmarks);
    }

    /**
     * 根据 ID 删除书签（含递归删除）
     */
    public static void removeBookmark(Context context, long id) {
        List<BookmarkItem> bookmarks = loadBookmarks(context);
        removeRecursive(bookmarks, id);
        saveBookmarks(context, bookmarks);
    }

    /**
     * 递归删除逻辑
     */
    private static boolean removeRecursive(List<BookmarkItem> items, long id) {
        for (int i = 0; i < items.size(); i++) {
            BookmarkItem item = items.get(i);
            if (item.id == id) {
                items.remove(i);
                return true;
            }
            if (item.children != null && !item.children.isEmpty()) {
                if (removeRecursive(item.children, id)) return true;
            }
        }
        return false;
    }

    /**
     * 将书签添加至指定文件夹
     */
    public static void addBookmarkToFolder(Context context, BookmarkItem item, long folderId) {
        List<BookmarkItem> bookmarks = loadBookmarks(context);
        if (folderId == -1) {
            bookmarks.add(item);
        } else {
            addToFolderRecursive(bookmarks, item, folderId);
        }
        saveBookmarks(context, bookmarks);
    }

    /**
     * 递归寻找并添加至目标文件夹
     */
    private static void addToFolderRecursive(List<BookmarkItem> items, BookmarkItem itemToAdd, long folderId) {
        for (BookmarkItem folder : items) {
            if (folder.id == folderId) {
                if (folder.children == null) folder.children = new ArrayList<>();
                itemToAdd.parentId = folderId;
                folder.children.add(itemToAdd);
                return;
            }
            if (folder.children != null && !folder.children.isEmpty()) {
                addToFolderRecursive(folder.children, itemToAdd, folderId);
            }
        }
    }

    /**
     * 获取所有可用的文件夹列表，用于移动书签时的目标选择
     */
    public static List<BookmarkItem> getAllFolders(Context context) {
        List<BookmarkItem> allBookmarks = loadBookmarks(context);
        List<BookmarkItem> folders = new ArrayList<>();
        collectFolders(allBookmarks, folders);
        return folders;
    }

    /**
     * 递归搜集所有文件夹类型的项
     */
    private static void collectFolders(List<BookmarkItem> items, List<BookmarkItem> result) {
        for (BookmarkItem item : items) {
            if (item.type == BookmarkItem.Type.FOLDER) {
                result.add(item);
                if (item.children != null && !item.children.isEmpty()) {
                    collectFolders(item.children, result);
                }
            }
        }
    }

    /**
     * 移动书签逻辑：先递归删除原位数据，再递归插入目标位置
     */
    public static void moveBookmark(Context context, long bookmarkId, long targetFolderId) {
        List<BookmarkItem> bookmarks = loadBookmarks(context);
        BookmarkItem itemToMove = findAndRemoveRecursive(bookmarks, bookmarkId);
        if (itemToMove != null) {
            if (targetFolderId == -1) {
                itemToMove.parentId = -1;
                bookmarks.add(itemToMove);
            } else {
                addToFolderRecursive(bookmarks, itemToMove, targetFolderId);
            }
            saveBookmarks(context, bookmarks);
        }
    }

    /**
     * 移动逻辑中使用的：查找并移除
     */
    private static BookmarkItem findAndRemoveRecursive(List<BookmarkItem> items, long id) {
        for (int i = 0; i < items.size(); i++) {
            BookmarkItem item = items.get(i);
            if (item.id == id) {
                return items.remove(i);
            }
            if (item.children != null && !item.children.isEmpty()) {
                BookmarkItem found = findAndRemoveRecursive(item.children, id);
                if (found != null) return found;
            }
        }
        return null;
    }
}
