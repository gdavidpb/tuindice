package com.gdavidpb.tuindice.data.source.service

import com.gdavidpb.tuindice.utils.extensions.toString
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.jsoup.Jsoup

open class DstAuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        val responseBody = response.body
        val document = Jsoup.parse(responseBody?.string() ?: "")
        val ltInput = document.selectFirst("input[name=lt]")
        val accessKey = ltInput?.attr("value")

        responseBody?.close()

        return if (accessKey != null) {
            val body = request.body.toString()
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
}