package com.gdavidpb.tuindice.persistence.data.source.room.otm

import androidx.room.Embedded
import androidx.room.Relation
import com.gdavidpb.tuindice.persistence.data.source.room.entities.QuarterEntity
import com.gdavidpb.tuindice.persistence.data.source.room.entities.SubjectEntity
import com.gdavidpb.tuindice.persistence.data.source.room.schema.QuarterTable
import com.gdavidpb.tuindice.persistence.data.source.room.schema.SubjectTable

data class QuarterWithSubjects(
	@Embedded
	val quarter: QuarterEntity,
	@Relation(
		parentColumn = QuarterTable.ID,
		entityColumn = SubjectTable.QUARTER_ID
	)
	val subjects: List<SubjectEntity>
)
