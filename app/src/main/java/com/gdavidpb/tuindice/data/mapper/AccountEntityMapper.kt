package com.gdavidpb.tuindice.data.mapper

import com.gdavidpb.tuindice.data.model.database.AccountEntity
import com.gdavidpb.tuindice.domain.mapper.BidirectionalMapper
import com.gdavidpb.tuindice.domain.model.Account

open class AccountEntityMapper(
        private val careerEntityMapper: CareerEntityMapper,
        private val recordEntityMapper: RecordEntityMapper
) : BidirectionalMapper<Account, AccountEntity> {
    override fun mapTo(from: Account): AccountEntity {
        return AccountEntity(
                uid = from.id,
                usbId = from.usbId,
                password = from.password,
                firstNames = from.firstNames,
                lastNames = from.lastNames,
                scholarship = from.scholarship,
                career = from.career.let(careerEntityMapper::mapTo),
                record = from.record.let(recordEntityMapper::mapTo)
        )
    }

    override fun mapFrom(to: AccountEntity): Account {
        return Account(
                usbId = to.usbId,
                id = to.uid,
                password = to.password,
                firstNames = to.firstNames,
                lastNames = to.lastNames,
                career = to.career.let(careerEntityMapper::mapFrom),
                record = to.record.let(recordEntityMapper::mapFrom),
                scholarship = to.scholarship
        )
    }

}