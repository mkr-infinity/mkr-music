/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.extensions

import androidx.sqlite.db.SimpleSQLiteQuery
import java.net.InetSocketAddress
import java.net.InetSocketAddress.createUnresolved

inline fun <reified T : Enum<T>> String?.toEnum(defaultValue: T): T =
    if (this == null) {
        defaultValue
    } else {
        try {
            enumValueOf(this)
        } catch (e: IllegalArgumentException) {
            defaultValue
        }
    }

fun String.toSQLiteQuery(): SimpleSQLiteQuery = SimpleSQLiteQuery(this)

fun String.toInetSocketAddress(): InetSocketAddress {
    val clean = removePrefix("http://").removePrefix("https://").trim()
    val lastColon = clean.lastIndexOf(':')
    if (lastColon <= 0 || lastColon == clean.length - 1) {
        throw IllegalArgumentException("Invalid proxy address format: $this")
    }
    val host = clean.substring(0, lastColon).removePrefix("[").removeSuffix("]")
    val port = clean.substring(lastColon + 1).toIntOrNull()
        ?: throw IllegalArgumentException("Invalid proxy port in: $this")
    return createUnresolved(host, port)
}
