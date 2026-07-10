package dev.wceng.sufei.util

import org.junit.Assert.assertEquals
import org.junit.Test

class PoemParserTest {

    @Test
    fun `cleanPoemContent removes Chinese parentheses`() {
        val original = "床前明月光（疑是地上霜）"
        val expected = "床前明月光"
        assertEquals(expected, cleanPoemContent(original))
    }

    @Test
    fun `cleanPoemContent removes English parentheses`() {
        val original = "Hello (World)"
        val expected = "Hello"
        assertEquals(expected, cleanPoemContent(original))
    }


    @Test
    fun `cleanTitle removes content after slash`() {
        assertEquals("出师表", "出师表 / 前出师表".cleanTitle())
    }

    @Test
    fun `cleanAuthor removes space and Zhuan at end`() {
        assertEquals("吕不韦", "吕不韦 撰".cleanAuthor())
    }

    @Test
    fun `cleanDescription removes triangle symbols`() {
        assertEquals("描述", "描述▲▲".cleanDescription())
    }
}
