package com.music.mkr.constants

internal enum class MiniPlayerDesignOption {
    CLASSIC,
    NEW,
    APPLE;

    companion object {
        const val DefaultUseAppleMiniPlayer = false
        const val DefaultUseNewMiniPlayerDesign = false

        fun fromPreferences(
            useAppleMiniPlayer: Boolean = DefaultUseAppleMiniPlayer,
            useNewMiniPlayerDesign: Boolean = DefaultUseNewMiniPlayerDesign,
        ): MiniPlayerDesignOption = when {
            useAppleMiniPlayer -> APPLE
            useNewMiniPlayerDesign -> NEW
            else -> CLASSIC
        }
    }
}
