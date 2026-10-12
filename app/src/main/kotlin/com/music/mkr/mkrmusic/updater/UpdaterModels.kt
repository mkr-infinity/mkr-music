package com.music.mkr.mkrmusic.updater

data class SDUIModifier(
    val weight: Float? = null,
    val heightDp: Int? = null,
    val fillMaxWidth: Boolean = false,
    val paddingDp: Int? = null,
    val aspectRatio: Float? = null
)

class SDUIBlock(
    val type: String,
    val url: String? = null,
    val modifierParams: SDUIModifier? = null,
    val children: List<SDUIBlock>? = null
)

data class ChangelogSection(val title: String, val items: List<String>, val description: String? = null, val blocks: List<SDUIBlock>? = null)

sealed class MkrUpdateStatus {
    object Idle : MkrUpdateStatus()
    object Checking : MkrUpdateStatus()
    data class Available(
        val version: String,
        val changelog: List<ChangelogSection>,
        val size: String,
        val releaseDate: String,
        val description: String?,
        val imageUrl: String?,
        val apkUrl: String?
    ) : MkrUpdateStatus()

    data class NoUpdate(val version: String) : MkrUpdateStatus()
    data class Error(val message: String) : MkrUpdateStatus()
}
