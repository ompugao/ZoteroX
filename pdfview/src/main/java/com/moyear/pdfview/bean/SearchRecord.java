package com.moyear.pdfview.bean;

/**
 * @version V1.0
 * @Author : Moyear
 * @Time : 2023/6/13 10:39
 * @Description : sou
 */
public class SearchRecord {

    public final int pageIdx;
    public final int findStart;
    public Object data;

    public SearchRecord(int pageIdx, int findStart) {
        this.pageIdx = pageIdx;
        this.findStart = findStart;
    }

}
