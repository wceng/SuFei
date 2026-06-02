package dev.wceng.sufei.data.repository

import dev.wceng.sufei.data.model.Poet
import dev.wceng.sufei.data.model.Poem
import dev.wceng.sufei.data.model.SearchResult
import dev.wceng.sufei.data.model.Tag
import dev.wceng.sufei.data.model.Tune
import dev.wceng.sufei.data.model.UserPoem
import kotlinx.coroutines.flow.Flow

interface PoemRepository {
    /**
     * 获取所有诗词 (带实时偏好状态)
     */
    fun getAllUserPoems(limit: Int = 50): Flow<List<UserPoem>>

    /**
     * 根据 ID 获取单首诗词 (带实时偏好状态)
     */
    fun getUserPoemById(id: String): Flow<UserPoem?>

    /**
     * 搜索诗词，支持朝代和标签过滤
     */
    fun searchUserPoems(
        query: String, 
        dynasty: String? = null, 
        tag: String? = null,
        tune: String? = null,
        limit: Int = 50
    ): Flow<List<UserPoem>>

    /**
     * 混合搜索：返回诗人和诗词的流
     */
    fun searchAll(
        query: String,
        dynasty: String? = null,
        tag: String? = null,
        tune: String? = null,
        limit: Int = 50
    ): Flow<SearchResult>

    /**
     * 获取高质量随机诗词
     */
    fun getRandomUserPoem(): Flow<UserPoem?>

    /**
     * 获取一系列高质量随机诗词
     */
    fun getRandomUserPoems(limit: Int = 10): Flow<List<UserPoem>>

    /**
     * 获取收藏的诗词
     */
    fun getFavoriteUserPoems(): Flow<List<UserPoem>>

    /**
     * 获取所有标签
     */
    fun getAllTags(): Flow<List<Tag>>

    /**
     * 获取所有词牌
     */
    fun getAllTunes(): Flow<List<Tune>>

    /**
     * 搜索诗人
     */
    fun searchPoets(query: String): Flow<List<Poet>>

    /**
     * 根据诗人名查询诗人 ID
     */
    suspend fun getPoetIdByName(name: String): String?

    /**
     * 根据 ID 获取诗人详情
     */
    fun getPoetById(id: String): Flow<Poet?>

    /**
     * 获取指定诗人的所有作品
     */
    fun getPoemsByPoet(authorName: String): Flow<List<UserPoem>>

    /**
     * 设置待固定的诗词 ID（用户点击 Pin 后写入内存，不持久化）
     */
    fun setPendingWidgetPoem(poemId: String)

    /**
     * 解析 widget 实例对应的诗词 ID（Widget provideGlance 时调用）
     */
    suspend fun resolveWidgetPoemId(appWidgetId: Int): String?
}
