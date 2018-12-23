package com.gdavidpb.tuindice.data.model.database

import androidx.room.Embedded
import androidx.room.Relation
import com.gdavidpb.tuindice.data.utils.COLUMN_AID
import com.gdavidpb.tuindice.data.utils.COLUMN_ID

data class AccountAndQuartersEntity(
        @Embedded
        val account: AccountEntity = AccountEntity(),
        @Relation(entity = QuarterEntity::class,
                parentColumn = COLUMN_ID,
                entityColumn = COLUMN_AID)
        val quarters: List<QuarterAndSubjectsEntity> = listOf()
)