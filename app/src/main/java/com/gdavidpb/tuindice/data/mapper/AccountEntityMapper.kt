package com.gdavidpb.tuindice.data.mapper

import com.gdavidpb.tuindice.data.model.database.AccountEntity
import com.gdavidpb.tuindice.domain.mapper.Mapper
import com.gdavidpb.tuindice.domain.model.Account

open class AccountEntityMapper(
        private val careerEntityMapper: CareerEntityMapper,
        private val recordEntityMapper: RecordEntityMapper
) : Mapper<AccountEntity, Account> {
    override fun map(value: AccountEntity): Account {
        return Account(
                usbId = value.usbId,
                id = value.uid,
                password = value.password,
                firstNames = value.firstNames,
                lastNames = value.lastNames,
                career = value.career.let(careerEntityMapper::map),
                record = value.record.let(recordEntityMapper::map),
                scholarship = value.scholarship
        )
    }
}