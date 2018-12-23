package com.gdavidpb.tuindice.data.mapper

import com.gdavidpb.tuindice.data.model.database.CareerEmbeddedEntity
import com.gdavidpb.tuindice.domain.mapper.Mapper
import com.gdavidpb.tuindice.domain.model.Career

open class CareerEntityMapper : Mapper<CareerEmbeddedEntity, Career> {
    override fun map(value: CareerEmbeddedEntity): Career {
        return Career(
                code = value.code,
                name = value.name
        )
    }
}