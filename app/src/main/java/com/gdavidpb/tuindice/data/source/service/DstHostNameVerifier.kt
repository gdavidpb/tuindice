package com.gdavidpb.tuindice.data.source.service

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

open class DstHostNameVerifier : HostnameVerifier {

    private val verifiedHostNames = arrayOf(
            "expediente.dii.usb.ve",
            "comprobante.dii.usb.ve",
            "secure.dst.usb.ve"
    )

    override fun verify(hostname: String, session: SSLSession): Boolean {
        return verifiedHostNames.contains(hostname) && session.isValid
    }
}