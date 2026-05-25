package dev.wceng.sufei.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import dev.wceng.sufei.MainActivity
import dagger.hilt.android.EntryPointAccessors
import dev.wceng.sufei.data.model.Poem
import dev.wceng.sufei.data.model.UserPoem
import dev.wceng.sufei.di.PoemRepositoryEntryPoint
import dev.wceng.sufei.util.PoemExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class DailyPoemWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val poemRepository = EntryPointAccessors.fromApplication(
            context.applicationContext,
            PoemRepositoryEntryPoint::class.java
        ).poemRepository

        val userPoem = withContext(Dispatchers.IO) {
            poemRepository.getRandomUserPoem().firstOrNull()
        }

        provideContent {
            GlanceTheme {
                if (userPoem != null) {
                    PoemCard(userPoem)
                } else {
                    ErrorContent()
                }
            }
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        val samplePoem = UserPoem(
            poem = Poem(
                id = "preview",
                sourceUrl = "",
                title = "静夜思",
                author = "李白",
                dynasty = "唐",
                content = "床前明月光\n疑是地上霜\n举头望明月\n低头思故乡",
                tags = listOf("唐诗", "五言古诗")
            ),
            isFavorite = false
        )

        provideContent {
            GlanceTheme {
                PoemCard(samplePoem)
            }
        }
    }
}

private val extraPoemId = ActionParameters.Key<String>("poem_id")

@Composable
private fun PoemCard(userPoem: UserPoem) {
    val poem = userPoem.poem
    val highlightLines = PoemExtractor.extractHighlight(poem)
    val highlightText = highlightLines.joinToString("\n")

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(16.dp)
            .clickable(
                onClick = actionStartActivity<MainActivity>(
                    actionParametersOf(extraPoemId to poem.id)
                )
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = highlightText.ifEmpty { poem.title },
            style = TextStyle(
                fontSize = 18.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal
            ),
            maxLines = 3
        )

        Box(
            modifier = GlanceModifier
                .padding(top = 12.dp, bottom = 8.dp)
                .width(24.dp)
                .height(1.dp)
                .background(GlanceTheme.colors.outline)
        ) { }

        Text(
            text = "${poem.title}  ·  ${poem.author}",
            style = TextStyle(
                fontSize = 11.sp,
                color = GlanceTheme.colors.primary
            ),
            maxLines = 1
        )
    }
}

@Composable
private fun ErrorContent() {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "加载中...",
            style = TextStyle(fontSize = 12.sp)
        )
    }
}
