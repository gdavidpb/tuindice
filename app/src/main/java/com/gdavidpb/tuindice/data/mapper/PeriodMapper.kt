package com.gdavidpb.tuindice.data.mapper

import com.gdavidpb.tuindice.data.model.service.DstPeriod
import com.gdavidpb.tuindice.domain.mapper.Mapper
import com.gdavidpb.tuindice.domain.model.Period

open class PeriodMapper : Mapper<DstPeriod, Period> {
    override fun map(value: DstPeriod): Period {
        return Period(
                startDate = value.startDate,
                endDate = value.endDate
        )
    }
}