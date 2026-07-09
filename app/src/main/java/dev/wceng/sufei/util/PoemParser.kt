package dev.wceng.sufei.util

fun cleanPoemContent(content: String): String {
    return content.replace(Regex("[（(][^（）()]*([）)]|$)"), "").trim()
}

fun String.cleanTitle(): String {
    return if (this.contains("/")) {
        this.substringBefore("/").trim()
    } else {
        this
    }
}

fun String.cleanAuthor(): String {
    return this.replace(Regex("\\s+撰\\s*$"), "").trim()
}

fun String.cleanDescription(): String {
    return this.replace("▲", "").trim()
}

fun extractVerses(content: String): List<String> {
    return content.split("\n").flatMap { paragraph ->
        paragraph.split(Regex("(?<=[，。！？；])")).filter { it.isNotBlank() }
    }.map { it.trim() }
}
