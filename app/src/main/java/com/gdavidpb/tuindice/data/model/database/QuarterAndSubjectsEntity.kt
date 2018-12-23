package com.gdavidpb.tuindice.data.model.database

import androidx.room.Embedded
import androidx.room.Relation
import com.gdavidpb.tuindice.data.utils.COLUMN_ID
import com.gdavidpb.tuindice.data.utils.COLUMN_QID

data class QuarterAndSubjectsEntity(
        @Embedded
        val quarter: QuarterEntity = QuarterEntity(),
        @Relation(entity = SubjectEntity::class,
                parentColumn = COLUMN_ID,
                entityColumn = COLUMN_QID)
        val subjects: List<SubjectEntity> = listOf()
)