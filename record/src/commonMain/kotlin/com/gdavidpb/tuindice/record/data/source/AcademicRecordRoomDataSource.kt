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
				curriculumKey = recordEntity.curriculumKey,
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

private fun List<AcademicTermEntity>.toAcademicTerms(attempts: List<AcademicAttempt>): List<AcademicTerm> {
	val attemptsByTermId = attempts.groupBy(AcademicAttempt::termId)
	return sortedWith(
		compareByDescending<AcademicTermEntity> { it.startAt }
			.thenBy { it.id }
	).map { term ->
		AcademicTerm(
			id = term.id,
			label = term.label,
			startAtMillis = term.startAt,
			endAtMillis = term.endAt,
			order = term.order,
			current = term.current,
			closed = term.closed,
			editable = term.editable,
			synthetic = term.synthetic,
			sourceReportedPeriodAverage = term.sourceReportedPeriodAverage,
			sourceReportedCumulativeAverage = term.sourceReportedCumulativeAverage,
			attempts = attemptsByTermId[term.id].orEmpty()
				.sortedWith(
					compareBy<AcademicAttempt> { it.sequenceInTerm }
						.thenBy { it.id }
				)
		)
	}
}

private fun List<AcademicLocalTermEntity>.toAcademicLocalTerms(attempts: List<AcademicAttempt>): List<AcademicTerm> {
	val attemptsByTermId = attempts.groupBy(AcademicAttempt::termId)
	return sortedWith(
		compareByDescending<AcademicLocalTermEntity> { it.startAt }
			.thenBy { it.id }
	).map { term ->
		AcademicTerm(
			id = term.id,
			label = term.label,
			startAtMillis = term.startAt,
			endAtMillis = term.endAt,
			order = term.order,
			current = false,
			closed = term.closed,
			editable = term.editable,
			synthetic = term.synthetic,
			attempts = attemptsByTermId[term.id].orEmpty()
				.sortedWith(
					compareBy<AcademicAttempt> { it.sequenceInTerm }
						.thenBy { it.id }
				)
		)
	}
}

private fun List<AcademicTermProjectionEntity>.toTermProjections(
	attempts: List<AttemptProjection>
): List<TermProjection> {
	val attemptsByTermId = attempts.groupBy(AttemptProjection::termId)
	return sortedWith(
		compareByDescending<AcademicTermProjectionEntity> { it.startAt }
			.thenBy { it.id }
	).map { term ->
		TermProjection(
			id = term.id,
			label = term.label,
			startAtMillis = term.startAt,
			endAtMillis = term.endAt,
			order = term.order,
			current = term.current,
			closed = term.closed,
			editable = term.editable,
			synthetic = term.synthetic,
			grade = term.grade,
			gradeSum = term.gradeSum,
			credits = term.credits,
			creditsSum = term.creditsSum,
			attempts = attemptsByTermId[term.id].orEmpty()
				.sortedWith(
					compareBy<AttemptProjection> { it.sequenceInTerm }
						.thenBy { it.id }
				)
		)
	}
}

private fun AcademicRecord.toAcademicRecordEntity(): AcademicRecordEntity {
	return AcademicRecordEntity(
		id = id,
		revision = revision,
		curriculumKey = curriculumKey,
		updatedAt = updatedAtMillis
	)
}

private fun AcademicTerm.toAcademicTermEntity(recordId: String): AcademicTermEntity {
	return AcademicTermEntity(
		id = id,
		recordId = recordId,
		label = label,
		startAt = startAtMillis,
		endAt = endAtMillis,
		order = order,
		current = current,
		closed = closed,
		editable = editable,
		synthetic = synthetic,
		sourceReportedPeriodAverage = sourceReportedPeriodAverage,
		sourceReportedCumulativeAverage = sourceReportedCumulativeAverage
	)
}

private fun AcademicAttempt.toAcademicAttemptEntity(recordId: String): AcademicAttemptEntity {
	return AcademicAttemptEntity(
		id = id,
		recordId = recordId,
		termId = termId,
		subjectCode = subjectCode,
		subjectName = subjectName,
		credits = credits,
		sequenceInTerm = sequenceInTerm,
		gradingMode = gradingMode.name,
		rawGradeToken = rawGradeToken,
		rawObservationText = rawObservationText,
		scoreKind = officialScore.kind.name,
		scoreNumericValue = officialScore.numericValue,
		scoreSymbolicValue = officialScore.symbolicValue,
		officialOutcome = officialOutcome.name,
		officialBadge = officialBadge.name,
		editable = editable,
		source = source.name,
		synthetic = synthetic
	)
}

private fun AttemptOverride.toAcademicAttemptOverrideEntity(
	recordId: String,
	committed: Boolean
): AcademicAttemptOverrideEntity {
	return AcademicAttemptOverrideEntity(
		attemptId = attemptId,
		recordId = recordId,
		scoreKind = score?.kind?.name,
		scoreNumericValue = score?.numericValue,
		scoreSymbolicValue = score?.symbolicValue,
		outcome = outcome?.name,
		updatedAt = updatedAtMillis,
		committed = committed
	)
}

private fun AcademicTerm.toAcademicLocalTermEntity(recordId: String): AcademicLocalTermEntity {
	return AcademicLocalTermEntity(
		id = id,
		recordId = recordId,
		label = label,
		startAt = startAtMillis,
		endAt = endAtMillis,
		order = order,
		closed = closed,
		editable = editable,
		synthetic = synthetic
	)
}

private fun AcademicAttempt.toAcademicLocalAttemptEntity(recordId: String): AcademicLocalAttemptEntity {
	return AcademicLocalAttemptEntity(
		id = id,
		recordId = recordId,
		termId = termId,
		subjectCode = subjectCode,
		subjectName = subjectName,
		credits = credits,
		sequenceInTerm = sequenceInTerm,
		gradingMode = gradingMode.name,
		rawGradeToken = rawGradeToken,
		rawObservationText = rawObservationText,
		scoreKind = officialScore.kind.name,
		scoreNumericValue = officialScore.numericValue,
		scoreSymbolicValue = officialScore.symbolicValue,
		officialOutcome = officialOutcome.name,
		officialBadge = officialBadge.name,
		editable = editable,
		source = source.name,
		synthetic = synthetic
	)
}

private fun TermProjection.toAcademicTermProjectionEntity(
	recordId: String,
	viewMode: ProjectionViewMode
): AcademicTermProjectionEntity {
	return AcademicTermProjectionEntity(
		viewMode = viewMode.storageValue,
		id = id,
		recordId = recordId,
		label = label,
		startAt = startAtMillis,
		endAt = endAtMillis,
		order = order,
		current = current,
		closed = closed,
		editable = editable,
		synthetic = synthetic,
		grade = grade,
		gradeSum = gradeSum,
		credits = credits,
		creditsSum = creditsSum
	)
}

private fun AttemptProjection.toAcademicAttemptProjectionEntity(
	recordId: String,
	viewMode: ProjectionViewMode
): AcademicAttemptProjectionEntity {
	return AcademicAttemptProjectionEntity(
		viewMode = viewMode.storageValue,
		id = id,
		recordId = recordId,
		termId = termId,
		subjectCode = subjectCode,
		subjectName = subjectName,
		credits = credits,
		sequenceInTerm = sequenceInTerm,
		gradingMode = gradingMode.name,
		rawGradeToken = rawGradeToken,
		rawObservationText = rawObservationText,
		scoreKind = score.kind.name,
		scoreNumericValue = score.numericValue,
		scoreSymbolicValue = score.symbolicValue,
		outcome = outcome.name,
		badge = badge.name,
		editable = editable,
		synthetic = synthetic,
		countsTowardTermAverage = countsTowardTermAverage,
		countsTowardCumulativeAverage = countsTowardCumulativeAverage
	)
}

private fun RecordSummary.toAcademicSummaryEntity(recordId: String): AcademicSummaryEntity {
	return AcademicSummaryEntity(
		recordId = recordId,
		officialGrade = official.grade,
		officialEnrolledSubjects = official.enrolledSubjects,
		officialEnrolledCredits = official.enrolledCredits,
		officialApprovedSubjects = official.approvedSubjects,
		officialApprovedCredits = official.approvedCredits,
		officialApprovedRelation = official.approvedRelation,
		officialRetiredSubjects = official.retiredSubjects,
		officialRetiredCredits = official.retiredCredits,
		officialRetiredRelation = official.retiredRelation,
		officialFailedSubjects = official.failedSubjects,
		officialFailedCredits = official.failedCredits,
		officialFailedRelation = official.failedRelation,
		officialWithoutEffectAttempts = official.withoutEffectAttempts,
		simulationGrade = simulation.grade,
		simulationEnrolledSubjects = simulation.enrolledSubjects,
		simulationEnrolledCredits = simulation.enrolledCredits,
		simulationApprovedSubjects = simulation.approvedSubjects,
		simulationApprovedCredits = simulation.approvedCredits,
		simulationApprovedRelation = simulation.approvedRelation,
		simulationRetiredSubjects = simulation.retiredSubjects,
		simulationRetiredCredits = simulation.retiredCredits,
		simulationRetiredRelation = simulation.retiredRelation,
		simulationFailedSubjects = simulation.failedSubjects,
		simulationFailedCredits = simulation.failedCredits,
		simulationFailedRelation = simulation.failedRelation,
		simulationWithoutEffectAttempts = simulation.withoutEffectAttempts
	)
}

private fun AcademicAttemptEntity.toAcademicAttempt(): AcademicAttempt {
	return AcademicAttempt(
		id = id,
		termId = termId,
		subjectCode = subjectCode,
		subjectName = subjectName,
		credits = credits,
		sequenceInTerm = sequenceInTerm,
		gradingMode = AttemptGradingMode.valueOf(gradingMode),
		rawGradeToken = rawGradeToken,
		rawObservationText = rawObservationText,
		officialScore = AttemptScore(
			kind = AttemptScoreKind.valueOf(scoreKind),
			numericValue = scoreNumericValue,
			symbolicValue = scoreSymbolicValue
		),
		officialOutcome = OfficialOutcome.valueOf(officialOutcome),
		officialBadge = HistoricalBadge.valueOf(officialBadge),
		editable = editable,
		source = AttemptSource.valueOf(source),
		synthetic = synthetic
	)
}

private fun AcademicLocalAttemptEntity.toAcademicAttempt(): AcademicAttempt {
	return AcademicAttempt(
		id = id,
		termId = termId,
		subjectCode = subjectCode,
		subjectName = subjectName,
		credits = credits,
		sequenceInTerm = sequenceInTerm,
		gradingMode = AttemptGradingMode.valueOf(gradingMode),
		rawGradeToken = rawGradeToken,
		rawObservationText = rawObservationText,
		officialScore = AttemptScore(
			kind = AttemptScoreKind.valueOf(scoreKind),
			numericValue = scoreNumericValue,
			symbolicValue = scoreSymbolicValue
		),
		officialOutcome = OfficialOutcome.valueOf(officialOutcome),
		officialBadge = HistoricalBadge.valueOf(officialBadge),
		editable = editable,
		source = AttemptSource.valueOf(source),
		synthetic = synthetic
	)
}

private fun AcademicAttemptOverrideEntity.toAttemptOverride(): AttemptOverride {
	return AttemptOverride(
		attemptId = attemptId,
		score = scoreKind?.let { kind ->
			AttemptScore(
				kind = AttemptScoreKind.valueOf(kind),
				numericValue = scoreNumericValue,
				symbolicValue = scoreSymbolicValue
			)
		},
		outcome = outcome?.let(OfficialOutcome::valueOf),
		updatedAtMillis = updatedAt
	)
}

private fun AcademicAttemptProjectionEntity.toAttemptProjection(): AttemptProjection {
	return AttemptProjection(
		id = id,
		termId = termId,
		subjectCode = subjectCode,
		subjectName = subjectName,
		credits = credits,
		sequenceInTerm = sequenceInTerm,
		gradingMode = AttemptGradingMode.valueOf(gradingMode),
		rawGradeToken = rawGradeToken,
		rawObservationText = rawObservationText,
		score = AttemptScore(
			kind = AttemptScoreKind.valueOf(scoreKind),
			numericValue = scoreNumericValue,
			symbolicValue = scoreSymbolicValue
		),
		outcome = OfficialOutcome.valueOf(outcome),
		badge = HistoricalBadge.valueOf(badge),
		editable = editable,
		synthetic = synthetic,
		countsTowardTermAverage = countsTowardTermAverage,
		countsTowardCumulativeAverage = countsTowardCumulativeAverage
	)
}

private fun AcademicSummaryEntity.toRecordSummary(): RecordSummary {
	return RecordSummary(
		official = ProjectionSummary(
			grade = officialGrade,
			enrolledSubjects = officialEnrolledSubjects,
			enrolledCredits = officialEnrolledCredits,
			approvedSubjects = officialApprovedSubjects,
			approvedCredits = officialApprovedCredits,
			approvedRelation = officialApprovedRelation,
			retiredSubjects = officialRetiredSubjects,
			retiredCredits = officialRetiredCredits,
			retiredRelation = officialRetiredRelation,
			failedSubjects = officialFailedSubjects,
			failedCredits = officialFailedCredits,
			failedRelation = officialFailedRelation,
			withoutEffectAttempts = officialWithoutEffectAttempts
		),
		simulation = ProjectionSummary(
			grade = simulationGrade,
			enrolledSubjects = simulationEnrolledSubjects,
			enrolledCredits = simulationEnrolledCredits,
			approvedSubjects = simulationApprovedSubjects,
			approvedCredits = simulationApprovedCredits,
			approvedRelation = simulationApprovedRelation,
			retiredSubjects = simulationRetiredSubjects,
			retiredCredits = simulationRetiredCredits,
			retiredRelation = simulationRetiredRelation,
			failedSubjects = simulationFailedSubjects,
			failedCredits = simulationFailedCredits,
			failedRelation = simulationFailedRelation,
			withoutEffectAttempts = simulationWithoutEffectAttempts
		)
	)
}

private fun AcademicRecordMutation.AddSyntheticTerm.toAcademicTerm(order: Int): AcademicTerm {
	return AcademicTerm(
		id = termId,
		label = label,
		startAtMillis = startAtMillis,
		endAtMillis = endAtMillis,
		order = order,
		closed = false,
		editable = true,
		synthetic = true,
		attempts = attempts.mapIndexed { index, attempt ->
			AcademicAttempt(
				id = attempt.attemptId,
				termId = termId,
				subjectCode = attempt.subjectCode,
				subjectName = attempt.subjectName,
				credits = attempt.credits,
				sequenceInTerm = index,
				gradingMode = attempt.gradingMode,
				officialScore = attempt.score ?: AttemptScore.empty(),
				officialOutcome = attempt.outcome ?: OfficialOutcome.PENDING,
				officialBadge = HistoricalBadge.NONE,
				editable = true,
				source = AttemptSource.LOCAL,
				synthetic = true
			)
		}
	)
}

private val ProjectionViewMode.storageValue: String
	get() = when (this) {
		ProjectionViewMode.OFFICIAL -> "official"
		ProjectionViewMode.SIMULATION -> "simulation"
	}
