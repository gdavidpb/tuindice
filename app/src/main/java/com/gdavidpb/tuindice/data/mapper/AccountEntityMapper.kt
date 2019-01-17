package com.gdavidpb.tuindice.data.mapper

import com.gdavidpb.tuindice.data.model.database.AccountEntity
import com.gdavidpb.tuindice.data.utils.toShortName
import com.gdavidpb.tuindice.domain.mapper.BidirectionalMapper
import com.gdavidpb.tuindice.domain.model.Account

open class AccountEntityMapper : BidirectionalMapper<Account, AccountEntity> {
    override fun mapTo(from: Account): AccountEntity {
        return AccountEntity(
                uid = from.id,
                usbId = from.usbId,
                email = from.email,
                fullName = from.fullName,
                firstNames = from.firstNames,
                lastNames = from.lastNames,
                scholarship = from.scholarship
        )
    }

    override fun mapFrom(to: AccountEntity): Account {
        return Account(
                id = to.uid,
                usbId = to.usbId,
                email = to.email,
                fullName = to.fullName,
                shortName = to.fullName.toShortName(),
                firstNames = to.firstNames,
                lastNames = to.lastNames,
                scholarship = to.scholarship
        )
    }
}