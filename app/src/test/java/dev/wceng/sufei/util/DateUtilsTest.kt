package dev.wceng.sufei.util

import android.content.Context
import dev.wceng.sufei.R
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class DateUtilsTest {

    private val context: Context = mockk()

    @Test
    fun `formatDate returns old favorites when timestamp is 0`() {
        every { context.getString(R.string.date_old_favorites) } returns "往日收藏"
        val result = DateUtils.formatDate(context, 0L)
        assertEquals("往日收藏", result)
    }

    @Test
    fun `formatDate returns date without year when timestamp is in current year`() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        calendar.set(currentYear, Calendar.JULY, 8)
        val timestamp = calendar.timeInMillis

        val result = DateUtils.formatDate(context, timestamp)
        assertEquals("7月8日", result)
    }

    @Test
    fun `formatDate returns date with year when timestamp is in previous year`() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        calendar.set(currentYear - 1, Calendar.MARCH, 4)
        val timestamp = calendar.timeInMillis

        val result = DateUtils.formatDate(context, timestamp)
        assertEquals("${currentYear - 1}年3月4日", result)
    }
}
