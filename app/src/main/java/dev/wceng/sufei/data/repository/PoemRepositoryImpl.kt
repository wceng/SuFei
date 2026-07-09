package dev.wceng.sufei.data.repository

import dev.wceng.sufei.data.local.datastore.UserPreferencesDataSource
import dev.wceng.sufei.data.local.room.PoemDao
import dev.wceng.sufei.data.local.room.PoetDao
import dev.wceng.sufei.data.local.room.TagDao
import dev.wceng.sufei.data.local.room.TuneDao
import dev.wceng.sufei.data.local.room.entity.toPoet
import dev.wceng.sufei.data.local.room.entity.toTag
import dev.wceng.sufei.data.local.room.entity.toTune
import dev.wceng.sufei.data.model.Poem
import dev.wceng.sufei.data.model.Poet
import dev.wceng.sufei.data.model.SearchResult
import dev.wceng.sufei.data.model.Tag
import dev.wceng.sufei.data.model.Tune
import dev.wceng.sufei.data.model.UserPoem
import dev.wceng.sufei.util.ChineseConverter
import dev.wceng.sufei.util.convert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PoemRepositoryImpl @Inject constructor(
    private val poemDao: PoemDao,
    private val tagDao: TagDao,
    private val poetDao: PoetDao,
    private val tuneDao: TuneDao,
    private val userPreferencesDataSource: UserPreferencesDataSource
) : PoemRepository {

    @Volatile
    private var pendingWidgetPoemId: String? = null

    override fun setPendingWidgetPoem(poemId: String) {
        pendingWidgetPoemId = poemId
    }

    override suspend fun resolveWidgetPoemId(appWidgetId: Int): String? {
        return userPreferencesDataSource.resolveWidgetPoemId(appWidgetId, pendingWidgetPoemId)
    }

    override fun getAllUserPoems(limit: Int): Flow<List<UserPoem>> {
        return combine(
            poemDao.getAllPoems(limit),
            userPreferencesDataSource.userPreferencesFlow
        ) { entities, prefs ->
            entities.map { entity ->
                UserPoem(entity, prefs)
            }
        }.flowOn(Dispatchers.IO)
    }

    override fun getUserPoemById(id: String): Flow<UserPoem?> {
        return combine(
            poemDao.getPoemByIdFlow(id),
            userPreferencesDataSource.userPreferencesFlow
        ) { entity, prefs ->
            entity?.let {
                UserPoem(it, prefs)
            }
        }.flowOn(Dispatchers.IO)
    }


    override fun searchUserPoems(
        query: String,
        dynasty: String?,
        tag: String?,
        tune: String?,
        limit: Int
    ): Flow<List<UserPoem>> {
        val sQuery = ChineseConverter.toSimplified(query)
        val sDynasty = dynasty?.let { ChineseConverter.toSimplified(it) }
        val sTag = tag?.let { ChineseConverter.toSimplified(it) }
        val sTune = tune?.let { ChineseConverter.toSimplified(it) }

        return combine(
            poemDao.searchPoems(sQuery, sDynasty, sTag, sTune, limit),
            userPreferencesDataSource.userPreferencesFlow
        ) { entities, prefs ->
            entities.map { entity ->
                UserPoem(entity, prefs)
            }
        }.flowOn(Dispatchers.IO)
    }

    override fun searchAll(
        query: String,
        dynasty: String?,
        tag: String?,
        tune: String?,
        limit: Int
    ): Flow<SearchResult> {
        val poemsFlow = searchUserPoems(query, dynasty, tag, tune, limit)
        val poetsFlow = if (query.isNotBlank()) {
            searchPoets(query)
        } else {
            flowOf(emptyList())
        }

        return combine(poemsFlow, poetsFlow) { poems, poets ->
            SearchResult(poems = poems, poets = poets)
        }.flowOn(Dispatchers.IO)
    }

    override fun getRandomUserPoem(): Flow<UserPoem?> {
        return combine(
            poemDao.getHighQualityRandomPoems(1),
            userPreferencesDataSource.userPreferencesFlow
        ) { entities, prefs ->
            entities.firstOrNull()?.let { UserPoem(it, prefs) }
        }.flowOn(Dispatchers.IO)
    }

    override fun getRandomUserPoems(limit: Int): Flow<List<UserPoem>> {
        return combine(
            poemDao.getHighQualityRandomPoems(limit),
            userPreferencesDataSource.userPreferencesFlow
        ) { entities, prefs ->
            entities.map { UserPoem(it, prefs) }
        }.flowOn(Dispatchers.IO)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getFavoriteUserPoems(): Flow<List<UserPoem>> {
        return userPreferencesDataSource.userPreferencesFlow
            .flatMapLatest { prefs ->
                if (prefs.favorites.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    combine(
                        poemDao.getPoemsByIds(prefs.favorites.keys),
                        userPreferencesDataSource.userPreferencesFlow
                    ) { entities, currentPrefs ->
                        entities.map { entity ->
                            UserPoem(entity, currentPrefs)
                        }
                    }
                }
            }
            .flowOn(Dispatchers.IO)
    }

    override fun getAllTags(): Flow<List<Tag>> {
        return combine(
            tagDao.getAllTags(),
            userPreferencesDataSource.userPreferencesFlow
        ) { entities, prefs ->
            entities.map { Tag(ChineseConverter.convert(it.name, prefs.chineseVariant)) }
        }.flowOn(Dispatchers.IO)
    }

    override fun getAllTunes(): Flow<List<Tune>> =
        combine(
            tuneDao.getAllTunes(),
            userPreferencesDataSource.userPreferencesFlow
        ) { entities, prefs ->
            entities.map { Tune(ChineseConverter.convert(it.name, prefs.chineseVariant)) }
        }.flowOn(Dispatchers.IO)

    override fun searchPoets(query: String): Flow<List<Poet>> {
        val sQuery = ChineseConverter.toSimplified(query)
        return combine(
            poetDao.searchPoetsByName(sQuery),
            userPreferencesDataSource.userPreferencesFlow
        ) { entities, prefs ->
            entities.map { it.toPoet().convert(prefs.chineseVariant) }
        }.flowOn(Dispatchers.IO)
    }

    override fun getPoetById(id: String): Flow<Poet?> {
        return combine(
            poetDao.getPoetByIdFlow(id),
            userPreferencesDataSource.userPreferencesFlow
        ) { entity, prefs ->
            entity?.toPoet()?.convert(prefs.chineseVariant)
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getPoetIdByName(name: String): String? {
        val sName = ChineseConverter.toSimplified(name)
        return poetDao.getPoetIdByName(sName)
    }

    override fun getPoemsByPoet(authorName: String): Flow<List<UserPoem>> {
        val sAuthor = ChineseConverter.toSimplified(authorName)
        return combine(
            poemDao.getPoemsByAuthor(sAuthor, limit = 20),
            userPreferencesDataSource.userPreferencesFlow
        ) { entities, prefs ->
            entities.map { entity ->
                UserPoem(entity, prefs)
            }
        }.flowOn(Dispatchers.IO)
    }
}
