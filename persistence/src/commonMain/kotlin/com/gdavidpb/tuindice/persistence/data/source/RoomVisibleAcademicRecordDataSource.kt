package com.gdavidpb.tuindice.persistence.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicAttemptDao
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicAttemptOverrideDao
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicRecordDao
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicTermDao
import com.gdavidpb.tuindice.persistence.data.room.daos.PendingMutationDao
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicAttemptOverrideEntity
import com.gdavidpb.tuindice.persistence.data.room.mapper.toAcademicTerms
import com.gdavidpb.tuindice.persistence.data.room.mapper.toAttemptOverride
import com.gdavidpb.tuindice.persistence.data.room.mapper.toMutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.record.AcademicRecordMutation
import com.gdavidpb.tuindice.persistence.domain.record.AcademicRecordMutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.record.RECORD_MUTATION_SCOPE
import com.gdavidpb.tuindice.persistence.domain.record.RECORD_MUTATION_STORE_ID
import com.gdavidpb.tuindice.persistence.domain.record.reapplying
import com.gdavidpb.tuindice.persistence.domain.record.sortedForReplay
import com.gdavidpb.tuindice.persistence.domain.repository.VisibleAcademicRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class RoomVisibleAcademicRecordDataSource(
	private val academicRecordDao: AcademicRecordDao,
	private val academicTermDao: AcademicTermDao,
	private val academicAttemptDao: AcademicAttemptDao,
	private val academicAttemptOverrideDao: AcademicAttemptOverrideDao,
	private val pendingMutationDao: PendingMutationDao,
	private val json: Json = Json
) : VisibleAcademicRecordRepository {
	override fun observeVisibleAcademicRecordFlow(): Flow<AcademicRecord?> {
		return combine(
			observeConfirmedRecordFlow(),
			observeRecordMutationsFlow()
		) { confirmedRecord, mutations ->
			confirmedRecord?.reapplying(mutations.sortedForReplay())
		}
	}

	private fun observeConfirmedRecordFlow(): Flow<AcademicRecord?> {
		return combine(
			academicRecordDao.observeRecordFlow(),
			academicTermDao.observeTermsFlow(),
			academicAttemptDao.observeAttemptsFlow(),
			academicAttemptOverrideDao.observeOverridesFlow()
		) { recordEntity, terms, attempts, overrides ->
			recordEntity?.let { persistedRecord ->
				AcademicRecord(
					id = persistedRecord.id,
					terms = terms.toAcademicTerms(attempts),
					attemptOverrides = overrides.map(AcademicAttemptOverrideEntity::toAttemptOverride)
				)
			}
		}
	}

	private fun observeRecordMutationsFlow(): Flow<List<AcademicRecordMutationEnvelope>> {
		return pendingMutationDao.observeMutations(
			storeId = RECORD_MUTATION_STORE_ID,
			scopeKey = RECORD_MUTATION_SCOPE
		).map { entities ->
			entities.map { entity ->
				entity.toMutationEnvelope(
					commandSerializer = AcademicRecordMutation.serializer(),
					json = json
				)
			}
		}
	}
}
