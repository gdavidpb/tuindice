package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicProjectionEngine
import com.gdavidpb.tuindice.academiccore.domain.model.*
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.entity.*
import com.gdavidpb.tuindice.persistence.data.room.withImmediateTransaction
import com.gdavidpb.tuindice.record.data.mutation.AcademicRecordMutation
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordLocalDataRepository
import com.gdavidpb.tuindice.record.data.source.api.mapper.CURRENT_ACADEMIC_RECORD_ID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AcademicRecordRoomDataSource(
	private val room: TuIndiceDatabase
) : AcademicRecordLocalDataRepository {
	private val writeMutex = Mutex()

	override fun observeAcademicRecordFlow(): Flow<AcademicRecord?> {
		val officialSnapshotFlow = combine(
			room.academicTerms.observeTermsFlow(CURRENT_ACADEMIC_RECORD_ID),
			room.academicAttempts.observeAttemptsFlow(CURRENT_ACADEMIC_RECORD_ID)
		) { terms, attempts ->
			AcademicSnapshot(
				terms = terms.toAcademicTerms(
					attempts = attempts.map(AcademicAttemptEntity::toAcademicAttempt)
				)
			)
		}
		val overlayFlow = combine(
			room.academicAttemptOverrides.observeOverridesFlow(CURRENT_ACADEMIC_RECORD_ID),
			room.academicLocalTerms.observeTermsFlow(CURRENT_ACADEMIC_RECORD_ID),
			room.academicLocalAttempts.observeAttemptsFlow(CURRENT_ACADEMIC_RECORD_ID)
		) { overrides, localTerms, localAttempts ->
			AcademicOverlay(
				attemptOverrides = overrides.map(AcademicAttemptOverrideEntity::toAttemptOverride),
				syntheticTerms = localTerms.toAcademicLocalTerms(
					attempts = localAttempts.map(AcademicLocalAttemptEntity::toAcademicAttempt)
				)
			)
		}
		val officialProjectionFlow = combine(
			room.academicTermProjections.observeTermsFlow(
				CURRENT_ACADEMIC_RECORD_ID,
				ProjectionViewMode.OFFICIAL.storageValue
			),
			room.academicAttemptProjections.observeAttemptsFlow(
				CURRENT_ACADEMIC_RECORD_ID,
				ProjectionViewMode.OFFICIAL.storageValue
			)
		) { terms, attempts ->
			RecordProjection(
				viewMode = ProjectionViewMode.OFFICIAL,
				terms = terms.toTermProjections(
					attempts = attempts.map(AcademicAttemptProjectionEntity::toAttemptProjection)
				)
			)
		}
		val simulationProjectionFlow = combine(
			room.academicTermProjections.observeTermsFlow(
				CURRENT_ACADEMIC_RECORD_ID,
				ProjectionViewMode.SIMULATION.storageValue
			),
			room.academicAttemptProjections.observeAttemptsFlow(
				CURRENT_ACADEMIC_RECORD_ID,
				ProjectionViewMode.SIMULATION.storageValue
			)
		) { terms, attempts ->
			RecordProjection(
				viewMode = ProjectionViewMode.SIMULATION,
				terms = terms.toTermProjections(
					attempts = attempts.map(AcademicAttemptProjectionEntity::toAttemptProjection)
				)
			)
		}
		val partialFlow = combine(
			room.academicRecords.observeRecordFlow(),
			officialSnapshotFlow,
			overlayFlow,
			officialProjectionFlow,
			simulationProjectionFlow
		) { recordEntity, officialSnapshot, overlay, officialProjection, simulationProjection ->
			PartialRecord(
				recordEntity = recordEntity,
				officialSnapshot = officialSnapshot,
				overlay = overlay,
				officialProjection = officialProjection,
				simulationProjection = simulationProjection
			)
		}

		return combine(partialFlow, room.academicSummaries.observeSummaryFlow()) { partial, summary ->
			val recordEntity = partial.recordEntity ?: return@combine null
			AcademicRecord(
				id = recordEntity.id,
				revision = recordEntity.revision,
				officialSnapshot = partial.officialSnapshot,
				localOverlay = partial.overlay,
				officialProjection = partial.officialProjection,
				simulationProjection = partial.simulationProjection,
				summary = summary?.toRecordSummary() ?: RecordSummary(),
				updatedAtMillis = recordEntity.updatedAt
			)
		}
	}

	override suspend fun getAcademicRecord(): AcademicRecord? {
		return observeAcademicRecordFlow().first()
	}

	override suspend fun saveAcademicRecord(record: AcademicRecord) {
		writeMutex.withLock {
			persistRecord(record = record, replaceSnapshot = true)
		}
	}

	override suspend fun upsertAttemptOverride(
		attemptId: String,
		score: AttemptScore?,
		outcome: OfficialOutcome?,
		committed: Boolean
	): AcademicRecord? {
		return writeMutex.withLock {
			val current = getAcademicRecord() ?: return@withLock null
			val updatedOverrides = current.localOverlay.attemptOverrides
				.filterNot { override -> override.attemptId == attemptId } +
				AttemptOverride(
					attemptId = attemptId,
					score = score,
					outcome = outcome,
					updatedAtMillis = currentTimeMillis()
				)
			val updated = AcademicProjectionEngine.reproject(
				current.copy(
					localOverlay = current.localOverlay.copy(attemptOverrides = updatedOverrides),
					updatedAtMillis = currentTimeMillis()
				)
			)
			persistRecord(
				record = updated,
				replaceSnapshot = false,
				overrideCommitOverrides = mapOf(attemptId to committed)
			)
			updated
		}
	}

	override suspend fun deleteAttemptOverride(attemptId: String): AcademicRecord? {
		return writeMutex.withLock {
			val current = getAcademicRecord() ?: return@withLock null
			val updated = AcademicProjectionEngine.reproject(
				current.copy(
					localOverlay = current.localOverlay.copy(
						attemptOverrides = current.localOverlay.attemptOverrides.filterNot { override ->
							override.attemptId == attemptId
						}
					),
					updatedAtMillis = currentTimeMillis()
				)
			)
			persistRecord(record = updated, replaceSnapshot = false)
			updated
		}
	}

	override suspend fun addSyntheticTerm(command: AcademicRecordMutation.AddSyntheticTerm): AcademicRecord? {
		return writeMutex.withLock {
			val current = getAcademicRecord() ?: return@withLock null
			val currentSyntheticTerms = current.localOverlay.syntheticTerms.filterNot { term ->
				term.id == command.termId
			}
			val updated = AcademicProjectionEngine.reproject(
				current.copy(
					localOverlay = current.localOverlay.copy(
						syntheticTerms = currentSyntheticTerms + command.toAcademicTerm(
							order = current.officialSnapshot.terms.size + currentSyntheticTerms.size
						)
					),
					updatedAtMillis = currentTimeMillis()
				)
			)
			persistRecord(record = updated, replaceSnapshot = false)
			updated
		}
	}

	override suspend fun deleteSyntheticTerm(termId: String): AcademicRecord? {
		return writeMutex.withLock {
			val current = getAcademicRecord() ?: return@withLock null
			val updated = AcademicProjectionEngine.reproject(
				current.copy(
					localOverlay = current.localOverlay.copy(
						syntheticTerms = current.localOverlay.syntheticTerms.filterNot { term ->
							term.id == termId
						}
					),
					updatedAtMillis = currentTimeMillis()
				)
			)
			persistRecord(record = updated, replaceSnapshot = false)
			updated
		}
	}

	private suspend fun persistRecord(
		record: AcademicRecord,
		replaceSnapshot: Boolean,
		overrideCommitOverrides: Map<String, Boolean> = emptyMap()
	) {
		room.withImmediateTransaction {
			room.academicRecords.upsertEntity(record.toAcademicRecordEntity())
			if (replaceSnapshot) {
				room.academicTerms.deleteByRecord(record.id)
				room.academicAttempts.deleteByRecord(record.id)
				room.academicTerms.upsertEntities(record.officialSnapshot.terms.map { term ->
					term.toAcademicTermEntity(record.id)
				})
				room.academicAttempts.upsertEntities(record.officialSnapshot.terms.flatMap { term ->
					term.attempts.map { attempt -> attempt.toAcademicAttemptEntity(record.id) }
				})
			}

			room.academicAttemptOverrides.deleteByRecord(record.id)
			if (record.localOverlay.attemptOverrides.isNotEmpty()) {
				room.academicAttemptOverrides.upsertEntities(record.localOverlay.attemptOverrides.map { override ->
					override.toAcademicAttemptOverrideEntity(
						recordId = record.id,
						committed = overrideCommitOverrides[override.attemptId] ?: true
					)
				})
			}

			room.academicLocalTerms.deleteByRecord(record.id)
			room.academicLocalAttempts.deleteByRecord(record.id)
			if (record.localOverlay.syntheticTerms.isNotEmpty()) {
				room.academicLocalTerms.upsertEntities(record.localOverlay.syntheticTerms.map { term ->
					term.toAcademicLocalTermEntity(record.id)
				})
				room.academicLocalAttempts.upsertEntities(record.localOverlay.syntheticTerms.flatMap { term ->
					term.attempts.map { attempt -> attempt.toAcademicLocalAttemptEntity(record.id) }
				})
			}

			room.academicTermProjections.deleteByRecord(record.id)
			room.academicAttemptProjections.deleteByRecord(record.id)
			room.academicTermProjections.upsertEntities(
				record.officialProjection.terms.map { term ->
					term.toAcademicTermProjectionEntity(record.id, ProjectionViewMode.OFFICIAL)
				} + record.simulationProjection.terms.map { term ->
					term.toAcademicTermProjectionEntity(record.id, ProjectionViewMode.SIMULATION)
				}
			)
			room.academicAttemptProjections.upsertEntities(
				record.officialProjection.terms.flatMap { term ->
					term.attempts.map { attempt ->
						attempt.toAcademicAttemptProjectionEntity(record.id, ProjectionViewMode.OFFICIAL)
					}
				} + record.simulationProjection.terms.flatMap { term ->
					term.attempts.map { attempt ->
						attempt.toAcademicAttemptProjectionEntity(record.id, ProjectionViewMode.SIMULATION)
					}
				}
			)

			room.academicSummaries.upsertEntity(record.summary.toAcademicSummaryEntity(record.id))
		}
	}

	private data class PartialRecord(
		val recordEntity: AcademicRecordEntity?,
		val officialSnapshot: AcademicSnapshot,
		val overlay: AcademicOverlay,
		val officialProjection: RecordProjection,
		val simulationProjection: RecordProjection
	)
}
