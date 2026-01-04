package com.petcare.app.data.api

import android.content.Context

object TokenPrefs {
    private const val PREF = "petcare_prefs"
    private const val KEY_TOKEN = "token"

    fun save(context: Context, token: String) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TOKEN, token)
            .apply()
    }

    fun get(context: Context): String? {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_TOKEN, null)
            ?.takeIf { it.isNotBlank() }
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_TOKEN)
            .apply()
    }
}
