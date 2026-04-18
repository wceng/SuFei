package dev.wceng.sufei.data.repository

import dev.wceng.sufei.data.local.datastore.UserPreferencesDataSource
import dev.wceng.sufei.data.local.room.PoemDao
import dev.wceng.sufei.data.local.room.PoetDao
import dev.wceng.sufei.data.local.room.TagDao
import dev.wceng.sufei.data.local.room.TuneDao
import dev.wceng.sufei.data.local.room.entity.toPoem
import dev.wceng.sufei.data.local.room.entity.toPoet
import dev.wceng.sufei.data.local.room.entity.toTag
import dev.wceng.sufei.data.local.room.entity.toTune
import dev.wceng.sufei.data.model.Poet
import dev.wceng.sufei.data.model.SearchResult
import dev.wceng.sufei.data.model.Tag
import dev.wceng.sufei.data.model.Tune
import dev.wceng.sufei.data.model.UserPoem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
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

    override fun getAllUserPoems(limit: Int): Flow<List<UserPoem>> {
        return combine(
            poemDao.getAllPoems(limit),
            userPreferencesDataSource.userPreferencesFlow
        ) { entities, prefs ->
            entities.map { entity ->
                UserPoem(poem = entity.toPoem(), userPreferences = prefs)
            }
        }.flowOn(Dispatchers.IO)
    }

    override fun getUserPoemById(id: String): Flow<UserPoem?> {
        return combine(
            poemDao.getPoemByIdFlow(id),
            userPreferencesDataSource.userPreferencesFlow
        ) { entity, prefs ->
            entity?.let {
                UserPoem(poem = it.toPoem(), userPreferences = prefs)
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
        return combine(
            poemDao.searchPoems(query, dynasty, tag, tune, limit),
            userPreferencesDataSource.userPreferencesFlow
        ) { entities, prefs ->
            entities.map { entity ->
                UserPoem(poem = entity.toPoem(), userPreferences = prefs)
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
            poemDao.getRandomPoem(),
            userPreferencesDataSource.userPreferencesFlow
        ) { entity, prefs ->
            entity?.let {
                UserPoem(poem = it.toPoem(), userPreferences = prefs)
            }
        }.flowOn(Dispatchers.IO)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getFavoriteUserPoems(): Flow<List<UserPoem>> {
        return userPreferencesDataSource.userPreferencesFlow
            .flatMapLatest { prefs ->
                if (prefs.favoritePoemIds.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    poemDao.getPoemsByIds(prefs.favoritePoemIds).map { entities ->
                        entities.map { entity ->
                            UserPoem(poem = entity.toPoem(), userPreferences = prefs)
                        }
                    }
                }
            }
            .flowOn(Dispatchers.IO)
    }

    override fun getAllTags(): Flow<List<Tag>> {
        return tagDao.getAllTags().map { entities ->
            entities.map { it.toTag() }
        }.flowOn(Dispatchers.IO)
    }

    override fun getAllTunes(): Flow<List<Tune>> =
        tuneDao.getAllTunes().map { entities ->
            entities.map { it.toTune() }
        }.flowOn(Dispatchers.IO)

    override fun searchPoets(query: String): Flow<List<Poet>> {
        return poetDao.searchPoetsByName(query).map { entities ->
            entities.map { it.toPoet() }
        }.flowOn(Dispatchers.IO)
    }

    override fun getTopPoets(limit: Int): Flow<List<Poet>> {
        return poetDao.getAllPoets().map { entities ->
            entities.take(limit).map { it.toPoet() }
        }.flowOn(Dispatchers.IO)
    }

    override fun getAllPoets(): Flow<List<Poet>> {
        return poetDao.getAllPoets().map { entities ->
            entities.map { it.toPoet() }
        }.flowOn(Dispatchers.IO)
    }

    override fun getPoetById(id: String): Flow<Poet?> {
        return poetDao.getPoetByIdFlow(id).map { it?.toPoet() }
            .flowOn(Dispatchers.IO)
    }

    override fun getPoemsByPoet(authorName: String): Flow<List<UserPoem>> {
        return combine(
            poemDao.getPoemsByAuthor(authorName, limit = 20),
            userPreferencesDataSource.userPreferencesFlow
        ) { entities, prefs ->
            entities.map { entity ->
                UserPoem(poem = entity.toPoem(), userPreferences = prefs)
            }
        }.flowOn(Dispatchers.IO)
    }
}
