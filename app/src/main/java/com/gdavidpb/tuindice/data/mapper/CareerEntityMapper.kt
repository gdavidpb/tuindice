package com.gdavidpb.tuindice.data.mapper

import com.gdavidpb.tuindice.data.model.database.CareerEmbeddedEntity
import com.gdavidpb.tuindice.domain.mapper.BidirectionalMapper
import com.gdavidpb.tuindice.domain.model.Career

open class CareerEntityMapper : BidirectionalMapper<Career, CareerEmbeddedEntity> {
    override fun mapTo(from: Career): CareerEmbeddedEntity {
        return CareerEmbeddedEntity(
                code = from.code,
                name = from.name
        )
    }

    override fun mapFrom(to: CareerEmbeddedEntity): Career {
        return Career(
                code = to.code,
                name = to.name
        )
    }
}