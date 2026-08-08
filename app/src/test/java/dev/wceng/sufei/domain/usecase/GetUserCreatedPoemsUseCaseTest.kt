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

class GetUserCreatedPoemsUseCaseTest {

    private val poemRepository: PoemRepository = mockk()
    private val context: Context = mockk()
    private val useCase = GetUserCreatedPoemsUseCase(poemRepository, context)

    @Test
    fun `invoke groups user poems by date and sorts them descending`() = runTest {
        val poem1 = UserPoem(
            poem = createPoem("1"),
            isFavorite = false,
            favoritedTimestamp = 1000L // 创作时间戳（复用 favoritedTimestamp 字段）
        )
        val poem2 = UserPoem(
            poem = createPoem("2"),
            isFavorite = false,
            favoritedTimestamp = 2000L
        )
        val poem3 = UserPoem(
            poem = createPoem("3"),
            isFavorite = false,
            favoritedTimestamp = 500L
        )

        every { poemRepository.getUserCreatedPoems() } returns flowOf(listOf(poem1, poem2, poem3))
        every { context.getString(R.string.date_old_favorites) } returns "往日收藏"

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
        author = "佚名",
        dynasty = "当代",
        content = "Content",
        tags = emptyList()
    )
}
