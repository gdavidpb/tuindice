package com.gdavidpb.tuindice.data.source.service

import com.gdavidpb.tuindice.utils.extensions.bodyToString
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.jsoup.Jsoup

open class DstAuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val firstRequest = chain.request()
        val interceptedRequest = chain.proceed(firstRequest)
        val interceptedBody = interceptedRequest.body
        val interceptedResponse = Jsoup.parse(interceptedBody?.string() ?: "")
        val ltInput = interceptedResponse.selectFirst("input[name=lt]")
        val accessKey = ltInput?.attr("value")

        interceptedBody?.close()

        return if (accessKey != null) {
            val firstBody = firstRequest.body.bodyToString()
            val mediaType = "application/x-www-form-urlencoded;charset=UTF-8".toMediaTypeOrNull()
            val lastBody = "$firstBody&lt=$accessKey".toRequestBody(mediaType)

            val lastRequest = firstRequest
                    .newBuilder()
                    .addHeader("Accept-Language", "es-VE")
                    .post(lastBody)
                    .build()

            chain.proceed(lastRequest)
        } else {
            chain.proceed(firstRequest)
        }
    }
}