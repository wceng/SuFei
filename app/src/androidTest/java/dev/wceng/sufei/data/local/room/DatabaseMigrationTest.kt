package dev.wceng.sufei.data.local.room

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {
    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        listOf(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate5To6() {
        // 创建 v5 版本的数据库
        var db = helper.createDatabase(TEST_DB, 5)
        
        // 插入包含 " 撰" 的测试数据
        db.execSQL(
            "INSERT INTO poems (id, sourceUrl, title, author, dynasty, content, tags, notes, translation, intro, background) " +
            "VALUES ('test_5_1', '', 'Title', '吕不韦 撰', '秦代', 'Content', '[]', '', '', '', '')"
        )
        db.execSQL(
            "INSERT INTO poems (id, sourceUrl, title, author, dynasty, content, tags, notes, translation, intro, background) " +
            "VALUES ('test_5_2', '', 'Title', '司马迁  撰 ', '汉代', 'Content', '[]', '', '', '', '')"
        )
        db.execSQL(
            "INSERT INTO poems (id, sourceUrl, title, author, dynasty, content, tags, notes, translation, intro, background) " +
            "VALUES ('test_5_3', '', 'Title', '陈撰', '清代', 'Content', '[]', '', '', '', '')"
        )
        db.close()

        // 迁移到 v6
        db = helper.runMigrationsAndValidate(TEST_DB, 6, true, AppDatabase.MIGRATION_5_6)

        // 验证作者字段是否被清理
        val cursor = db.query("SELECT id, author FROM poems")
        while (cursor.moveToNext()) {
            val id = cursor.getString(cursor.getColumnIndex("id"))
            val author = cursor.getString(cursor.getColumnIndex("author"))
            when (id) {
                "test_5_1" -> assertEquals("吕不韦", author)
                "test_5_2" -> assertEquals("司马迁", author)
                "test_5_3" -> assertEquals("陈撰", author)
            }
        }
        cursor.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate8To9() {
        // 创建 v8 版本的数据库并插入带括号的数据
        var db = helper.createDatabase(TEST_DB, 8)
        
        // 插入测试数据
        db.execSQL(
            "INSERT INTO poems (id, sourceUrl, title, author, dynasty, content, tags, notes, translation, intro, background) " +
            "VALUES ('test_8_1', '', 'Title', 'Author', 'Dynasty', '功盖三分国，名成八阵图。（名成 一作：名高）', '[]', '', '', '', '')"
        )
        db.execSQL(
            "INSERT INTO poems (id, sourceUrl, title, author, dynasty, content, tags, notes, translation, intro, background) " +
            "VALUES ('test_8_2', '', 'Title', 'Author', 'Dynasty', '客路青山外，行舟绿水前。(青山外 一作：青山下)', '[]', '', '', '', '')"
        )
        db.close()

        // 迁移到 v9
        db = helper.runMigrationsAndValidate(TEST_DB, 9, true, AppDatabase.MIGRATION_8_9)

        // 验证结果
        val cursor = db.query("SELECT id, content FROM poems")
        while (cursor.moveToNext()) {
            val id = cursor.getString(cursor.getColumnIndex("id"))
            val content = cursor.getString(cursor.getColumnIndex("content"))
            when (id) {
                "test_8_1" -> assertEquals("功盖三分国，名成八阵图。", content)
                "test_8_2" -> assertEquals("客路青山外，行舟绿水前。", content)
            }
        }
        cursor.close()
    }
}
