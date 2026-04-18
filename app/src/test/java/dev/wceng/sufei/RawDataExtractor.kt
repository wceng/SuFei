package dev.wceng.sufei

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import java.io.File

class RawDataExtractor {

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

    @Test
    fun extractRawPoetryData() {
        // 输入路径与 DataCleaner 保持一致
        val inputPath = "C:\\Users\\Wceng\\Desktop\\poems\\gushiwen-cn-200k.jsonl"
        val outputPath = "D:/Code/Android/SuFei/app/src/test/resources/raw_poems_extracted.jsonl"

        val inputFile = File(inputPath)
        val outputFile = File(outputPath)

        // 确保输出目录存在
        outputFile.parentFile?.mkdirs()

        if (!inputFile.exists()) {
            println("找不到原始文件: ${inputFile.absolutePath}")
            return
        }

        println("开始提取原始数据并保持原样...")
        var count = 0
        var errorCount = 0

        outputFile.bufferedWriter().use { writer ->
            inputFile.bufferedReader().forEachLine { line ->
                if (line.isBlank()) return@forEachLine
                try {
                    // 仅解析为 RawPoem 对象以验证格式，不进行任何字段修改
                    val raw = json.decodeFromString<RawPoem>(line)
                    
                    // 将验证后的原始对象重新序列化并写入
                    writer.write(json.encodeToString(raw))
                    writer.newLine()

                    count++
                    if (count % 10000 == 0) println("已提取 $count 首...")
                } catch (e: Exception) {
                    errorCount++
                }
            }
        }

        println("提取完成！")
        println("处理总数: $count, 失败: $errorCount")
        println("提取后的文件已保存至: ${outputFile.absolutePath}")
    }
}
