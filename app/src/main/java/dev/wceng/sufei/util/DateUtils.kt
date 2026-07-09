package dev.wceng.sufei.util

import android.content.Context
import dev.wceng.sufei.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    /**
     * 格式化收藏日期
     * - 时间戳为 0：往日收藏
     * - 今年：7月8日
     * - 往年：2024年3月4日
     */
    fun formatDate(context: Context, timestamp: Long): String {
        if (timestamp <= 0) {
            return context.getString(R.string.date_old_favorites)
        }

        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        
        calendar.timeInMillis = timestamp
        val favoriteYear = calendar.get(Calendar.YEAR)

        return if (currentYear == favoriteYear) {
            SimpleDateFormat("M月d日", Locale.getDefault()).format(Date(timestamp))
        } else {
            SimpleDateFormat("yyyy年M月d日", Locale.getDefault()).format(Date(timestamp))
        }
    }
}
