package com.gdavidpb.tuindice.data.source.service.converters

import com.gdavidpb.tuindice.domain.model.service.DstPersonal
import org.jsoup.nodes.Element
import pl.droidsonroids.jspoon.ElementConverter
import pl.droidsonroids.jspoon.annotation.Selector

class DstPersonalConverter : ElementConverter<DstPersonal> {
    override fun convert(node: Element, selector: Selector): DstPersonal {
        return node.select("td td").run {
            val (careerCode, careerName) = get(4).text().split("\\s*-\\s*".toRegex())

            DstPersonal(
                    usbId = get(0).text().replace("-", ""),
                    id = get(1).text(),
                    firstNames = get(2).text(),
                    lastNames = get(3).text(),
                    careerCode = careerCode.toInt(),
                    careerName = careerName,
                    scholarship = !get(5).text().contains(" no ")
            )
        }
    }
}