package com.gdavidpb.tuindice.data.mapper

import com.gdavidpb.tuindice.data.model.database.RecordEmbeddedEntity
import com.gdavidpb.tuindice.domain.mapper.BidirectionalMapper
import com.gdavidpb.tuindice.domain.model.Record

open class RecordEntityMapper : BidirectionalMapper<Record, RecordEmbeddedEntity> {
    override fun mapTo(from: Record): RecordEmbeddedEntity {
        return RecordEmbeddedEntity(
                enrolledCredits = from.enrolledCredits,
                enrolledSubjects = from.enrolledSubjects,
                approvedCredits = from.approvedCredits,
                approvedSubject = from.approvedSubject,
                retiredCredits = from.retiredCredits,
                retiredSubjects = from.retiredSubjects,
                failedCredits = from.failedCredits,
                failedSubjects = from.failedSubjects
        )
    }

    override fun mapFrom(to: RecordEmbeddedEntity): Record {
        return Record(
                quarters = listOf(), // todo empty
                enrolledCredits = to.enrolledCredits,
                enrolledSubjects = to.enrolledSubjects,
                approvedCredits = to.approvedCredits,
                approvedSubject = to.approvedSubject,
                retiredCredits = to.retiredCredits,
                retiredSubjects = to.retiredSubjects,
                failedCredits = to.failedCredits,
                failedSubjects = to.failedSubjects
        )
    }
}