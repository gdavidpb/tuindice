package com.gdavidpb.tuindice.data.source.service

import com.gdavidpb.tuindice.domain.model.exception.SynchronizationException
import com.gdavidpb.tuindice.utils.HEADER_DATE
import com.gdavidpb.tuindice.utils.extensions.bodyToString
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.jsoup.Jsoup
import java.util.*
import kotlin.math.abs

open class DstAuthInterceptor(
        private val syncTime: Long
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request).checkSynchronized()
        val responseBody = response.body
        val document = Jsoup.parse(responseBody?.string() ?: "")
        val ltInput = document.selectFirst("input[name=lt]")
        val accessKey = ltInput?.attr("value")

        responseBody?.close()

        return if (accessKey != null) {
            val body = request.body.bodyToString()
            val mediaType = "application/x-www-form-urlencoded;charset=UTF-8".toMediaTypeOrNull()
            val requestBody = "$body&lt=$accessKey".toRequestBody(mediaType)

            val authRequest = request
                    .newBuilder()
                    .addHeader("Accept-Language", "es-VE")
                    .post(requestBody)
                    .build()

            chain.proceed(authRequest)
        } else {
            chain.proceed(request)
        }
    }

    private fun Response.checkSynchronized() = apply {
        headers.getDate(HEADER_DATE)?.also { remoteDate ->
            val localDate = Date()
            val deltaTime = abs(localDate.time - remoteDate.time)

            if (deltaTime > syncTime) throw SynchronizationException()
        }
    }
}