package com.gdavidpb.tuindice.data.source.service.converter

import com.gdavidpb.tuindice.data.model.service.DstCareer
import com.gdavidpb.tuindice.data.model.service.DstPersonal
import org.jsoup.nodes.Element
import pl.droidsonroids.jspoon.ElementConverter
import pl.droidsonroids.jspoon.annotation.Selector

class DstPersonalDataConverter : ElementConverter<DstPersonal> {
    override fun convert(node: Element, selector: Selector): DstPersonal {
        return node.select("td td").run {
            DstPersonal(
                    usbId = get(0).text(),
                    id = get(1).text(),
                    firstNames = get(2).text(),
                    lastNames = get(3).text(),
                    career = get(4).text().split(" - ").let { DstCareer(code = it[0].toInt(), name = it[1]) },
                    scholarship = !get(5).text().contains(" no ")
            )
        }
    }
}