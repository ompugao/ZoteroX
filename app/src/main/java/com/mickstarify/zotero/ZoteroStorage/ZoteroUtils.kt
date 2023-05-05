package com.mickstarify.zotero.ZoteroStorage

/**
 * @version V1.0
 * @Author : Moyear
 * @Time : 2023/1/29 16:39
 * @Description : Zotero相关工具类
 */
object ZoteroUtils {

    /**
     * 获取item类型String
     */
    fun getItemTypeHumanReadableString(itemType: String): CharSequence? {
        return when (itemType) {
            "journalArticle" -> "期刊论文"
            "conferencePaper" -> "会议论文"
            "thesis" -> "学位论文"
            "note" -> "笔记"
            "book" -> "书籍"
            "bookSection" -> "图书章节"
            "magazineArticle" -> "杂志文章"
            "newspaperArticle" -> "报纸文章"
            "letter" -> "信件"
            "manuscript" -> "手稿"
            "interview" -> "采访文章"
            "film" -> "电影"
            "artwork" -> "艺术品"
            "webpage" -> "网页"
            "attachment" -> "附件"
            "report" -> "报告"
            "bill" -> "Bill"
            "case" -> "司法案例"
            "hearing" -> "听证会"
            "patent" -> "专利"
            "statute" -> "法规"
            "email" -> "E-mail"
            "map" -> "地图"
            "blogPost" -> "博客帖子"
            "instantMessage" -> "即时讯息"
            "forumPost" -> "论坛帖子"
            "audioRecording" -> "音频"
            "presentation" -> "演示文档"
            "videoRecording" -> "视频"
            "tvBroadcast" -> "电视广播"
            "radioBroadcast" -> "电台广播"
            "podcast" -> "博客"
            "computerProgram" -> "软件"
            "document" -> "文档"
            "encyclopediaArticle" -> "预印本"
            "dictionaryEntry" -> "词条"
            else -> itemType
        }
    }

    /**
     * Used to convert the key of an item in the database to the actual meaning it represents
     */
    fun getItemKeyNameHumanReadableString(keyName: String): String {
        return when(keyName) {
            "date" -> "日期"
            "language" -> "语言"
            "dateAdded" -> "添加日期"
            "pages" -> "页码"
            "accessDate" -> "访问日期"
            "publisher" -> "出版社"
            "abstractNote" -> "摘要"
            "dateModified" -> "修改日期"
            "libraryCatalog" -> "馆藏目录"
            "bookTitle" -> "图书标题"
            "university" -> "大学"
            "thesisType" -> "论文类型"
            "extra" -> "其他"
            "author" -> "作者"
            "contributor" -> "贡献者"
            else -> keyName
        }
    }

    fun isImportantTag(tag: String): Boolean {
        val importantTags = setOf("在读", "已读", "待读", "综述",
            "参考", "实验方法",
            "★★★★★", "★★★★", "★★★", )
        if (importantTags.contains(tag)) return true
        return false
    }

    fun getTagColor(tag: String): String {
        return when (tag) {
            "在读" -> "#576DD9"
            "已读" -> "#5FB236"
            "待读"-> "#FF8C19"
            "综述" -> "#FF6666"
            "参考" -> "#009980"
            "实验方法" -> "#A6507B"
            "★★★★★" -> "#FF6666"
            "★★★★" -> "#576DD9"
            "★★★" -> "#2EA8E5"
            else -> return "#4B5162"

        }
    }


}