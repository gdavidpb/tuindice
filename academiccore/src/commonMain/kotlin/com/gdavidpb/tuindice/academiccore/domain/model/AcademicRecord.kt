package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AcademicRecord(
	val id: String,
	val revision: Long = 0L,
	val profile: AcademicProfile = AcademicProfile(),
	@SerialName("curriculum_key") val curriculumKey: String = "",
	@SerialName("official_snapshot") val officialSnapshot: AcademicSnapshot = AcademicSnapshot(),
	@SerialName("local_overlay") val localOverlay: AcademicOverlay = AcademicOverlay(),
	@SerialName("official_projection") val officialProjection: RecordProjection = RecordProjection(
		viewMode = ProjectionViewMode.OFFICIAL
	),
	@SerialName("simulation_projection") val simulationProjection: RecordProjection = RecordProjection(
		viewMode = ProjectionViewMode.SIMULATION
	),
	val summary: RecordSummary = RecordSummary(),
	@SerialName("sync_meta") val syncMeta: RecordSyncMeta = RecordSyncMeta(),
	@SerialName("updated_at") val updatedAtMillis: Long = 0L
)
