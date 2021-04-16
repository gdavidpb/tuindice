package com.gdavidpb.tuindice.data.source.service.responses

import com.gdavidpb.tuindice.domain.model.service.DstRecord
import com.gdavidpb.tuindice.data.source.service.converters.DstRecordConverter
import pl.droidsonroids.jspoon.annotation.Selector

data class DstRecordResponse(
        @Selector(value = ".tabla", converter = DstRecordConverter::class)
        var selected: DstRecord? = null
)