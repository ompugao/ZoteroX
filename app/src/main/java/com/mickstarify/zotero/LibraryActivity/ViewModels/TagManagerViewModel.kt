package com.mickstarify.zotero.LibraryActivity.ViewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.mickstarify.zotero.MyLog
import com.mickstarify.zotero.TagStyler
import com.mickstarify.zotero.ZoteroStorage.Database.ItemTag
import com.mickstarify.zotero.adapters.TagWrapper
import java.text.Collator
import java.util.*

class TagManagerViewModel(application: Application) : AndroidViewModel(application) {

    private var myStyleTags: MutableList<TagWrapper>? = null

    /**
     * 用于监听标签样式变化的
     */
    var styleItems = MutableLiveData<List<TagWrapper>> ()

    /**
     * 用于显示列表的标签tag items
     */
    val tagItems = MutableLiveData<List<TagWrapper>>()

    private var tagStyler: TagStyler? = null

    private var mutableTags = MutableLiveData<List<ItemTag>>()

    fun getMutableTags(): MutableLiveData<List<ItemTag>> {
        return mutableTags
    }

    private fun getTagStyler(): TagStyler {
        if (tagStyler == null) tagStyler = TagStyler.getInstance(getApplication())
        return tagStyler!!
    }

    private fun getStyleTags(): MutableList<TagWrapper> {
        if (myStyleTags == null) {
            myStyleTags = getTagStyler().getStyleTags().map { TagWrapper(it.tag, it.color, false) }.toMutableList()
        }
        return myStyleTags!!
    }

    /**
     * 将标签列表按照指定的标签样式重新排序
     */
    fun sortTags(targetTags: List<TagWrapper>, referenceStyledTags: List<TagWrapper>) : List<TagWrapper> {
        val importantTagList = referenceStyledTags.map { it.tag }

        // 没有分配颜色的标签按照中文拼音排序
        val noMarkedTags = targetTags.filter {
            return@filter !importantTagList.contains(it.tag)
        }.sortedWith { o1, o2 ->
            Collator.getInstance(Locale.CHINA).compare(o1?.tag, o2?.tag)
        }.map {
            TagWrapper(it.tag, "", false)
        }

        val rearrangedTags: MutableList<TagWrapper> = mutableListOf()
        // 添加前面分配了颜色和排序的标签
        rearrangedTags.addAll(referenceStyledTags)
        rearrangedTags.addAll(noMarkedTags)

        return rearrangedTags
    }

    /**
     * 解析标签样式配置文件，获取重要的标签列表
     */
    fun sortTags(tags: List<TagWrapper>) : List<TagWrapper> {
        // 解析标签样式配置文件，获取重要的标签列表
        val referenceStyledTags = getStyleTags()
        return sortTags(tags, referenceStyledTags)
    }

    fun convertTag(tag: ItemTag): TagWrapper {
        return TagWrapper(tag.tag, getTagColor(tag.tag), false)
    }

    fun getTagColor(tag: String): String {
        getTagStyler().getStyleTags().forEach {
            if (it.tag == tag) {
                return it.color
            }
        }
        return "#4B5162"
    }

    fun parseColor(intColor: Int): String {
        return String.format("#%06X", 0xFFFFFF and intColor)
    }

    fun modifyTagColor(tag: String, hexColor: String) {
        var flagUpdateConfigFile = false

        var isTagAlreadyStyled = false

        for (styledTagEntry in getStyleTags()) {
            if (styledTagEntry.tag == tag) {

                styledTagEntry.color = hexColor
                flagUpdateConfigFile = true
                isTagAlreadyStyled = true
            }
        }

        if (!isTagAlreadyStyled && tag.isNotEmpty()) {
            val newTag = TagWrapper(tag, hexColor, false)
            getStyleTags().add(newTag)

            flagUpdateConfigFile = true
        }

        if (flagUpdateConfigFile) {
            styleItems.value = getStyleTags()
        }
    }

    fun isTagStyled(tag: String): Boolean {
        getTagStyler().getStyleTags().forEach {
            if (it.tag == tag) return true
        }
        return false
    }

    fun removeTagStyle(tag: String) {
        var index = -1

        getStyleTags().mapIndexed { i, styledTagEntry ->
            if (styledTagEntry.tag == tag) {
                index = i
                return@mapIndexed
            }
        }

        MyLog.e("ZoteroDebug", "removed tag is $index")

        if (index > 0) {
            getStyleTags().removeAt(index)
            styleItems.value = getStyleTags()
        }
    }

    /**
     * 将标签样式列表，写入配置文件
     */
    private fun writeTagConfig(tags: List<TagWrapper>) {
        tags.mapIndexed { index, it ->
            TagStyler.StyledTagEntry(it.tag, it.color, index)
        }.let {
            getTagStyler().writeTagConfig(it)
        }
    }

    fun updateMyTagStyle(styleTags: List<TagWrapper>) {
        tagItems.value = sortTags(tagItems.value!!, styleTags)

        // 将变化后的标签样式列表，写入配置文件
        writeTagConfig(styleTags)
    }

    fun filterWithTag(tag: String) {
        val old = tagItems.value

        if (tag.isNullOrEmpty()) {
            tagItems.value = old?.map { TagWrapper(it.tag, it.color, false) }
            return
        }

        tagItems.value = old?.map {
            TagWrapper(it.tag, it.color, it.tag == tag)
        }

    }

    var itemTags: List<ItemTag>? = null

    fun setTagCollection(uniqueItemTags: List<ItemTag>) {
        mutableTags.value = uniqueItemTags

        this.itemTags = uniqueItemTags
        itemTags
            ?.map { convertTag(it) }
            ?.let {
                tagItems.postValue(sortTags(it))
            }
    }

    fun fetchTags() {
        itemTags
            ?.map { convertTag(it) }
            ?.let {
                tagItems.value = sortTags(it)
            }
    }

}