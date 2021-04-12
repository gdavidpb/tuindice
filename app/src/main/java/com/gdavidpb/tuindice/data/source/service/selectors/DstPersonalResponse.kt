package com.gdavidpb.tuindice.data.source.service.selectors

import com.gdavidpb.tuindice.domain.model.service.DstPersonal
import com.gdavidpb.tuindice.data.source.service.converters.DstPersonalConverter
import pl.droidsonroids.jspoon.annotation.Selector

data class DstPersonalResponse(
        @Selector(value = ".tablaL", converter = DstPersonalConverter::class)
        var selected: DstPersonal? = null
)