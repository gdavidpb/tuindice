package com.gdavidpb.tuindice.data.model.database

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import com.gdavidpb.tuindice.data.utils.*
import com.gdavidpb.tuindice.domain.model.QuarterStatus

@Entity(tableName = TABLE_QUARTERS,
        foreignKeys = [(ForeignKey(
                entity = AccountEntity::class,
                parentColumns = [COLUMN_ID],
                childColumns = [COLUMN_AID],
                onDelete = CASCADE,
                onUpdate = CASCADE))],
        indices = [
            Index(value = [COLUMN_START_TIME, COLUMN_END_TIME], unique = true),
            Index(value = [COLUMN_AID])])
data class QuarterEntity(
        @PrimaryKey(autoGenerate = true)
        val id: Long = 0,
        val aid: Long = 0,
        val status: QuarterStatus = QuarterStatus.NONE,
        @Embedded
        val period: PeriodEmbeddedEntity = PeriodEmbeddedEntity(),
        val grade: Double = 0.0,
        val gradeSum: Double = 0.0
)