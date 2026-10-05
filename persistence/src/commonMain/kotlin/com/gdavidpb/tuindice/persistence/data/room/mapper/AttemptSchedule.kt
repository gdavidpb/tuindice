package com.gdavidpb.tuindice.persistence.data.room.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicScheduleEntry
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

// The schedule and the enrollment errors are stored as JSON text in one column each: they are only
// ever read back whole, never queried.
private val scheduleSerializer = ListSerializer(AcademicScheduleEntry.serializer())
private val enrollmentErrorsSerializer = ListSerializer(String.serializer())

internal fun encodeSchedule(schedule: List<AcademicScheduleEntry>): String =
	Json.encodeToString(scheduleSerializer, schedule)

internal fun decodeSchedule(json: String): List<AcademicScheduleEntry> =
	Json.decodeFromString(scheduleSerializer, json)

internal fun encodeEnrollmentErrors(errors: List<String>): String =
	Json.encodeToString(enrollmentErrorsSerializer, errors)

internal fun decodeEnrollmentErrors(json: String): List<String> =
	Json.decodeFromString(enrollmentErrorsSerializer, json)
