package dev.wceng.sufei.widget

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
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
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.TextAlign
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import dagger.hilt.android.EntryPointAccessors
import dev.wceng.sufei.MainActivity
import dev.wceng.sufei.R
import dev.wceng.sufei.data.model.Poem
import dev.wceng.sufei.data.model.UserPoem
import dev.wceng.sufei.di.PoemRepositoryEntryPoint
import dev.wceng.sufei.util.PoemExtractor
import dev.wceng.sufei.widget.LocalFeihongColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class DailyPoemWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(150.dp, 110.dp),
            DpSize(200.dp, 180.dp),
            DpSize(250.dp, 300.dp),
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val feihongColor = Color(ContextCompat.getColor(context, R.color.feihong))
        val loadingText = context.getString(R.string.widget_daily_poem_loading)
        val poemRepository = EntryPointAccessors.fromApplication(
            context.applicationContext,
            PoemRepositoryEntryPoint::class.java
        ).poemRepository

        val userPoem = withContext(Dispatchers.IO) {
            poemRepository.getRandomUserPoem().firstOrNull()
        }

        provideContent {
            CompositionLocalProvider(LocalFeihongColor provides feihongColor) {
                GlanceTheme {
                    if (userPoem != null) {
                        PoemCard(userPoem)
                    } else {
                        ErrorContent(loadingText)
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
    val size = LocalSize.current
    val poem = userPoem.poem
    val highlightLines = PoemExtractor.extractHighlight(poem)
    val highlightText = highlightLines.joinToString("\n")
    val displayText = highlightText.ifEmpty { poem.title }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable(
                onClick = actionStartActivity<MainActivity>(
                    actionParametersOf(extraPoemId to poem.id)
                )
            )
    ) {
        when {
            size.height <= 130.dp -> SmallContent(displayText)
            size.height <= 200.dp -> MediumContent(displayText, poem)
            else -> LargeContent(displayText, poem)
        }
    }
}

@Composable
private fun SmallContent(text: String) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            style = TextStyle(
                color = GlanceTheme.colors.onBackground,
                fontSize = 18.sp,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center
            ),
            maxLines = 3
        )
    }
}

@Composable
private fun MediumContent(text: String, poem: Poem) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            style = TextStyle(
                color = GlanceTheme.colors.onBackground,
                fontSize = 18.sp,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center
            ),
            maxLines = 4
        )
        Spacer(modifier = GlanceModifier.height(16.dp))
        Box(
            modifier = GlanceModifier
                .width(32.dp)
                .height(1.dp)
                .background(LocalFeihongColor.current)        ) { }
        Spacer(modifier = GlanceModifier.height(12.dp))
        Text(
            text = "${poem.title}  ·  ${poem.author}",
            style = TextStyle(
                fontSize = 12.sp,
                color = ColorProvider(LocalFeihongColor.current),
                textAlign = TextAlign.Center
            ),
            maxLines = 1
        )
    }
}

@Composable
private fun LargeContent(text: String, poem: Poem) {
    val lines = remember(text) { text.split("\n") }

    Box(
        modifier = GlanceModifier.fillMaxSize()
    ) {
        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: title 多列并排，每列最多 8 字 + author 逐字纵向
            Column(
                modifier = GlanceModifier.defaultWeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.Top
                ) {
                    poem.title.chunked(8).reversed().forEachIndexed { colIndex, chunk ->
                        if (colIndex > 0) {
                            Spacer(modifier = GlanceModifier.width(16.dp))
                        }
                        Column(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            chunk.forEach { char ->
                                Text(
                                    text = char.toString(),
                                    style = TextStyle(
                                        color = GlanceTheme.colors.onBackground,
                                        fontSize = 16.sp,
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = GlanceModifier.height(12.dp))
                Box(
                    modifier = GlanceModifier
                        .background(LocalFeihongColor.current.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Column(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        poem.author.forEach { char ->
                            Text(
                                text = char.toString(),
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    color = ColorProvider(LocalFeihongColor.current),
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                }
            }

            // Right: 摘句逐字纵向排列（多行 RTL，每行超 5 字则分组）
            Row(
                modifier = GlanceModifier.defaultWeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                lines.asReversed().forEachIndexed { index, line ->
                    Column(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        line.chunked(5).forEachIndexed { chunkIndex, chunk ->
                            if (chunkIndex > 0) {
                                Spacer(modifier = GlanceModifier.height(2.dp))
                            }
                            Column(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                chunk.forEach { char ->
                                    Text(
                                        text = char.toString(),
                                        style = TextStyle(
                                            color = GlanceTheme.colors.onBackground,
                                            fontSize = 18.sp,
                                            fontFamily = FontFamily.Serif,
                                            textAlign = TextAlign.Center
                                        )
                                    )
                                    Spacer(modifier = GlanceModifier.height(2.dp))
                                }
                            }
                        }
                    }
                    if (index < lines.size - 1) {
                        Spacer(modifier = GlanceModifier.width(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(text: String) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            style = TextStyle(fontSize = 12.sp)
        )
    }
}
