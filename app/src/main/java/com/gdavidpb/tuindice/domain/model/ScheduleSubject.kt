package com.gdavidpb.tuindice.domain.model

data class ScheduleSubject(
        val code: String,
        val section: Int,
        val name: String,
        val credits: Int,
        val status: String,
        val schedule: List<ScheduleEntry>
)