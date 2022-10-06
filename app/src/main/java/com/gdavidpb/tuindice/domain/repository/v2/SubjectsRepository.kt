package com.gdavidpb.tuindice.domain.repository.v2

import com.gdavidpb.tuindice.base.domain.model.Subject

interface SubjectsRepository {
	suspend fun getSubjects(qid: String): List<Subject>
	suspend fun getSubject(sid: String): Subject
	suspend fun updateSubject(sid: String, subject: Subject): Subject
	suspend fun deleteSubject(sid: String)
}