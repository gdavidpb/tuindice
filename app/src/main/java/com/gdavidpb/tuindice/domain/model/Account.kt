package com.gdavidpb.tuindice.domain.model

data class Account(
        val id: String = "",
        val usbId: String = "",
        val email: String = "",
        val fullName: String = "",
        val firstNames: String = "",
        val lastNames: String = "",
        val careerName: String = "",
        val careerCode: Int = 0,
        val scholarship: Boolean = false
)