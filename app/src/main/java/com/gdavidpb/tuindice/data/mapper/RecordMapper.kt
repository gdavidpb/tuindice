package com.gdavidpb.tuindice.data.mapper

import com.gdavidpb.tuindice.data.source.service.selector.DstRecordDataSelector
import com.gdavidpb.tuindice.domain.mapper.Mapper
import com.gdavidpb.tuindice.domain.model.Record

open class RecordMapper(private val quarterMapper: QuarterMapper) : Mapper<DstRecordDataSelector, Record> {
    override fun map(value: DstRecordDataSelector): Record {
        return value.selected.run {
            Record(
                    quarters = quarters.map(quarterMapper::map),
                    enrolledCredits = enrolledCredits,
                    enrolledSubjects = enrolledSubjects,
                    approvedCredits = approvedCredits,
                    approvedSubject = approvedSubject,
                    retiredCredits = retiredCredits,
                    retiredSubjects = retiredSubjects,
                    failedCredits = failedCredits,
                    failedSubjects = failedSubjects
            )
        }
    }
}