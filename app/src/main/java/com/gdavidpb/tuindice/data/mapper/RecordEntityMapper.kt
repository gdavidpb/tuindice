package com.gdavidpb.tuindice.data.mapper

import com.gdavidpb.tuindice.data.model.database.RecordEmbeddedEntity
import com.gdavidpb.tuindice.domain.mapper.Mapper
import com.gdavidpb.tuindice.domain.model.Record

open class RecordEntityMapper : Mapper<RecordEmbeddedEntity, Record> {
    override fun map(value: RecordEmbeddedEntity): Record {
        return Record(
                quarters = listOf(), // todo empty
                enrolledCredits = value.enrolledCredits,
                enrolledSubjects = value.enrolledSubjects,
                approvedCredits = value.approvedCredits,
                approvedSubject = value.approvedSubject,
                retiredCredits = value.retiredCredits,
                retiredSubjects = value.retiredSubjects,
                failedCredits = value.failedCredits,
                failedSubjects = value.failedSubjects
        )
    }
}