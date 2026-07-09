package dev.wceng.sufei.domain.usecase

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.wceng.sufei.data.repository.PoemRepository
import dev.wceng.sufei.domain.model.GroupedUserPoem
import dev.wceng.sufei.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetGroupedFavoritesUseCase @Inject constructor(
    private val poemRepository: PoemRepository,
    @ApplicationContext private val context: Context
) {
    operator fun invoke(): Flow<List<GroupedUserPoem>> {
        return poemRepository.getFavoriteUserPoems().map { poems ->
            poems.sortedByDescending { it.favoritedTimestamp ?: 0L }
                .groupBy { DateUtils.formatDate(context, it.favoritedTimestamp ?: 0L) }
                .map { (date, poemsAtDate) ->
                    GroupedUserPoem(date, poemsAtDate)
                }
        }
    }
}
