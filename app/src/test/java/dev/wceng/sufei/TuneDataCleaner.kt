package dev.wceng.sufei

import dev.wceng.sufei.data.model.Tune
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import java.io.File

class TuneDataCleaner {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
        prettyPrint = false
    }

    @Serializable
    data class RawTune(
        val name: String? = null,
        val description: String? = null
    )

    @Test
    fun cleanTuneData() {
        val inputPath = "C:\\Users\\Wceng\\Desktop\\poems\\poems-cipai.json"
        val outputPath = "D:/Code/Android/SuFei/app/src/main/assets/tunes.jsonl"

        val inputFile = File(inputPath)
        val outputFile = File(outputPath)

        outputFile.parentFile?.mkdirs()

        if (!inputFile.exists()) {
            println("找不到词牌原始文件: ${inputFile.absolutePath}")
            return
        }

        println("开始清洗词牌数据...")
        var count = 0
        var errorCount = 0

        outputFile.bufferedWriter().use { writer ->
            inputFile.bufferedReader().forEachLine { line ->
                if (line.isBlank()) return@forEachLine
                try {
                    val raw = json.decodeFromString<RawTune>(line)
                    
                    val cleaned = Tune(
                        name = raw.name?.trim() ?: "未知",
                        description = raw.description?.replace("　", " ")?.trim()
                    )

                    writer.write(json.encodeToString(cleaned))
                    writer.newLine()

                    count++
                } catch (e: Exception) {
                    errorCount++
                }
            }
        }

        println("清洗完成！")
        println("词牌总数: $count, 失败: $errorCount")
        println("结果文件: ${outputFile.absolutePath}")
    }
}
