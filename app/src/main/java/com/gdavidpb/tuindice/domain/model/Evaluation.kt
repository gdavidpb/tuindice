package com.gdavidpb.tuindice.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Evaluation(
        val id: String,
        val sid: String,
        val subjectCode: String,
        val type: EvaluationType,
        val grade: Double,
        val maxGrade: Double,
        val date: Date,
        val notes: String,
        val isDone: Boolean
) : Parcelable