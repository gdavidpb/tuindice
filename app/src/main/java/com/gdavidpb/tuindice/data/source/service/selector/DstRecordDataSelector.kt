package com.gdavidpb.tuindice.data.source.service.selector

import com.gdavidpb.tuindice.data.source.service.converter.DstRecordDataConverter
import com.gdavidpb.tuindice.data.model.service.DstRecord
import pl.droidsonroids.jspoon.annotation.Selector

open class DstRecordDataSelector {
    @Selector(".tabla", converter = DstRecordDataConverter::class)
    lateinit var selected: DstRecord
}