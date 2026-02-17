/**
 * 标签页信息的实体类，存储单个标签页的状态信息。
 */
package com.olsc.manorbrowser.data;
import org.mozilla.geckoview.GeckoSession;
public class TabInfo {
    public GeckoSession session;
    public String title;
    public String url;
    public android.graphics.Bitmap thumbnail;
    public long id;
    public int scrollY = 0;
    public boolean canGoBack = false;
    public TabInfo(GeckoSession session) {
        this.id = System.currentTimeMillis();
        this.session = session;
        this.title = null;
        this.url = "about:blank";
    }
}
