package dev.wceng.sufei

import dev.wceng.sufei.data.model.Poem
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import java.io.File
import java.util.UUID

class DataCleaner {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
        prettyPrint = false
    }

    @Serializable
    data class RawPoem(
        val id: String? = null,
        val title: String? = null,
        val zuozhe: String? = null,
        val chaodai: String? = null,
        val shici_text: String? = null,
        val tags: List<String> = emptyList(),
        val zhushi: String? = null,
        val fanyi: String? = null,
        val content_json: Map<String, String>? = null
    )

    /**
     * 清洗正文内容：去除首尾空格、去除全角空格、合并多个换行为标准双换行
     */
    private fun cleanContent(text: String?): String {
        if (text == null) return ""
        return text.replace("　", " ") // 先将全角空格替换为普通空格
            .lines()
            .map { it.trim() } // 去除每一行的首尾空格
            .filter { it.isNotBlank() } // 过滤掉空行
            .joinToString("\n") // 使用双换行，使布局更工整
    }

    /**
     * 深度清洗字段：处理前缀、截断脏数据、格式化段落
     */
    private fun cleanField(text: String?, type: String): String? {
        if (text == null) return null
        var result = text.replace("　", " ").trim()

        // 1. 移除常见前缀
        val prefixes = when (type) {
            "notes" -> listOf("注释", "译文及注释")
            "translation" -> listOf("译文", "译文及注释")
            "intro" -> listOf("赏析", "创作背景")
            else -> emptyList()
        }
        for (prefix in prefixes) {
            if (result.startsWith(prefix)) {
                result = result.removePrefix(prefix).trim()
            }
        }

        // 2. 截断脏数据占位符
        val markers = listOf("参考资料：", "完善", "▲", "展开阅读全文", "1、", "2、", "本节内容", "本站免费发布", "站务邮箱")
        for (marker in markers) {
            if (result.contains(marker)) {
                result = result.substringBefore(marker).trim()
            }
        }

        // 3. 再次进行段落工整化处理
        return if (result.isEmpty()) null else cleanContent(result)
    }

    @Test
    fun cleanPoetryData() {
        val inputPath = "C:\\Users\\Wceng\\Desktop\\poems\\gushiwen-cn-200k.jsonl"
        val basePath = "D:/Code/Android/SuFei/app/src/main/assets/"
        val tagsOutputPath = "${basePath}tags.jsonl"
        val dynastiesOutputPath = "${basePath}dynasties.jsonl"

        val inputFile = File(inputPath)
        if (!inputFile.exists()) return
        File(basePath).mkdirs()

        println("开始清洗并分片存储数据...")
        val allTags = mutableSetOf<String>()
        val allDynasties = mutableSetOf<String>()
        var count = 0

        // 创建 5 个分片文件的写入流
        val writers = (0..4).map { File("${basePath}poems_$it.jsonl").bufferedWriter() }

        try {
            inputFile.bufferedReader().forEachLine { line ->
                if (line.isBlank()) return@forEachLine
                try {
                    val raw = json.decodeFromString<RawPoem>(line)

                    // 策略：优先从 content_json["译文及注释"] 提取，因为 raw.fanyi 往往不完整
                    val combinedInfo = raw.content_json?.get("译文及注释")
                    var finalTranslation = raw.fanyi
                    var finalNotes = raw.zhushi

                    if (combinedInfo != null && combinedInfo.contains("注释")) {
                        // 尝试分割译文和注释
                        val parts = combinedInfo.split("\n注释\n", limit = 2)
                        if (parts.size == 2) {
                            finalTranslation = parts[0].replace("译文\n", "")
                            finalNotes = parts[1]
                        }
                    }

                    val cleanedPoem = Poem(
                        id = UUID.randomUUID().toString(),
                        sourceUrl = raw.id ?: "",
                        title = raw.title?.trim() ?: "无题",
                        author = raw.zuozhe?.trim() ?: "未知",
                        content = cleanContent(raw.shici_text),
                        dynasty = raw.chaodai?.trim() ?: "未知",
                        tags = raw.tags.map { it.trim() }.filter { it.isNotBlank() },
                        notes = cleanField(finalNotes, "notes"),
                        translation = cleanField(finalTranslation, "translation"),
                        intro = cleanField(raw.content_json?.get("赏析"), "intro"),
                        background = cleanField(raw.content_json?.get("创作背景"), "intro")
                    )

                    // 均匀分发到 5 个文件
                    val writer = writers[count % 5]
                    writer.write(json.encodeToString(cleanedPoem))
                    writer.newLine()
                    
                    raw.tags.forEach { if (it.isNotBlank()) allTags.add(it.trim()) }
                    val d = raw.chaodai?.trim() ?: "未知"
                    if (d.isNotBlank()) allDynasties.add(d)

                    count++
                    if (count % 10000 == 0) println("已处理 $count 首...")
                } catch (e: Exception) {}
            }
        } finally {
            writers.forEach { it.close() }
        }

        saveMetadata(allTags, allDynasties, tagsOutputPath, dynastiesOutputPath)
        println("清洗完成！已分片存入 5 个文件，共 $count 首。")
    }

    private fun saveMetadata(tags: Set<String>, dynasties: Set<String>, tagPath: String, dynastyPath: String) {
        File(tagPath).bufferedWriter().use { w -> 
            tags.filter { it != "离别｜抒情｜伤感｜怀人" }.sorted().forEach { w.write("{\"name\":\"$it\"}\n") }
        }
        File(dynastyPath).bufferedWriter().use { w -> 
            dynasties.sorted().forEach { w.write("{\"name\":\"$it\"}\n") } 
        }
    }
}
