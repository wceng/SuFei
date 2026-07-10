package dev.wceng.sufei.util

fun cleanPoemContent(content: String): String {
    var result = content
    // 使用 Unicode 编码支持 () 和 （）
    val open = "\\u0028\\uFF08"
    val close = "\\u0029\\uFF09"
    val regex = Regex("[$open][^$open$close]*[$close]")
    
    var prev: String
    do {
        prev = result
        result = result.replace(regex, "")
    } while (result != prev)

    return result.trim()
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
