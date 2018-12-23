package com.gdavidpb.tuindice.data.model.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gdavidpb.tuindice.data.utils.COLUMN_USB_ID
import com.gdavidpb.tuindice.data.utils.TABLE_ACCOUNTS

@Entity(tableName = TABLE_ACCOUNTS,
        indices = [Index(value = [COLUMN_USB_ID], unique = true)])
data class AccountEntity(
        @PrimaryKey(autoGenerate = true)
        val id: Long = -1,
        val uid: String = "",
        val usbId: String = "",
        val password: String = "",
        val firstNames: String = "",
        val lastNames: String = "",
        val scholarship: Boolean = false,
        @Embedded
        val career: CareerEmbeddedEntity = CareerEmbeddedEntity(),
        @Embedded
        val record: RecordEmbeddedEntity = RecordEmbeddedEntity(),
        val active: Boolean = false,
        val lastUpdate: Long = 0
)