package com.wilco375.settingseditor.object;

/**
 * Object that holds a title and description. Used for list in MainActivity
 */
public class MainListItem {
    public String title = "";
    public String desc = "";
    public String id = "";

    public MainListItem(String id, String title, String desc) {
        this.id = id;
        this.title = title;
        this.desc = desc;
    }
}
