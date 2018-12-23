package com.gdavidpb.tuindice.data.mapper

import com.gdavidpb.tuindice.data.model.service.DstSubject
import com.gdavidpb.tuindice.domain.mapper.Mapper
import com.gdavidpb.tuindice.domain.model.Subject

open class SubjectMapper : Mapper<DstSubject, Subject> {
    override fun map(value: DstSubject): Subject {
        return Subject(
                code = value.code,
                name = value.name,
                credits = value.credits,
                grade = value.grade,
                status = value.status
        )
    }
}