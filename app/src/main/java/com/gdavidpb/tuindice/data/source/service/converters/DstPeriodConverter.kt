package com.gdavidpb.tuindice.data.source.service.converters

import com.gdavidpb.tuindice.domain.model.service.DstPeriod
import com.gdavidpb.tuindice.utils.mappers.parseStartEndDate
import org.jsoup.nodes.Element
import pl.droidsonroids.jspoon.ElementConverter
import pl.droidsonroids.jspoon.annotation.Selector

class DstPeriodConverter : ElementConverter<DstPeriod> {
    override fun convert(node: Element, selector: Selector): DstPeriod {
        val (startDate, endDate) = node.text().parseStartEndDate()

        return DstPeriod(startDate, endDate)
    }
}