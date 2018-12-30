package com.gdavidpb.tuindice.domain.model

data class Account(
        val id: String = "",
        val usbId: String = "",
        val password: String = "",
        val firstNames: String = "",
        val lastNames: String = "",
        val career: Career = Career(),
        val record: Record = Record(),
        val scholarship: Boolean = false
)