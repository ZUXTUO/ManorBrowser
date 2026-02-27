/**
 * 书签项实体类
 * 支持两种类型：普通链接 (LINK) 和 文件夹 (FOLDER)。
 * 文件夹类型包含递归的子项列表，支持 JSON 的全量转换。
 */
package com.olsc.manorbrowser.data;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class BookmarkItem {
    /** 类别枚举：链接或文件夹 */
    public enum Type { LINK, FOLDER }

    /** 标题 */
    public String title;
    /** 地址（仅对于 LINK 类型有效） */
    public String url;
    /** 唯一 ID */
    public long id;
    /** 类型 */
    public Type type;
    /** 子书签列表（仅对于 FOLDER 类型有效） */
    public List<BookmarkItem> children;
    /** 父级文件夹 ID (-1 表示根目录) */
    public long parentId;

    /**
     * 构建一个普通链接书签
     */
    public BookmarkItem(String title, String url) {
        this.title = title;
        this.url = url;
        // 使用时间戳加随机数生成尽量唯一的 ID
        this.id = System.currentTimeMillis() + (long)(Math.random() * 1000);
        this.type = Type.LINK;
        this.parentId = -1;
    }

    /**
     * 构建一个文件夹书签
     */
    public BookmarkItem(String title) {
        this.title = title;
        this.id = System.currentTimeMillis() + (long)(Math.random() * 1000);
        this.type = Type.FOLDER;
        this.children = new ArrayList<>();
        this.parentId = -1;
    }

    /**
     * 将当前书签对象（及其子项）转换为 JSON
     */
    public JSONObject toJson() throws Exception {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("title", title);
        json.put("type", type.name());
        json.put("parentId", parentId);
        
        if (type == Type.LINK) {
            json.put("url", url);
        } else {
            JSONArray arr = new JSONArray();
            if (children != null) {
                for (BookmarkItem child : children) {
                    arr.put(child.toJson());
                }
            }
            json.put("children", arr);
        }
        return json;
    }

    /**
     * 从 JSON 对象解析出书签结构（递归）
     */
    public static BookmarkItem fromJson(JSONObject json) throws Exception {
        Type type = Type.valueOf(json.getString("type"));
        BookmarkItem item;
        
        if (type == Type.LINK) {
            item = new BookmarkItem(json.getString("title"), json.getString("url"));
        } else {
            item = new BookmarkItem(json.getString("title"));
            JSONArray arr = json.optJSONArray("children");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    BookmarkItem child = fromJson(arr.getJSONObject(i));
                    child.parentId = item.id;
                    item.children.add(child);
                }
            }
        }
        
        // 恢复被构造函数随机生成的原始 ID
        item.id = json.getLong("id");
        item.parentId = json.optLong("parentId", -1);
        return item;
    }
}