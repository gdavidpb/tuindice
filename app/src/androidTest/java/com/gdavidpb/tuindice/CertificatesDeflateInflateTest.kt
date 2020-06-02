package com.gdavidpb.tuindice

import android.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.zip.DeflaterInputStream
import java.util.zip.InflaterInputStream

class CertificatesDeflateInflateTest {

    @Test
    fun shouldDeflateInflateCertificates() {
        val certificates = CERT_DATA
                .replace(CERT_SEPARATOR, "*")
                .replace(CERT_HEADER, "")
                .replace(CERT_FOOTER, "")
                .replace("\n", "")
                .split("*")
                .map { Base64.decode(it, Base64.NO_WRAP) }
                .reduce { acc, bytes -> acc + bytes }
                .let(::ByteArrayInputStream)
                .let(::DeflaterInputStream)
                .use { stream -> stream.readBytes() }
                .let { bytes -> Base64.encodeToString(bytes, Base64.DEFAULT) }
                .trim()
                .let { Base64.decode(it, Base64.DEFAULT) }
                .let(::ByteArrayInputStream)
                .let(::InflaterInputStream)
                .use { inputStream ->
                    CertificateFactory.getInstance("X.509")
                            .generateCertificates(inputStream)
                            .map { certificate -> certificate as X509Certificate }
                }

        assertEquals(CERT_COUNT, certificates.size)
    }
}
