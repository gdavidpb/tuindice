package com.gdavidpb.tuindice.data.source.functions.responses

import com.google.gson.annotations.SerializedName

data class QuarterResponse(
	@SerializedName("id") val id: String,
	@SerializedName("start_date") val startDate: Long,
	@SerializedName("end_date") val endDate: Long,
	@SerializedName("grade") val grade: Double,
	@SerializedName("grade_sum") val gradeSum: Double,
	@SerializedName("credits") val credits: Int,
	@SerializedName("status") val status: Int,
	@SerializedName("is_mock") val isMock: Boolean,
	@SerializedName("subjects") val subjects: List<SubjectResponse>
)