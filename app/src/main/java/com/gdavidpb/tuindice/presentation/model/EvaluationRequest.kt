package com.gdavidpb.tuindice.presentation.model

import android.os.Parcelable
import com.gdavidpb.tuindice.domain.model.Evaluation
import com.gdavidpb.tuindice.domain.model.Subject
import kotlinx.android.parcel.Parcelize

@Parcelize
data class EvaluationRequest(
        val subject: Subject,
        val evaluation: Evaluation? = null
) : Parcelable