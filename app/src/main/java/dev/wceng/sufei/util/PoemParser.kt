package dev.wceng.sufei.util

fun cleanPoemContent(content: String): String {
    return content.replace(Regex("[（(][^）)]*?[）)]"), "").trim()
}

fun extractVerses(content: String): List<String> {
    return content.split("\n").flatMap { paragraph ->
        paragraph.split(Regex("(?<=[，。！？；])")).filter { it.isNotBlank() }
    }.map { it.trim() }
}
