package com.gdavidpb.tuindice.data.source.service.converter

import com.gdavidpb.tuindice.data.model.service.DstPeriod
import com.gdavidpb.tuindice.utils.parse
import org.jsoup.nodes.Element
import pl.droidsonroids.jspoon.ElementConverter
import pl.droidsonroids.jspoon.annotation.Selector

open class DstPeriodConverter : ElementConverter<DstPeriod> {
    override fun convert(node: Element, selector: Selector): DstPeriod {
        val value = "\\w+-\\w+ \\d{4}".toRegex().find(node.text())?.value

        return DstPeriod(
                startDate = value
                        ?.replace("-[^\\s]+".toRegex(), "")
                        ?.parse("MMMM yyyy"),
                endDate = value
                        ?.replace("^[^-]+-".toRegex(), "")
                        ?.parse("MMMM yyyy")
        )
    }
}