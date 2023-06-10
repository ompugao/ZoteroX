package com.mickstarify.zotero

import android.content.Context
import com.blankj.utilcode.util.FileIOUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.mickstarify.zotero.ZoteroStorage.AttachmentStorageManager
import com.mickstarify.zotero.ZoteroStorage.Database.Collection
import com.mickstarify.zotero.ZoteroStorage.Database.Item
import java.io.File
import java.lang.reflect.Type

/**
 * 用于保存收藏的item或着collection，以及隐藏不想要的
 */
class MyItemFilter {

    private var staredItems: MutableList<ItemEntity>? = null

    private var shieldItems: MutableList<ItemEntity>? = null

    private var workSpace: File

    private constructor(context: Context) {
        workSpace = context.getExternalFilesDir(AttachmentStorageManager.ZOTERO_WORKSPACE)!!

    }

    companion object {

        private var INSTANCE: MyItemFilter? = null

        fun get(context: Context): MyItemFilter {
            if (INSTANCE == null) {
                INSTANCE = MyItemFilter(context)
            }
            return INSTANCE!!
        }

    }

    /**
     * 解析标签配置文件，获取
     */
    fun parseStarConfig(): List<ItemEntity> {
        val configFile = getStarConfigFile()

        var list: List<ItemEntity> = listOf()
        if (configFile.exists()) {
            val type: Type = object : TypeToken<List<ItemEntity>>() {}.getType()

            val jsonString = FileIOUtils.readFile2String(configFile)
            if (!jsonString.isNullOrEmpty()) {
                list = Gson().fromJson(jsonString, type)
            }
        }
        return list
    }

    private fun getStarItems(): MutableList<ItemEntity> {
        if (staredItems == null) {
            staredItems = mutableListOf()
            staredItems!!.addAll(parseStarConfig())
        }
        return staredItems!!
    }

    fun getMyStars(): List<ItemEntity> {
        return getStarItems()
    }

    fun addToStar(item: Item) {
        val entity = ItemEntity.convert(item)
        star(entity)
    }

    fun addToStar(collection: Collection) {
        val entity = ItemEntity.convert(collection)
        star(entity)
    }

    private fun star(entity: ItemEntity) {
        if (getStarItems().contains(entity)) {
            MyLog.d("Zotero", "The item: ${entity.itemKey} has been added to stars")
            return
        }

        getStarItems().add(entity)

        MyLog.d("Zotero", "All item: ${entity.itemKey} to stars")

        writeStarConfig(getStarItems())
    }

    fun isStared(item: Item): Boolean {
        return getStarItems().contains(ItemEntity.convert(item))
    }

    fun isStared(collection: Collection): Boolean {
        return getStarItems().contains(ItemEntity.convert(collection))
    }

    fun shieldItem() {

    }

    fun getStarConfigFile(): File {
        val configDir = File(workSpace.path + File.separator + "config")
        val starConfigFile = File(configDir, "myStars.json")

        if (!configDir.exists() || !starConfigFile.exists()) {
            configDir.mkdirs()
            // 如果配置文件不存在，则创建
            if (!starConfigFile.exists()) starConfigFile.createNewFile()
        }
        return starConfigFile
    }

    fun getIgnoreConfigFile(): File {
        val configDir = File(workSpace.path + File.separator + "config")
        val ignoreConfigFile = File(configDir, "ignore.json")

        if (!configDir.exists() || !ignoreConfigFile.exists()) {
            configDir.mkdirs()
            // 如果配置文件不存在，则创建
            if (!ignoreConfigFile.exists()) ignoreConfigFile.createNewFile()
        }
        return ignoreConfigFile
    }

    private fun writeStarConfig(itemConfigList: List<ItemEntity>) {
        writeStarConfig(getStarConfigFile(), itemConfigList)
    }

    private fun writeStarConfig(starConfigFile: File, itemConfigList: List<ItemEntity>) {
        // 根据列表的顺序为每个对象的order属性赋值，保证写入后配置文件的顺序是正常的
//        val tagsList = tagConfigList.mapIndexed { index, tag ->
//            TagStyler.StyledTagEntry(tag.tag, tag.color, index)
//        }

        val json = GsonBuilder().setPrettyPrinting().create().toJson(itemConfigList)
        FileIOUtils.writeFileFromString(starConfigFile, json)
        MyLog.d("Zotero", "write my star config: $json to file: ${starConfigFile.path}")

        // 更新列表配置文件后
//        tagStyles = tagsList
    }

    fun removeStar(item: Item) {
        removeStar(ItemEntity.convert(item))
    }

    fun removeStar(collection: Collection) {
        removeStar(ItemEntity.convert(collection))
    }

    fun removeStar(entity: ItemEntity) {
        if (!getStarItems().contains(entity)) {
            MyLog.d("Zotero", "The item: ${entity.itemKey} has not been added to stars")
            return
        }

        getStarItems().remove(entity)

        MyLog.d("Zotero", "remove item: ${entity.itemKey} from stars")

        writeStarConfig(getStarItems())
    }

    data class ItemEntity(var itemKey: String, var name: String, var isCollection: Boolean) {

        companion object {
            fun convert(item: Item): ItemEntity {
                return ItemEntity(item.itemKey, item.getTitle(), false)
            }

            fun convert(collection: Collection): ItemEntity {
                return ItemEntity(collection.key, collection.name, true)
            }
        }
    }

}