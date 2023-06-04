package com.mickstarify.zotero

import android.content.Context
import com.blankj.utilcode.util.FileIOUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.mickstarify.zotero.ZoteroStorage.AttachmentStorageManager
import com.mickstarify.zotero.ZoteroStorage.Database.ItemTag
import com.mickstarify.zotero.ZoteroStorage.ZoteroUtils
import java.io.File
import java.lang.reflect.Type
import java.text.Collator
import java.util.*

class TagStyler {

    private var tagStyles: List<StyledTagEntry>? = null

    private var workSpace: File

    private constructor(context: Context) {
        workSpace = context.getExternalFilesDir(AttachmentStorageManager.ZOTERO_WORKSPACE)!!
    }

    companion object {
        private var INSTANCE: TagStyler? = null

        fun getInstance(context: Context): TagStyler {
            if (INSTANCE == null) INSTANCE = TagStyler(context)
            return INSTANCE!!
        }
    }

    data class StyledTagEntry(var tag: String = "",
                              var color: String = "",
                              var order: Int = -1) {

    }

    /**
     * 解析标签配置文件，获取
     */
    fun parseTagConfig(): List<StyledTagEntry> {
        val tagConfigFile = getTagConfigFile()

        var styledTags: List<StyledTagEntry> = listOf()
        if (!tagConfigFile.exists()) {

            // 如果配置文件不存在，则创建新的配置文件并将内置的规则写入
            val importantTag = ZoteroUtils.getImportantTag()

            styledTags = importantTag.mapIndexed { index, it ->
                StyledTagEntry(it, ZoteroUtils.getTagColor(it), index)
            }

            writeTagConfig(tagConfigFile, styledTags)
        } else {
            val type: Type = object : TypeToken<List<StyledTagEntry>>() {}.getType()

            val jsonString = FileIOUtils.readFile2String(tagConfigFile)
            jsonString?.let {
                styledTags = Gson().fromJson(it, type)
            }
        }
        return styledTags
    }

    fun getTagConfigFile(): File {
        val tagConfigDir = File(workSpace.path + File.separator + "config")
        val tagConfigFile = File(tagConfigDir, "tagConfig.json")

        if (!tagConfigDir.exists() || !tagConfigFile.exists()) {
            tagConfigDir.mkdirs()
//            tagConfigDir.createNewFile()

            // 如果配置文件不存在，则创建新的配置文件并将内置的规则写入
            val importantTag = ZoteroUtils.getImportantTag()

            val styledTags = importantTag.mapIndexed { index, it ->
                StyledTagEntry(it, ZoteroUtils.getTagColor(it), index)
            }

            writeTagConfig(tagConfigFile, styledTags)
        }

        return tagConfigFile
    }

    fun writeTagConfig(tagConfigList: List<StyledTagEntry>) {
        writeTagConfig(getTagConfigFile(), tagConfigList)
    }

    private fun writeTagConfig(tagConfigFile: File, tagConfigList: List<StyledTagEntry>) {
        // 根据列表的顺序为每个对象的order属性赋值，保证写入后配置文件的顺序是正常的
        val tagsList = tagConfigList.mapIndexed { index, tag ->
            StyledTagEntry(tag.tag, tag.color, index)
        }

        val json = GsonBuilder().setPrettyPrinting().create().toJson(tagsList)
        FileIOUtils.writeFileFromString(tagConfigFile, json)
        MyLog.d("Zotero", "write tag config: $json to file: ${tagConfigFile.path}")

        // 更新列表配置文件后
        tagStyles = tagsList
    }

    /**
     * 对标签列表进行重新整理
     */
    fun rearrangeTags(tags: List<ItemTag>, referenceStyledTags: List<StyledTagEntry>) : List<StyledTagEntry> {
        // 解析标签样式配置文件，获取重要的标签列表
//        val importantTags = parseTagConfig()
        val importantTagList = referenceStyledTags.map { it.tag }

        // 没有分配颜色的标签按照中文拼音排序
        val noMarkedTags = tags.filter {
            return@filter !importantTagList.contains(it.tag)
        }.sortedWith { o1, o2 ->
            Collator.getInstance(Locale.CHINA).compare(o1?.tag, o2?.tag)
        }.map { StyledTagEntry(it.tag) }

        val rearrangedTags: MutableList<StyledTagEntry> = mutableListOf()
        // 添加前面分配了颜色和排序的标签
        rearrangedTags.addAll(referenceStyledTags)
        rearrangedTags.addAll(noMarkedTags)
        return rearrangedTags
    }

    fun isTagStyled(tag: String): Boolean {
        getStyleTags().forEach {
            if (it.tag == tag) return true
        }
        return false
    }

    fun getTagColor(tag: String): String {
        getStyleTags().forEach {
            if (it.tag == tag) {
                return it.color
            }
        }
        return "#4B5162"
    }

    fun getStyleTags(): List<StyledTagEntry> {
        if (tagStyles == null) tagStyles = parseTagConfig()
        return tagStyles!!
    }


    fun indexOf(tag: String): Int {
        getStyleTags().forEachIndexed() {
            i, tagEntry ->
            if (tagEntry.tag == tag) {
                return i
            }
        }

        return -1
    }

}