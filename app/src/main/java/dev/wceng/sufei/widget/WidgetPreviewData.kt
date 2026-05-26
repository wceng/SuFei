package dev.wceng.sufei.widget

import dev.wceng.sufei.data.model.Poem
import dev.wceng.sufei.data.model.UserPoem

fun sampleUserPoem(): UserPoem = UserPoem(
    poem = Poem(
        id = "preview",
        sourceUrl = "",
        title = "静夜思",
        author = "李白",
        dynasty = "唐",
        content = "床前明月光，疑是地上霜。\n举头望明月，低头思故乡。",
        tags = listOf("唐诗", "五言古诗")
    ),
    isFavorite = false
)
