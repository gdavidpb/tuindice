package com.gdavidpb.tuindice.record.domain.repository

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.model.quarter.QuarterRemoveTransaction
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectUpdateTransaction
import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import kotlinx.coroutines.flow.Flow

interface QuarterRepository {
	suspend fun getQuarters(uid: String): Flow<List<Quarter>>

	suspend fun removeQuarter(uid: String, transaction: Transaction<QuarterRemoveTransaction>)

	suspend fun updateSubject(uid: String, transaction: Transaction<SubjectUpdateTransaction>)
}