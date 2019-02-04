package com.gdavidpb.tuindice.domain.model

data class Account(
        val id: String = "",
        val usbId: String = "",
        val token: String = "",
        val email: String = "",
        val fullName: String = "",
        val firstNames: String = "",
        val lastNames: String = "",
        val scholarship: Boolean = false
)