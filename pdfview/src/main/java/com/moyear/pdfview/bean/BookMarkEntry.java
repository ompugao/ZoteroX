package com.moyear.pdfview.bean;

/**
 * @version V1.0
 * @Author : Moyear
 * @Time : 2023/6/13 10:46
 * @Description :
 */
public class BookMarkEntry {

    public int page;
    public String entryName;
    public BookMarkEntry(String entryName, int page) {
        this.entryName = entryName;
        this.page = page;
    }

//    @Override
//    public int getLayoutId() {
//        return R.layout.bookmark_item;
//    }

    @Override
    public String toString() {
        return "BookMarkEntry{" +
                "page=" + page +
                ", entryName='" + entryName + '\'' +
                '}';
    }

}
