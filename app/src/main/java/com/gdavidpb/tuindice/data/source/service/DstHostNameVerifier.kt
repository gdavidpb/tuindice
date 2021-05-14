package com.gdavidpb.tuindice.data.source.service

import com.gdavidpb.tuindice.BuildConfig
import java.net.URL
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

open class DstHostNameVerifier : HostnameVerifier {
    private val verifiedHostNames = arrayOf(
            URL(BuildConfig.ENDPOINT_DST_SECURE).host,
            URL(BuildConfig.ENDPOINT_DST_RECORD).host,
            URL(BuildConfig.ENDPOINT_DST_ENROLLMENT).host
    )

    override fun verify(hostname: String, session: SSLSession): Boolean {
        return verifiedHostNames.contains(hostname) && session.isValid
    }
}