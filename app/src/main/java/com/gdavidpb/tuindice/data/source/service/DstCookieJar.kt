package com.gdavidpb.tuindice.data.source.service

import com.gdavidpb.tuindice.domain.repository.StorageRepository
import com.gdavidpb.tuindice.utils.PATH_COOKIES
import com.gdavidpb.tuindice.utils.extensions.copyTo
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.io.File

open class DstCookieJar(
        private val storageRepository: StorageRepository<File>
) : CookieJar {
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val name = getCookiePath(url)

        /* Join cookies as lines */
        val inputStream = cookies.joinToString("\n").byteInputStream()

        /* Save file in local storage */

        val outputStream = storageRepository.encryptedOutputStream(name)

        inputStream.copyTo(outputStream = outputStream, autoFlush = true, autoClose = true)
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val name = getCookiePath(url)

        /* Return empty CookieJar if the file does not exist */
        return if (storageRepository.exists(name))
            storageRepository.encryptedInputStream(name)
                    .bufferedReader()
                    .readLines()
                    .mapNotNull { cookie -> Cookie.parse(url, cookie) }
        else
            listOf()
    }

    private fun getCookiePath(url: HttpUrl) = File(PATH_COOKIES, url.host).path
}