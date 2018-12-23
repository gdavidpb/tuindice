package com.gdavidpb.tuindice.data.model.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gdavidpb.tuindice.data.utils.COLUMN_CODE
import com.gdavidpb.tuindice.data.utils.COLUMN_ID
import com.gdavidpb.tuindice.data.utils.COLUMN_QID
import com.gdavidpb.tuindice.data.utils.TABLE_SUBJECTS
import com.gdavidpb.tuindice.domain.model.SubjectStatus

@Entity(tableName = TABLE_SUBJECTS,
        foreignKeys = [ForeignKey(
                entity = QuarterEntity::class,
                parentColumns = [COLUMN_ID],
                childColumns = [COLUMN_QID],
                onDelete = CASCADE,
                onUpdate = CASCADE)],
        indices = [
            Index(value = [COLUMN_CODE], unique = true),
            Index(value = [COLUMN_QID])])
data class SubjectEntity(
        @PrimaryKey(autoGenerate = true)
        val id: Long = 0,
        val qid: Long = 0,
        val code: String = "",
        val name: String = "",
        val credits: Int = 0,
        val careerCode: Int = 0,
        val grade: Int = 0,
        val status: SubjectStatus = SubjectStatus.NONE
)