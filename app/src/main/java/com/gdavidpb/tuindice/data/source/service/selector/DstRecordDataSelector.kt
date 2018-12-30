package com.gdavidpb.tuindice.data.source.service.selector

import com.gdavidpb.tuindice.data.model.service.DstRecord
import com.gdavidpb.tuindice.data.source.service.converter.DstRecordDataConverter
import pl.droidsonroids.jspoon.annotation.Selector

data class DstRecordDataSelector(
        @Selector(".tabla", converter = DstRecordDataConverter::class)
        var selected: DstRecord
)