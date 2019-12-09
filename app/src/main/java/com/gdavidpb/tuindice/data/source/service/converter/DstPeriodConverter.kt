package com.gdavidpb.tuindice.data.source.service.converter

import com.gdavidpb.tuindice.domain.model.service.DstPeriod
import com.gdavidpb.tuindice.utils.toStartEndDate
import org.jsoup.nodes.Element
import pl.droidsonroids.jspoon.ElementConverter
import pl.droidsonroids.jspoon.annotation.Selector

open class DstPeriodConverter : ElementConverter<DstPeriod> {
    override fun convert(node: Element, selector: Selector): DstPeriod {
        val (startDate, endDate) = node.text().toStartEndDate()

        return DstPeriod(startDate, endDate)
    }
}