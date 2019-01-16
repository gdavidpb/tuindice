package com.gdavidpb.tuindice.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.gdavidpb.tuindice.data.model.database.SubjectEntity
import com.gdavidpb.tuindice.data.utils.COLUMN_QID
import com.gdavidpb.tuindice.data.utils.COLUMN_STATUS
import com.gdavidpb.tuindice.data.utils.TABLE_SUBJECTS
import com.gdavidpb.tuindice.domain.model.SubjectStatus

@Dao
interface DstSubjectDao : BaseDao<SubjectEntity> {
    @Transaction
    @Query("SELECT * FROM $TABLE_SUBJECTS WHERE $COLUMN_QID = :qid")
    fun getForQuarter(qid: Long): List<SubjectEntity>

    @Query("SELECT * FROM $TABLE_SUBJECTS WHERE $COLUMN_STATUS = :status ORDER BY RANDOM() LIMIT 1")
    fun getSample(status: SubjectStatus): SubjectEntity
}