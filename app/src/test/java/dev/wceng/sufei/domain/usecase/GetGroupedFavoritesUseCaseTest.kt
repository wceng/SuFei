package dev.wceng.sufei.domain.usecase

import android.content.Context
import dev.wceng.sufei.R
import dev.wceng.sufei.data.model.Poem
import dev.wceng.sufei.data.model.UserPoem
import dev.wceng.sufei.data.repository.PoemRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetGroupedFavoritesUseCaseTest {

    private val poemRepository: PoemRepository = mockk()
    private val context: Context = mockk()
    private val useCase = GetGroupedFavoritesUseCase(poemRepository, context)

    @Test
    fun `invoke groups poems by date and sorts them descending`() = runTest {
        val poem1 = UserPoem(
            poem = createPoem("1"),
            isFavorite = true,
            favoritedTimestamp = 1000L
        )
        val poem2 = UserPoem(
            poem = createPoem("2"),
            isFavorite = true,
            favoritedTimestamp = 2000L
        )
        val poem3 = UserPoem(
            poem = createPoem("3"),
            isFavorite = true,
            favoritedTimestamp = 500L
        )

        every { poemRepository.getFavoriteUserPoems() } returns flowOf(listOf(poem1, poem2, poem3))
        every { context.getString(R.string.date_old_favorites) } returns "往日收藏"
        // DateUtils.formatDate depends on system time for "今年" vs "往年", 
        // but for small timestamps it should return "往日收藏" (if <= 0) or a formatted date.
        // Since we are mocking context and R.string, let's just assume what DateUtils returns.

        val result = useCase().first()

        assertEquals(1, result.size) // All have small timestamps, grouped by DateUtils logic
        assertEquals(3, result[0].poems.size)
        assertEquals("2", result[0].poems[0].poem.id) // poem2 (2000)
        assertEquals("1", result[0].poems[1].poem.id) // poem1 (1000)
        assertEquals("3", result[0].poems[2].poem.id) // poem3 (500)
    }

    private fun createPoem(id: String) = Poem(
        id = id,
        sourceUrl = "",
        title = "Title $id",
        author = "Author",
        dynasty = "Dynasty",
        content = "Content",
        tags = emptyList()
    )
}
