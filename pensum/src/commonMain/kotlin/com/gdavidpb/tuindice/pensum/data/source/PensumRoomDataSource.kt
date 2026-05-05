package com.gdavidpb.tuindice.pensum.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.pensum.data.mapper.cacheKey
import com.gdavidpb.tuindice.pensum.data.model.GetPensumResponse
import com.gdavidpb.tuindice.pensum.data.repository.PensumLocalDataRepository
import com.gdavidpb.tuindice.pensum.domain.model.AcademicPensumSnapshot
import com.gdavidpb.tuindice.pensum.domain.model.PensumSelectionParams
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicAttemptDao
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicTermDao
import com.gdavidpb.tuindice.persistence.data.room.daos.PensumCacheDao
import com.gdavidpb.tuindice.persistence.data.room.daos.PensumSelectionDao
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicAttemptEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicTermEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.PensumCacheEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.PensumSelectionEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.PensumSelectionTable
import com.gdavidpb.tuindice.persistence.domain.repository.PersistenceTransactionRunner
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PensumRoomDataSource(
	private val pensumCacheDao: PensumCacheDao,
	private val pensumSelectionDao: PensumSelectionDao,
	private val academicTermDao: AcademicTermDao,
	private val academicAttemptDao: AcademicAttemptDao,
	private val transactionRunner: PersistenceTransactionRunner,
	private val json: Json
) : PensumLocalDataRepository {
	private val writeMutex = Mutex()

	@OptIn(ExperimentalCoroutinesApi::class)
	override fun observePensumResponseFlow(): Flow<GetPensumResponse?> {
		return pensumSelectionDao.observeSelection()
			.flatMapLatest { selection ->
				val cacheKey = selection?.cacheKey
				if (cacheKey == null) {
					flowOf(null)
				} else {
					pensumCacheDao.observePensum(cacheKey)
						.map { entity -> entity?.toResponse() }
				}
			}
	}

	override fun observeAcademicSnapshotFlow(): Flow<AcademicPensumSnapshot> {
		return combine(
			academicTermDao.observeTermsFlow(),
			academicAttemptDao.observeAttemptsFlow()
		) { terms, attempts ->
			terms.toAcademicSnapshot(attempts)
		}
	}

	override suspend fun getSelectionParams(): PensumSelectionParams {
		val selection = pensumSelectionDao.getSelection() ?: return PensumSelectionParams()
		return PensumSelectionParams(
			careerCode = selection.careerCode,
			year = selection.year,
			modalityId = selection.modalityId
		)
	}

	override suspend fun savePensumResponse(response: GetPensumResponse) {
		writeMutex.withLock {
			transactionRunner.immediate {
				val cacheKey = response.cacheKey()
				pensumCacheDao.upsertEntity(response.toCacheEntity(cacheKey))
				pensumSelectionDao.upsertEntity(
					PensumSelectionEntity(
						id = PensumSelectionTable.DEFAULT_ID,
						careerCode = response.selection.careerCode,
						year = response.selection.year,
						modalityId = response.selection.modalityId,
						cacheKey = cacheKey,
						updatedAt = currentTimeMillis()
					)
				)
			}
		}
	}

	override suspend fun selectPensum(careerCode: Int, year: Int) {
		writeMutex.withLock {
			pensumSelectionDao.upsertEntity(
				PensumSelectionEntity(
					id = PensumSelectionTable.DEFAULT_ID,
					careerCode = careerCode,
					year = year,
					modalityId = null,
					cacheKey = null,
					updatedAt = currentTimeMillis()
				)
			)
		}
	}

	override suspend fun selectModality(modalityId: String) {
		writeMutex.withLock {
			val currentSelection = pensumSelectionDao.getSelection() ?: return@withLock
			val cacheKey = currentSelection.careerCode?.let { careerCode ->
				currentSelection.year?.let { year ->
					pensumCacheDao.getPensum(
						careerCode = careerCode,
						year = year,
						modalityId = modalityId
					)?.cacheKey
				}
			}
			pensumSelectionDao.upsertEntity(
				currentSelection.copy(
					modalityId = modalityId,
					cacheKey = cacheKey,
					updatedAt = currentTimeMillis()
				)
			)
		}
	}

	private fun PensumCacheEntity.toResponse(): GetPensumResponse {
		return json.decodeFromString(payloadJson)
	}

	private fun GetPensumResponse.toCacheEntity(cacheKey: String): PensumCacheEntity {
		return PensumCacheEntity(
			cacheKey = cacheKey,
			careerCode = selection.careerCode,
			year = selection.year,
			modalityId = selection.modalityId,
			payloadJson = json.encodeToString(this),
			updatedAt = currentTimeMillis()
		)
	}

	private fun List<AcademicTermEntity>.toAcademicSnapshot(
		attempts: List<AcademicAttemptEntity>
	): AcademicPensumSnapshot {
		val termsById = associateBy(AcademicTermEntity::id)
		return AcademicPensumSnapshot(
			attempts = attempts.mapNotNull { attempt ->
				val term = termsById[attempt.termId] ?: return@mapNotNull null
				AcademicPensumSnapshot.Attempt(
					subjectCode = attempt.subjectCode,
					credits = attempt.credits,
					termKind = TermKind.valueOf(term.kind),
					outcome = AttemptOutcome.valueOf(attempt.officialOutcome)
				)
			}
		)
	}
}
