package dev.wceng.sufei

import dev.wceng.sufei.data.model.Poet
import dev.wceng.sufei.data.model.PoetDescription
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import java.io.File
import java.util.UUID
import kotlin.uuid.Uuid

class PoetDataCleaner {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
        prettyPrint = false
    }

    @Serializable
    data class RawPoet(
        val onlyId: String? = null,
        val name: String? = null,
        val dynasty: String? = null,
        val avatar: String? = null,
        val quantity: Int = 0,
        val lifetime: String? = null,
        val describe: List<RawDescribe> = emptyList()
    )

    @Serializable
    data class RawDescribe(
        val type: String? = null,
        val content: List<String> = emptyList()
    )

    private fun cleanContent(content: List<String>): String? {
        if (content.isEmpty()) return null
        val cleanedLines = content.map { line ->
            line.replace("　", " ").trim()
        }.filter { it.isNotBlank() }
        
        return if (cleanedLines.isEmpty()) null else cleanedLines.joinToString("\n\n")
    }

    @Test
    fun cleanPoetData() {
        val inputPath = "C:\\Users\\Wceng\\Desktop\\poems\\poems-authors.json"
        val outputPath = "D:/Code/Android/SuFei/app/src/main/assets/poets.jsonl"

        val inputFile = File(inputPath)
        val outputFile = File(outputPath)

        outputFile.parentFile?.mkdirs()

        if (!inputFile.exists()) {
            println("找不到诗人原始文件: ${inputFile.absolutePath}")
            return
        }

        println("开始清洗诗人数据 (支持多描述项)...")
        var count = 0
        var errorCount = 0

        outputFile.bufferedWriter().use { writer ->
            inputFile.bufferedReader().forEachLine { line ->
                if (line.isBlank()) return@forEachLine
                try {
                    val raw = json.decodeFromString<RawPoet>(line)
                    
                    // 将原始的 describe 列表转换为 PoetDescription 对象集合
                    val descriptions = raw.describe.mapNotNull { rawDesc ->
                        val cleanedText = cleanContent(rawDesc.content)
                        if (cleanedText != null) {
                            PoetDescription(
                                type = rawDesc.type?.trim() ?: "其他",
                                content = cleanedText
                            )
                        } else null
                    }

                    val cleaned = Poet(
                        id = UUID.randomUUID().toString(),
                        name = raw.name?.trim() ?: "未知",
                        dynasty = raw.dynasty?.trim() ?: "未知",
                        avatarUrl = raw.avatar,
                        lifetime = raw.lifetime?.replace("　", " ")?.trim(),
                        descriptions = descriptions,
                        poemCount = raw.quantity
                    )

                    writer.write(json.encodeToString(cleaned))
                    writer.newLine()

                    count++
                    if (count % 100 == 0) println("已处理 $count 位诗人...")
                } catch (e: Exception) {
                    errorCount++
                }
            }
        }

        println("清洗完成！")
        println("诗人总数: $count, 失败: $errorCount")
        println("结果文件: ${outputFile.absolutePath}")
    }
}
