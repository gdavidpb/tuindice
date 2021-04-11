package com.gdavidpb.tuindice.data.source.service.converter

import com.gdavidpb.tuindice.domain.model.service.DstPeriod
import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.utils.mappers.toStartEndDate
import org.jsoup.nodes.Element
import org.koin.core.KoinComponent
import org.koin.core.inject
import pl.droidsonroids.jspoon.ElementConverter
import pl.droidsonroids.jspoon.annotation.Selector

class DstPeriodConverter : ElementConverter<DstPeriod>, KoinComponent {

    private val settingsRepository by inject<SettingsRepository>()

    override fun convert(node: Element, selector: Selector): DstPeriod {
        val refYear = settingsRepository.getCredentialYear()

        val (startDate, endDate) = node.text().toStartEndDate(refYear)

        return DstPeriod(startDate, endDate)
    }
}