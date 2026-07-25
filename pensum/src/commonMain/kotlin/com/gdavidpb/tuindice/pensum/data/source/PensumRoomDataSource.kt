package com.gdavidpb.tuindice.pensum.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumSnapshot
import com.gdavidpb.tuindice.academiccore.domain.model.toAcademicPensumSnapshot
import com.gdavidpb.tuindice.academiccore.domain.utils.SubjectCatalogSearchNormalizer
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.pensum.data.mapper.cacheKey
import com.gdavidpb.tuindice.pensum.data.model.GetPensumResponse
import com.gdavidpb.tuindice.pensum.data.repository.PensumLocalDataRepository
import com.gdavidpb.tuindice.pensum.domain.model.PensumSelectionParams
import com.gdavidpb.tuindice.persistence.data.room.daos.PensumCacheDao
import com.gdavidpb.tuindice.persistence.data.room.daos.PensumSelectionDao
import com.gdavidpb.tuindice.persistence.data.room.daos.SubjectCatalogCacheDao
import com.gdavidpb.tuindice.persistence.data.room.entity.PensumCacheEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.PensumSelectionEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectCatalogCacheEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.PensumSelectionTable
import com.gdavidpb.tuindice.persistence.domain.repository.PersistenceTransactionRunner
import com.gdavidpb.tuindice.persistence.domain.repository.VisibleAcademicRecordRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
	private val subjectCatalogCacheDao: SubjectCatalogCacheDao,
	private val visibleAcademicRecordRepository: VisibleAcademicRecordRepository,
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
		return visibleAcademicRecordRepository
			.observeVisibleAcademicRecordFlow()
			.map { visibleRecord -> visibleRecord.toAcademicPensumSnapshot() }
	}

	override suspend fun hasSelectedPensumResponse(): Boolean {
		val cacheKey = pensumSelectionDao.getSelection()?.cacheKey ?: return false
		return pensumCacheDao.getPensum(cacheKey) != null
	}

	override suspend fun getSelectionParams(): PensumSelectionParams {
		val selection = pensumSelectionDao.getSelection() ?: return PensumSelectionParams()
		if (selection.inferred) return PensumSelectionParams()
		return PensumSelectionParams(
			year = selection.year,
			modalityId = selection.modalityId
		)
	}

	override suspend fun savePensumResponse(response: GetPensumResponse, inferredSelection: Boolean) {
		writeMutex.withLock {
			transactionRunner.immediate {
				val cacheKey = response.cacheKey()
				val now = currentTimeMillis()
				pensumCacheDao.upsertEntity(response.toCacheEntity(cacheKey))
				val subjectCatalog = response.toSubjectCatalogCacheEntities(updatedAt = now)
				if (subjectCatalog.isNotEmpty()) {
					subjectCatalogCacheDao.upsertEntities(subjectCatalog)
				}
				val selectedPensum = response.selectedPensum()
				pensumSelectionDao.upsertEntity(
					PensumSelectionEntity(
						id = PensumSelectionTable.DEFAULT_ID,
						year = selectedPensum.year,
						modalityId = selectedPensum.modalityId,
						inferred = inferredSelection,
						cacheKey = cacheKey,
						updatedAt = now
					)
				)
			}
		}
	}

	override suspend fun selectPensum(year: Int) {
		writeMutex.withLock {
			pensumSelectionDao.upsertEntity(
				PensumSelectionEntity(
					id = PensumSelectionTable.DEFAULT_ID,
					year = year,
					modalityId = null,
					inferred = false,
					cacheKey = null,
					updatedAt = currentTimeMillis()
				)
			)
		}
	}

	override suspend fun selectModality(modalityId: String) {
		writeMutex.withLock {
			val currentSelection = pensumSelectionDao.getSelection() ?: return@withLock
			val cacheKey = currentSelection.year?.let { year ->
				pensumCacheDao.getPensum(
					year = year,
					modalityId = modalityId
				)?.cacheKey
			}
			pensumSelectionDao.upsertEntity(
				currentSelection.copy(
					modalityId = modalityId,
					inferred = false,
					cacheKey = cacheKey,
					updatedAt = currentTimeMillis()
				)
			)
		}
	}

	override suspend fun selectSelection(year: Int, modalityId: String) {
		writeMutex.withLock {
			val cacheKey = pensumCacheDao.getPensum(
				year = year,
				modalityId = modalityId
			)?.cacheKey
			pensumSelectionDao.upsertEntity(
				PensumSelectionEntity(
					id = PensumSelectionTable.DEFAULT_ID,
					year = year,
					modalityId = modalityId,
					inferred = false,
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
		val pensum = selectedPensum()
		return PensumCacheEntity(
			cacheKey = cacheKey,
			year = pensum.year,
			modalityId = pensum.modalityId,
			payloadJson = json.encodeToString(this),
			updatedAt = currentTimeMillis()
		)
	}

	private fun GetPensumResponse.toSubjectCatalogCacheEntities(updatedAt: Long): List<SubjectCatalogCacheEntity> {
		return pensum.nodes
			.mapNotNull { node ->
				val subjectCode = node.subjectCode
					?.trim()
					?.uppercase()
					?.takeIf(RealSubjectCodeRegex::matches)
					?: return@mapNotNull null

				SubjectCatalogCacheEntity(
					subjectCode = subjectCode,
					name = node.name,
					credits = node.credits,
					gradingMode = null,
					normalizedCode = SubjectCatalogSearchNormalizer.normalize(subjectCode),
					normalizedName = SubjectCatalogSearchNormalizer.normalize(node.name),
					updatedAt = updatedAt
				)
			}
			.distinctBy(SubjectCatalogCacheEntity::subjectCode)
	}

	private fun GetPensumResponse.selectedPensum(): GetPensumResponse.Pensum {
		return pensum
	}
}

private val RealSubjectCodeRegex = Regex("^([A-Z]{2}\\d{4}|[A-Z]{3}\\d{3})$")
