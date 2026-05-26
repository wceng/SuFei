package dev.wceng.sufei.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.AppWidgetId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.width
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.wrapContentSize
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import dagger.hilt.android.EntryPointAccessors
import dev.wceng.sufei.R
import dev.wceng.sufei.MainActivity
import dev.wceng.sufei.widget.LocalFeihongColor
import dev.wceng.sufei.data.model.UserPoem
import dev.wceng.sufei.di.PoemRepositoryEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class BedsidePoemWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val feihongColor = Color(ContextCompat.getColor(context, R.color.feihong))
        val emptyText = context.getString(R.string.widget_bedside_poem_empty)
        val appWidgetId = (id as? AppWidgetId)?.appWidgetId ?: return

        val poemRepository = EntryPointAccessors.fromApplication(
            context.applicationContext,
            PoemRepositoryEntryPoint::class.java
        ).poemRepository

        val poemId = withContext(Dispatchers.IO) {
            poemRepository.resolveWidgetPoemId(appWidgetId)
        }

        if (poemId == null) {
            provideContent {
                CompositionLocalProvider(LocalFeihongColor provides feihongColor) {
                    GlanceTheme {
                        EmptyPlaceholder(emptyText)
                    }
                }
            }
            return
        }

        val userPoem = withContext(Dispatchers.IO) {
            poemRepository.getUserPoemById(poemId).firstOrNull()
        }

        provideContent {
            CompositionLocalProvider(LocalFeihongColor provides feihongColor) {
                GlanceTheme {
                    if (userPoem != null) {
                        PoemCard(userPoem)
                    } else {
                        EmptyPlaceholder(emptyText)
                    }
                }
            }
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        val feihongColor = Color(ContextCompat.getColor(context, R.color.feihong))
        val samplePoem = sampleUserPoem()

        provideContent {
            CompositionLocalProvider(LocalFeihongColor provides feihongColor) {
                GlanceTheme {
                    PoemCard(samplePoem)
                }
            }
        }
    }
}

private val extraPoemId = ActionParameters.Key<String>("poem_id")

@Composable
private fun PoemCard(userPoem: UserPoem) {
    val poem = userPoem.poem
    val paragraphs = poem.content.split("\n").filter { it.isNotBlank() }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .then(widgetBackgroundModifier())
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .clickable(
                onClick = actionStartActivity<MainActivity>(
                    actionParametersOf(extraPoemId to poem.id)
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 标题（固定）
        Text(
            text = poem.title,
            style = TextStyle(
                color = GlanceTheme.colors.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center
            ),
            maxLines = 2
        )

        Spacer(modifier = GlanceModifier.height(16.dp))

        // 朝代 · 作者（固定）
        Text(
            text = "${poem.dynasty} · ${poem.author}",
            style = TextStyle(
                fontSize = 13.sp,
                color = GlanceTheme.colors.onBackground,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center
            ),
            maxLines = 1
        )

        Spacer(modifier = GlanceModifier.height(12.dp))

        // 妃红分隔线（固定）
        Box(
            modifier = GlanceModifier
                .width(48.dp)
                .height(1.dp)
                .background(LocalFeihongColor.current)
        ) { }

        Spacer(modifier = GlanceModifier.height(16.dp))

        // 正文 — 可滚动，按段落展示，左对齐
        LazyColumn(
            modifier = GlanceModifier
                .fillMaxWidth(),
        ) {
            paragraphs.forEach { paragraph ->
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = GlanceModifier.fillMaxWidth()
                    ) {
                        Text(
                            text = paragraph,
                            modifier = GlanceModifier.wrapContentSize(),
                            style = TextStyle(
                                color = GlanceTheme.colors.onBackground,
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Serif,
                                textAlign = TextAlign.Start
                            )
                        )
                    }
                }
                item {
                    Spacer(modifier = GlanceModifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun EmptyPlaceholder(text: String) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .then(widgetBackgroundModifier())
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 12.sp,
                color = GlanceTheme.colors.onBackground
            )
        )
    }
}
