package com.gdavidpb.tuindice.data.source.service

import com.gdavidpb.tuindice.domain.repository.LocalStorageRepository
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

open class DstCookieJar(
        private val localStorageRepository: LocalStorageRepository
) : CookieJar {
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val name = getCookiePath(url)

        /* Join cookies as lines */
        localStorageRepository.putSync(name, cookies.joinToString("\n").byteInputStream())
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val name = getCookiePath(url)

        /* Return empty CookieJar if the file does not exist */
        return (localStorageRepository.getSync(name) ?: return listOf())
                .bufferedReader()
                .readLines()
                .mapNotNull { cookie ->
                    Cookie.parse(url, cookie)
                }
    }

    private fun getCookiePath(url: HttpUrl) = "cookies/${url.host}"
}