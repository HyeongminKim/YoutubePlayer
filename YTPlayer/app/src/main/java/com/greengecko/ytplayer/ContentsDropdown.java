package com.greengecko.ytplayer;

public class ContentsDropdown {
    private String name;
    private String author;
    private int resId;

    public ContentsDropdown(String name, String author, int resId) {
        this.name = name;
        this.author = author;
        this.resId = resId;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public int getResId() {
        return resId;
    }
}
