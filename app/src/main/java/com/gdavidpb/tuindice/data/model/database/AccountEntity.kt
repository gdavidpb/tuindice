package com.gdavidpb.tuindice.data.model.database

data class AccountEntity(
        val uid: String = "",
        val id: String = "",
        val usbId: String = "",
        val email: String = "",
        val fullName: String = "",
        val firstNames: String = "",
        val lastNames: String = "",
        val careerName: String = "",
        val careerCode: Int = 0,
        val scholarship: Boolean = false,
        val grade: Double = 0.0,
        val photoUrl: String = "",
        val enrolledSubjects: Int = 0,
        val enrolledCredits: Int = 0,
        val approvedSubjects: Int = 0,
        val approvedCredits: Int = 0,
        val retiredSubjects: Int = 0,
        val retiredCredits: Int = 0,
        val failedSubjects: Int = 0,
        val failedCredits: Int = 0
)