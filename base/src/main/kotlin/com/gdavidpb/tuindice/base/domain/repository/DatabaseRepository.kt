package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.model.Subject

interface DatabaseRepository {
	suspend fun isUpdated(uid: String): Boolean

	suspend fun addAccount(uid: String, account: Account)
	suspend fun getAccount(uid: String): Account

	suspend fun addQuarter(uid: String, quarter: Quarter)
	suspend fun getQuarter(uid: String, qid: String): Quarter
	suspend fun getQuarters(uid: String): List<Quarter>
	suspend fun removeQuarter(uid: String, qid: String)

	suspend fun getSubject(uid: String, sid: String): Subject

	suspend fun addEvaluation(uid: String, evaluation: Evaluation)
	suspend fun getEvaluation(uid: String, eid: String): Evaluation
	suspend fun getSubjectEvaluations(uid: String, sid: String): List<Evaluation>
	suspend fun removeEvaluation(uid: String, eid: String)

	suspend fun close()
}