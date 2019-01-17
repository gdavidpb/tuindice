package com.gdavidpb.tuindice.domain.model

data class Account(
        val id: String = "",
        val usbId: String = "",
        val email: String = "",
        val password: String = "",
        val fullName: String = "",
        val shortName: String = "",
        val firstNames: String = "",
        val lastNames: String = "",
        val scholarship: Boolean = false
)