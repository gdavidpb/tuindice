package com.gdavidpb.tuindice.data.mapper

import com.gdavidpb.tuindice.data.source.service.selector.DstPersonalDataSelector
import com.gdavidpb.tuindice.domain.mapper.Mapper
import com.gdavidpb.tuindice.domain.model.Account

open class AccountSelectorMapper : Mapper<DstPersonalDataSelector, Account> {
    override fun map(value: DstPersonalDataSelector): Account {
        return value.selected.run {
            Account(
                    id = id,
                    usbId = usbId,
                    firstNames = firstNames,
                    lastNames = lastNames,
                    scholarship = scholarship
            )
        }
    }
}