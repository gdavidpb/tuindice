package com.gdavidpb.tuindice.data.source.service

import android.content.SharedPreferences
import com.gdavidpb.tuindice.data.utils.edit
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

open class DstCookieJar(
        private val preferences: SharedPreferences
) : CookieJar {
    override fun saveFromResponse(url: HttpUrl, cookies: MutableList<Cookie>) {
        preferences.edit {
            putStringSet(url.host(), cookies.map { cookie -> cookie.toString() }.toSet())
        }
    }

    override fun loadForRequest(url: HttpUrl): MutableList<Cookie> {
        return preferences.getStringSet(url.host(), null)?.mapNotNull { cookie ->
            Cookie.parse(url, cookie)
        }?.toMutableList() ?: mutableListOf()
    }
}