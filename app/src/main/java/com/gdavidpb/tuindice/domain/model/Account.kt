package com.gdavidpb.tuindice.domain.model

import java.util.*

data class Account(
        val uid: String,
        val id: String,
        val usbId: String,
        val email: String,
        val fullName: String,
        val firstNames: String,
        val lastNames: String,
        val careerName: String,
        val careerCode: Int,
        val scholarship: Boolean,
        val grade: Double,
        val enrolledSubjects: Int,
        val enrolledCredits: Int,
        val approvedSubjects: Int,
        val approvedCredits: Int,
        val retiredSubjects: Int,
        val retiredCredits: Int,
        val failedSubjects: Int,
        val failedCredits: Int,
        val lastUpdate: Date,
        val approvedRelation: Double,
        val appVersionCode: Int
)