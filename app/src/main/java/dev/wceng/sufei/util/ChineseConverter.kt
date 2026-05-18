package dev.wceng.sufei.util

import com.github.houbb.opencc4j.util.ZhConverterUtil
import com.github.houbb.opencc4j.util.ZhHkConverterUtil
import com.github.houbb.opencc4j.util.ZhTwConverterUtil
import dev.wceng.sufei.data.model.ChineseVariant
import dev.wceng.sufei.data.model.Poem
import dev.wceng.sufei.data.model.Poet
import dev.wceng.sufei.data.model.UserPoem

/**
 * 繁简转换工具类
 */
object ChineseConverter {

    fun convert(text: String, variant: ChineseVariant): String {
        return when (variant) {
            ChineseVariant.TRADITIONAL_HK -> ZhHkConverterUtil.toTraditional(text)
            ChineseVariant.TRADITIONAL_TW -> ZhTwConverterUtil.toTraditional(text)
            else -> ZhConverterUtil.toSimple(text)
        }
    }

    fun toSimplified(text: String): String {
        return ZhConverterUtil.toSimple(text)
    }
}

fun Poem.convert(variant: ChineseVariant): Poem {
    if (variant == ChineseVariant.SIMPLIFIED) return this
    return this.copy(
        title = ChineseConverter.convert(title, variant),
        author = ChineseConverter.convert(author, variant),
        dynasty = ChineseConverter.convert(dynasty, variant),
        content = ChineseConverter.convert(content, variant),
        tags = tags.map { ChineseConverter.convert(it, variant) },
        notes = notes?.let { ChineseConverter.convert(it, variant) },
        translation = translation?.let { ChineseConverter.convert(it, variant) },
        intro = intro?.let { ChineseConverter.convert(it, variant) },
        background = background?.let { ChineseConverter.convert(it, variant) }
    )
}

fun UserPoem.convert(variant: ChineseVariant): UserPoem {
    if (variant == ChineseVariant.SIMPLIFIED) return this
    return this.copy(poem = poem.convert(variant))
}

fun Poet.convert(variant: ChineseVariant): Poet {
    if (variant == ChineseVariant.SIMPLIFIED) return this
    return this.copy(
        name = ChineseConverter.convert(name, variant),
        dynasty = ChineseConverter.convert(dynasty, variant),
        lifetime = lifetime?.let { ChineseConverter.convert(it, variant) },
        descriptions = descriptions.map { 
            it.copy(
                type = ChineseConverter.convert(it.type, variant),
                content = ChineseConverter.convert(it.content, variant)
            )
        }
    )
}
