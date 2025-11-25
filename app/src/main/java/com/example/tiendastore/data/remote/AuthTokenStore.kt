package com.example.tiendastore.data.remote

import android.content.Context

object AuthTokenStore {
    private const val PREFS = "auth_prefs"
    private const val KEY_TOKEN = "token"

    private var cached: String? = null

    fun init(context: Context) {
        if (cached == null) {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            cached = prefs.getString(KEY_TOKEN, null)
        }
    }

    fun get(): String? = cached

    fun set(context: Context, token: String?) {
        cached = token
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }
}
