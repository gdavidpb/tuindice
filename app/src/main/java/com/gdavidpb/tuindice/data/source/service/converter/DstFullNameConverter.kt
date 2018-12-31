package com.gdavidpb.tuindice.data.source.service.converter

import org.jsoup.nodes.Element
import pl.droidsonroids.jspoon.ElementConverter
import pl.droidsonroids.jspoon.annotation.Selector

open class DstFullNameConverter : ElementConverter<String> {
    override fun convert(node: Element, selector: Selector): String {
        return node.text().substringAfter(" | ")
    }
}