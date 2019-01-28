package com.gdavidpb.tuindice.data.source.database.dao

import androidx.room.*
import com.gdavidpb.tuindice.data.model.database.AccountAndQuartersEntity
import com.gdavidpb.tuindice.data.model.database.AccountEntity
import com.gdavidpb.tuindice.utils.*

@Dao
interface DstAccountDao : BaseDao<AccountEntity> {
    @Query("SELECT * FROM $TABLE_ACCOUNTS WHERE $COLUMN_ACTIVE = 1 LIMIT 1")
    fun getActive(): AccountEntity?

    @Transaction
    @Query("SELECT * FROM $TABLE_ACCOUNTS WHERE $COLUMN_ACTIVE = 1 LIMIT 1")
    fun getActiveAndQuarters(): List<AccountAndQuartersEntity>

    @Query("UPDATE $TABLE_ACCOUNTS SET $COLUMN_ACTIVE = 0 WHERE $COLUMN_ACTIVE = 1")
    fun removeActive()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun storeAccount(accountEntity: AccountEntity)

    @Query("UPDATE $TABLE_ACCOUNTS SET $COLUMN_ACTIVE = 1 WHERE $COLUMN_EMAIL = :email")
    fun activeAccount(email: String)

    @Query("UPDATE $TABLE_ACCOUNTS SET " +
            "$COLUMN_USB_ID = '', " +
            "$COLUMN_PASSWORD = '', " +
            "$COLUMN_FIRST_NAMES = '', " +
            "$COLUMN_LAST_NAMES = ''")
    fun anonymous()
}