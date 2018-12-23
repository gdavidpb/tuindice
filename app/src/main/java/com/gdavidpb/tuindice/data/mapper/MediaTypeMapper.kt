package com.gdavidpb.tuindice.data.mapper

import com.gdavidpb.tuindice.domain.mapper.Mapper

open class MediaTypeMapper : Mapper<okhttp3.MediaType, com.google.common.net.MediaType> {
    override fun map(value: okhttp3.MediaType): com.google.common.net.MediaType {
        return com.google.common.net.MediaType.parse("$value")
    }
}