package com.gdavidpb.tuindice.persistence.data.room.otm

import androidx.room.Embedded
import androidx.room.Relation
import com.gdavidpb.tuindice.persistence.data.room.entity.QuarterEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.QuarterTable
import com.gdavidpb.tuindice.persistence.data.room.schema.SubjectTable

data class QuarterWithSubjects(
	@Embedded
	val quarter: QuarterEntity,
	@Relation(
		parentColumn = QuarterTable.ID,
		entityColumn = SubjectTable.QUARTER_ID
	)
	val subjects: List<SubjectEntity>
)
