package com.gdavidpb.tuindice.domain.model

data class Account(
        val id: String,
        val usbId: String,
        val password: String,
        val firstNames: String,
        val lastNames: String,
        val career: Career,
        val record: Record,
        val scholarship: Boolean
)