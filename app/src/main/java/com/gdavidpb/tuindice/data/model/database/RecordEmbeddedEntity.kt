package com.gdavidpb.tuindice.data.model.database

data class RecordEmbeddedEntity(
    val failedCredits: Int = 0,
    val failedSubjects: Int = 0,
    val retiredCredits: Int = 0,
    val retiredSubjects: Int = 0,
    val approvedCredits: Int = 0,
    val approvedSubject: Int = 0,
    val enrolledCredits: Int = 0,
    val enrolledSubjects: Int = 0
)