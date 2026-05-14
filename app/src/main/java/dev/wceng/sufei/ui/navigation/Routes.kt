package dev.wceng.sufei.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object Splash

@Serializable
object Home

@Serializable
data class Explore(
    val query: String? = null,
    val tag: String? = null,
    val tune: String? = null,
    val dynasty: String? = null
)

@Serializable
object Collection

@Serializable
object Settings

@Serializable
data class Detail(val id: String)

@Serializable
data class PoetDetail(val id: String)

@Serializable
data class PoetWorks(val poetName: String)
