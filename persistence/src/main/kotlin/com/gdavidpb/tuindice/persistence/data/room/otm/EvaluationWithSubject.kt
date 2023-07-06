package com.gdavidpb.tuindice.persistence.data.room.otm

import androidx.room.Embedded
import androidx.room.Relation
import com.gdavidpb.tuindice.persistence.data.room.entity.EvaluationEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.EvaluationTable
import com.gdavidpb.tuindice.persistence.data.room.schema.SubjectTable

data class EvaluationWithSubject(
	@Embedded
	val evaluation: EvaluationEntity,
	@Relation(
		parentColumn = EvaluationTable.SUBJECT_ID,
		entityColumn = SubjectTable.ID
	)
	val subject: SubjectEntity
)
