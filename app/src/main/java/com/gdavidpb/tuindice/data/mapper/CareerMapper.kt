package com.gdavidpb.tuindice.data.mapper

import com.gdavidpb.tuindice.data.model.service.DstCareer
import com.gdavidpb.tuindice.domain.mapper.Mapper
import com.gdavidpb.tuindice.domain.model.Career

open class CareerMapper : Mapper<DstCareer, Career> {
    override fun map(value: DstCareer): Career {
        return Career(code = value.code, name = value.name)
    }
}