package dev.wceng.sufei.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.wceng.sufei.data.local.room.PoemDao
import dev.wceng.sufei.data.local.room.PoetDao
import dev.wceng.sufei.data.local.room.TagDao
import dev.wceng.sufei.data.local.room.TuneDao
import dev.wceng.sufei.data.local.room.entity.toEntity
import dev.wceng.sufei.data.model.Poem
import dev.wceng.sufei.data.model.Poet
import dev.wceng.sufei.data.model.Tag
import dev.wceng.sufei.data.model.Tune
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImportRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val poemDao: PoemDao,
    private val tagDao: TagDao,
    private val poetDao: PoetDao,
    private val tuneDao: TuneDao
) : ImportRepository {

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    override val importState: StateFlow<ImportState> = _importState.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun startImportIfNeeded() {
        withContext(Dispatchers.IO) {
            val poemCount = poemDao.getPoemCount()
            if (poemCount > 0) {
                _importState.value = ImportState.Success
                return@withContext
            }

            try {
                _importState.value = ImportState.Importing(0f)

                // 1. 导入基础库 (标签、词牌、诗人)
                importTags()
                importTunes()
                importPoets()
                
                // 2. 导入诗词库
                importPoems()
                
                _importState.value = ImportState.Success
            } catch (e: Exception) {
                _importState.value = ImportState.Error("初始化失败: ${e.message}")
            }
        }
    }

    private suspend fun importTags() {
        try {
            val inputStream = context.assets.open("tags.jsonl")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val tagsToInsert = mutableListOf<dev.wceng.sufei.data.local.room.entity.TagEntity>()
            
            reader.useLines { lines ->
                lines.forEach { line ->
                    if (line.isNotBlank()) {
                        val tag = json.decodeFromString<Tag>(line)
                        tagsToInsert.add(tag.toEntity())
                        if (tagsToInsert.size >= 500) {
                            tagDao.insertTags(tagsToInsert.toList())
                            tagsToInsert.clear()
                        }
                    }
                }
            }
            if (tagsToInsert.isNotEmpty()) {
                tagDao.insertTags(tagsToInsert)
            }
        } catch (e: Exception) {}
    }

    private suspend fun importTunes() {
        try {
            val inputStream = context.assets.open("tunes.jsonl")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val tunesToInsert = mutableListOf<dev.wceng.sufei.data.local.room.entity.TuneEntity>()
            
            reader.useLines { lines ->
                lines.forEach { line ->
                    if (line.isNotBlank()) {
                        val tune = json.decodeFromString<Tune>(line)
                        tunesToInsert.add(tune.toEntity())
                        if (tunesToInsert.size >= 200) {
                            tuneDao.insertTunes(tunesToInsert.toList())
                            tunesToInsert.clear()
                        }
                    }
                }
            }
            if (tunesToInsert.isNotEmpty()) {
                tuneDao.insertTunes(tunesToInsert)
            }
        } catch (e: Exception) {}
    }

    private suspend fun importPoets() {
        try {
            val inputStream = context.assets.open("poets.jsonl")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val poetsToInsert = mutableListOf<dev.wceng.sufei.data.local.room.entity.PoetEntity>()
            
            reader.useLines { lines ->
                lines.forEach { line ->
                    if (line.isNotBlank()) {
                        try {
                            val poet = json.decodeFromString<Poet>(line)
                            poetsToInsert.add(poet.toEntity())
                            if (poetsToInsert.size >= 200) {
                                poetDao.insertPoets(poetsToInsert.toList())
                                poetsToInsert.clear()
                            }
                        } catch (e: Exception) {}
                    }
                }
            }
            if (poetsToInsert.isNotEmpty()) {
                poetDao.insertPoets(poetsToInsert)
            }
        } catch (e: Exception) {}
    }

    private suspend fun importPoems() {
        val inputStream = context.assets.open("poems.jsonl")
        val totalEstimated = 200000 
        var currentCount = 0
        
        val reader = BufferedReader(InputStreamReader(inputStream))
        val entitiesToInsert = mutableListOf<dev.wceng.sufei.data.local.room.entity.PoemEntity>()

        reader.useLines { lines ->
            lines.forEach { line ->
                if (line.isNotBlank()) {
                    try {
                        val poem = json.decodeFromString<Poem>(line)
                        entitiesToInsert.add(poem.toEntity())
                        
                        currentCount++
                        if (entitiesToInsert.size >= 1000) {
                            poemDao.insertPoems(entitiesToInsert.toList())
                            entitiesToInsert.clear()
                            
                            val progress = (currentCount.toFloat() / totalEstimated).coerceAtMost(0.99f)
                            _importState.value = ImportState.Importing(progress)
                        }
                    } catch (e: Exception) {}
                }
            }
        }

        if (entitiesToInsert.isNotEmpty()) {
            poemDao.insertPoems(entitiesToInsert)
        }
    }
}
