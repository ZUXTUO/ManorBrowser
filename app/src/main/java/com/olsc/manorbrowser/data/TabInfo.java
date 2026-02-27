/**
 * 标签页信息实体类
 * 承载单个标签页的核心数据，包括 GeckoSession 实例、页面基本信息以及 UI 状态（如缩略图、滚动位置）。
 */
package com.olsc.manorbrowser.data;

import org.mozilla.geckoview.GeckoSession;

public class TabInfo {
    /** 绑定的 GeckoView 会话实例 */
    public GeckoSession session;
    /** 页面标题 */
    public String title;
    /** 页面 URL */
    public String url;
    /** 页面预览图（缩略图） */
    public android.graphics.Bitmap thumbnail;
    /** 唯一标识符 */
    public long id;
    /** 垂直滚动位置记录 */
    public int scrollY = 0;
    /** 后退状态缓存 */
    public boolean canGoBack = false;

    public TabInfo(GeckoSession session) {
        this.id = System.currentTimeMillis();
        this.session = session;
        this.title = null;
        this.url = "about:blank";
    }
}
