package com.gdavidpb.tuindice.data.mapper

import com.gdavidpb.tuindice.domain.mapper.Mapper

open class UsbIdMapper : Mapper<String, String> {
    override fun map(value: String): String {
        return "$value@usb.ve"
    }
}