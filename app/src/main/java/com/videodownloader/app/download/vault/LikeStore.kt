package com.videodownloader.app.download.vault

import android.content.Context

/** Tiny persistent store of "liked" vault items, keyed by file path. */
object LikeStore {
    private const val PREF = "vault_likes"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    fun isLiked(context: Context, path: String): Boolean =
        prefs(context).getBoolean(path, false)

    fun setLiked(context: Context, path: String, liked: Boolean) {
        prefs(context).edit().apply {
            if (liked) putBoolean(path, true) else remove(path)
        }.apply()
    }
}
