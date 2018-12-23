package com.gdavidpb.tuindice.data.mapper

import com.gdavidpb.tuindice.data.source.service.selector.DstPersonalDataSelector
import com.gdavidpb.tuindice.domain.mapper.Mapper
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.Record

open class AccountMapper(private val careerMapper: CareerMapper) : Mapper<DstPersonalDataSelector, Account> {
    override fun map(value: DstPersonalDataSelector): Account {
        return value.selected.run {
            Account(
                    usbId = usbId,
                    id = id,
                    password = "",
                    firstNames = firstNames,
                    lastNames = lastNames,
                    career = career.let(careerMapper::map),
                    record = Record.EMPTY,
                    scholarship = scholarship
            )
        }
    }
}