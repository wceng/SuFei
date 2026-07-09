package dev.wceng.sufei.data.local.datastore

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FavoritesMigrationTest {

    @Test
    fun `shouldMigrate returns true when old list is not empty and map is empty`() = runTest {
        val data = UserPreferences.newBuilder()
            .addFavoritePoemIds("poem1")
            .build()
        
        assertTrue(FavoritesMigration.shouldMigrate(data))
    }

    @Test
    fun `shouldMigrate returns false when old list is empty`() = runTest {
        val data = UserPreferences.newBuilder().build()
        
        assertFalse(FavoritesMigration.shouldMigrate(data))
    }

    @Test
    fun `shouldMigrate returns false when map is not empty`() = runTest {
        val data = UserPreferences.newBuilder()
            .putFavorites("poem1", 12345L)
            .build()
        
        assertFalse(FavoritesMigration.shouldMigrate(data))
    }

    @Test
    fun `migrate moves ids to favorites map with 0 timestamp`() = runTest {
        val data = UserPreferences.newBuilder()
            .addFavoritePoemIds("poem1")
            .addFavoritePoemIds("poem2")
            .build()
        
        val migrated = FavoritesMigration.migrate(data)
        
        assertEquals(2, migrated.favoritesMap.size)
        assertEquals(0L, migrated.favoritesMap["poem1"])
        assertEquals(0L, migrated.favoritesMap["poem2"])
        assertTrue(migrated.favoritePoemIdsList.isEmpty())
    }
}
