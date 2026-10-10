package com.music.mkr.utils

fun normalizeDataSyncId(raw: String?): String? {
    if (raw == null) return null
    if (!raw.contains("||")) return raw
    val delegated = raw.substringAfter("||")
    return delegated.ifEmpty { raw.substringBefore("||") }
}
