package com.gdavidpb.tuindice.domain.model

import java.util.*

data class Account(
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
        val profilePicture: String = "",
        val hasProfilePicture: Boolean = false,
        val enrolledSubjects: Int = 0,
        val enrolledCredits: Int = 0,
        val approvedSubjects: Int = 0,
        val approvedCredits: Int = 0,
        val retiredSubjects: Int = 0,
        val retiredCredits: Int = 0,
        val failedSubjects: Int = 0,
        val failedCredits: Int = 0,
        val lastUpdate: Date = Date(),
        val appVersionCode: Int = 0
)