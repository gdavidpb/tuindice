package com.gdavidpb.tuindice.utils

import java.security.MessageDigest

class DigestConcat(algorithm: String) {
    private val messages = arrayListOf<ByteArray>()
    private val messageDigest = MessageDigest.getInstance(algorithm)

    private fun concat(data: ByteArray): DigestConcat {
        val hash = messageDigest.digest(data)

        messages.add(hash)

        messageDigest.reset()

        return this
    }

    fun concat(data: Long) = concat(data.bytes())

    fun concat(data: String) = concat(data.toByteArray())

    fun build(): ByteArray {
        messages.forEach(messageDigest::update)

        val hash = messageDigest.digest()

        messageDigest.reset()

        messages.clear()

        return hash
    }
}