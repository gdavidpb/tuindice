package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One weekly meeting of an attempt. [dayOfWeek] follows the backend (1 = Sunday ... 7 = Saturday)
 * and blocks are the university's class blocks, not clock hours. [classroom] is empty when the
 * university did not assign one.
 */
@Serializable
data class AcademicScheduleEntry(
	@SerialName("day_of_week") val dayOfWeek: Int,
	@SerialName("start_block") val startBlock: Int,
	@SerialName("end_block") val endBlock: Int,
	val classroom: String = ""
)
