package com.gdavidpb.tuindice.utils

import com.gdavidpb.tuindice.utils.extensions.bytes
import java.io.ByteArrayOutputStream
import java.security.MessageDigest

class DigestConcat(algorithm: String) {
    private val messageOutput = ByteArrayOutputStream()
    private val messageDigest = MessageDigest.getInstance(algorithm)

    private fun concat(data: ByteArray) = apply {
        val hash = messageDigest.digest(data)

        messageOutput.write(hash)

        messageDigest.reset()
    }

    fun concat(data: Long) = concat(data.bytes())

    fun concat(data: String) = concat(data.toByteArray())

    fun build(): ByteArray {
        val data = messageOutput.toByteArray()
        val hash = messageDigest.digest(data)

        messageDigest.reset()
        messageOutput.reset()

        return hash
    }
}