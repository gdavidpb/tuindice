package com.gdavidpb.tuindice.data.model.database

data class AccountEntity(
        val id: String = "",
        val usbId: String = "",
        val token: String = "",
        val email: String = "",
        val fullName: String = "",
        val firstNames: String = "",
        val lastNames: String = "",
        val careerName: String = "",
        val photoUrl: String = "",
        val careerCode: Int = 0,
        val scholarship: Boolean = false
)