package com.gdavidpb.tuindice.data.source.service.selector

import com.gdavidpb.tuindice.data.source.service.converter.DstPersonalDataConverter
import com.gdavidpb.tuindice.data.model.service.DstPersonal
import pl.droidsonroids.jspoon.annotation.Selector

open class DstPersonalDataSelector {
    @Selector(".tablaL", converter = DstPersonalDataConverter::class)
    lateinit var selected: DstPersonal
}