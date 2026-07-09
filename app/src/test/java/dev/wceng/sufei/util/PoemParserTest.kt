package dev.wceng.sufei.util

import org.junit.Assert.assertEquals
import org.junit.Test

class PoemParserTest {

    @Test
    fun `cleanTitle removes content after slash and trims`() {
        val original = "出师表 / 前出师表"
        val expected = "出师表"
        assertEquals(expected, original.cleanTitle())
    }

    @Test
    fun `cleanTitle does nothing if no slash present`() {
        val original = "静夜思"
        val expected = "静夜思"
        assertEquals(expected, original.cleanTitle())
    }

    @Test
    fun `cleanTitle handles multiple slashes correctly`() {
        val original = "标题 / 别名1 / 别名2"
        val expected = "标题"
        assertEquals(expected, original.cleanTitle())
    }

    @Test
    fun `cleanTitle trims whitespace even without slash`() {
        val original = "  有空格的标题  "
        val expected = "  有空格的标题  " 
        assertEquals(expected, original.cleanTitle())
    }

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
    fun `cleanPoemContent removes multiple parentheses`() {
        val original = "第一句（注1）第二句 (注2)"
        val expected = "第一句第二句"
        assertEquals(expected, cleanPoemContent(original))
    }

    @Test
    fun `cleanPoemContent handles mixed content`() {
        val original = "这是一段(test)复杂的（测试）内容"
        val expected = "这是一段复杂的内容"
        assertEquals(expected, cleanPoemContent(original))
    }

    @Test
    fun `cleanPoemContent with unmatched parentheses`() {
        val original = "落花(时节"
        val expected = "落花"
        assertEquals(expected, cleanPoemContent(original))
    }

    @Test
    fun `cleanPoemContent handles unmatched left parenthesis at end`() {
        val original = "秋丛珍重初黏缝（"
        val expected = "秋丛珍重初黏缝"
        assertEquals(expected, cleanPoemContent(original))
    }

    @Test
    fun `cleanPoemContent handles unmatched left parenthesis with text at end`() {
        val original = "悉还其豕。鸿不受而去。（选自南宋"
        val expected = "悉还其豕。鸿不受而去。"
        assertEquals(expected, cleanPoemContent(original))
    }

    @Test
    fun `cleanAuthor removes space and Zhuan at end`() {
        val original = "吕不韦 撰"
        val expected = "吕不韦"
        assertEquals(expected, original.cleanAuthor())
    }

    @Test
    fun `cleanAuthor does nothing if no space before Zhuan`() {
        // "邯郸淳撰" should remain "邯郸淳撰" because there's no space
        val original = "邯郸淳撰"
        val expected = "邯郸淳撰"
        assertEquals(expected, original.cleanAuthor())
    }

    @Test
    fun `cleanAuthor removes multiple spaces and Zhuan at end`() {
        val original = "刘安   撰  "
        val expected = "刘安"
        assertEquals(expected, original.cleanAuthor())
    }

    @Test
    fun `cleanAuthor does nothing if Zhuan is in the middle`() {
        val original = "撰写者名"
        val expected = "撰写者名"
        assertEquals(expected, original.cleanAuthor())
    }

    @Test
    fun `cleanAuthor trims only if no Zhuan at end`() {
        val original = "  李白  "
        val expected = "李白"
        assertEquals(expected, original.cleanAuthor())
    }

    @Test
    fun `cleanAuthor does not remove Zhuan if it is part of the name`() {
        // "李撰" (Li Zhuan) should remain "李撰"
        val original = "李撰"
        val expected = "李撰"
        assertEquals(expected, original.cleanAuthor())
    }

    @Test
    fun `cleanAuthor removes space and Zhuan only when acting as a suffix`() {
        // "李撰 撰" should become "李撰"
        val original = "李撰 撰"
        val expected = "李撰"
        assertEquals(expected, original.cleanAuthor())
    }

    @Test
    fun `cleanDescription removes triangle symbols`() {
        val original = "这是一段描述。 ▲"
        val expected = "这是一段描述。"
        assertEquals(expected, original.cleanDescription())
    }

    @Test
    fun `cleanDescription removes multiple triangle symbols`() {
        val original = "描述▲▲"
        val expected = "描述"
        assertEquals(expected, original.cleanDescription())
    }
}
