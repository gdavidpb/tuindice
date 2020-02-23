package com.gdavidpb.tuindice.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Subject(
        val id: String,
        val qid: String,
        val code: String,
        val name: String,
        val credits: Int,
        val grade: Int,
        val status: Int
) : Parcelable