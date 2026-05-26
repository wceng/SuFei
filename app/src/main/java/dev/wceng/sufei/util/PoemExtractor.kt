package dev.wceng.sufei.util

import dev.wceng.sufei.data.model.Poem

object PoemExtractor {

    /**
     * 判断是否为词/曲
     */
    fun isCi(poem: Poem): Boolean {
        if (poem.tags.any { tag ->
                val sTag = ChineseConverter.toSimplified(tag)
                sTag.contains("词") || sTag.contains("曲") || sTag.contains("诗余")
            }) return true
        if (poem.title.contains("·") || poem.title.contains("・")) return true

        val lines = poem.content.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return false
        val lengths = lines.map { it.filter { char -> char.isLetterOrDigit() }.length }
        val isRegularPoem = lengths.all { it == 5 || it == 7 } && lengths.distinct().size == 1

        return !isRegularPoem
    }

    private val PUNCTUATION_REGEX = Regex("\\p{P}")

    /**
     * 提取精华句子，确保是完整句子
     * - 词/曲：取最后一句
     * - 诗：取第二句（篇幅短则取第一句）
     * 返回按标点分割的短语列表
     */
    fun extractHighlight(poem: Poem, keepPunctuation: Boolean = false): List<String> {
        val content = poem.content
        val isCiPoem = isCi(poem)

        val fullSentences = content
            .split(Regex("(?<=[。！？])"))
            .map { it.trim() }
            .filter { it.isNotBlank() && it.length > 2 }

        if (fullSentences.isEmpty()) return listOf(content.take(12))

        val targetFullSentence = if (isCiPoem) {
            fullSentences.last()
        } else {
            val allPhrases = content.split(Regex("(?<=[，。！？])")).map { it.trim() }.filter { it.isNotEmpty() }
            when {
                allPhrases.size >= 8 && fullSentences.size >= 2 -> fullSentences[1]
                else -> fullSentences.first()
            }
        }

        val phrases = targetFullSentence
            .split(Regex("(?<=[，；。！？])"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        return if (keepPunctuation) phrases
        else phrases.map { it.replace(PUNCTUATION_REGEX, "").trim() }.filter { it.isNotEmpty() }
    }
}
