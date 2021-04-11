package com.gdavidpb.tuindice.data.source.service

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

open class DstCookieJar : CookieJar {
    private val cookieJar = mutableListOf<Cookie>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieJar.addAll(cookies)
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieJar.filter { cookie -> cookie.matches(url) }
    }
}