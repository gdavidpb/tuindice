package com.gdavidpb.tuindice.data.source.service.selector

import com.gdavidpb.tuindice.data.model.service.DstPersonal
import com.gdavidpb.tuindice.data.source.service.converter.DstPersonalDataConverter
import pl.droidsonroids.jspoon.annotation.Selector

data class DstPersonalDataSelector(
        @Selector(".tablaL", converter = DstPersonalDataConverter::class)
        var selected: DstPersonal
)