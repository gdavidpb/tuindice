package com.gdavidpb.tuindice.data.mapper

import com.gdavidpb.tuindice.data.model.service.DstQuarter
import com.gdavidpb.tuindice.domain.mapper.Mapper
import com.gdavidpb.tuindice.domain.model.Quarter

open class QuarterMapper(
        private val periodMapper: PeriodMapper,
        private val subjectMapper: SubjectMapper
) : Mapper<DstQuarter, Quarter> {
    override fun map(value: DstQuarter): Quarter {
        return Quarter(
                period = value.period.let(periodMapper::map),
                subjects = value.subjects.map(subjectMapper::map),
                grade = value.grade,
                gradeSum = value.gradeSum
        )
    }
}