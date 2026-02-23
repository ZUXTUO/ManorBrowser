package com.olsc.manorbrowser.data;

import java.util.List;

public class CookieDisplayItem {
    public enum Type { GROUP, COOKIE }
    
    public Type type;
    public String domain;
    public CookieItem cookie;
    public int count; // For groups
    
    public CookieDisplayItem(String domain, int count) {
        this.type = Type.GROUP;
        this.domain = domain;
        this.count = count;
    }
    
    public CookieDisplayItem(CookieItem cookie) {
        this.type = Type.COOKIE;
        this.cookie = cookie;
    }
}
