package com.olsc.manorbrowser.data;

public class CookieItem {
    public String name;
    public String value;
    public String domain;
    public String path;
    public boolean isSecure;
    public boolean isHttpOnly;
    public long expires;

    public CookieItem(String name, String value, String domain, String path, boolean isHttpOnly) {
        this.name = name;
        this.value = value;
        this.domain = domain;
        this.path = path;
        this.isHttpOnly = isHttpOnly;
    }
}
