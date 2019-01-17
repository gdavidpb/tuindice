package com.gdavidpb.tuindice.data.model.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gdavidpb.tuindice.data.utils.COLUMN_USB_ID
import com.gdavidpb.tuindice.data.utils.TABLE_ACCOUNTS
import java.util.*

@Entity(tableName = TABLE_ACCOUNTS,
        indices = [Index(value = [COLUMN_USB_ID], unique = true)])
data class AccountEntity(
        @PrimaryKey(autoGenerate = true)
        val id: Long = -1,
        val uid: String = "",
        val usbId: String = "",
        val email: String = "",
        val password: String = "",
        val fullName: String = "",
        val firstNames: String = "",
        val lastNames: String = "",
        val scholarship: Boolean = false,
        val active: Boolean = false,
        val lastUpdate: Date = Date(0)
)