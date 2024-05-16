package com.gdavidpb.tuindice.record.data.repository.quarter.source.store

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.source.database.mapper.toLocalQuarter
import org.mobilenativefoundation.store.store5.Converter

class QuarterConverter
	: Converter<List<RemoteQuarter>, List<LocalQuarter>, List<Quarter>> {
	override fun fromNetworkToLocal(network: List<RemoteQuarter>): List<LocalQuarter> {
		return network.map { quarter -> quarter.toLocalQuarter() }
	}

	override fun fromOutputToLocal(output: List<Quarter>): List<LocalQuarter> {
		return output.map { quarter -> quarter.toLocalQuarter() }
	}
}