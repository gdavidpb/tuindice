package com.gdavidpb.tuindice.data.model.database

import com.gdavidpb.tuindice.utils.extensions.toSubjectStatus

data class SubjectUpdate(
        val grade: Int,
        val status: Int = grade.toSubjectStatus()
)