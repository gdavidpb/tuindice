package com.gdavidpb.tuindice.data.model.service

data class DstScheduledSubject(
        val code: String,
        val section: Int,
        val name: String,
        val credits: Int,
        val status: String,
        val schedule: List<DstScheduleEntry>
)