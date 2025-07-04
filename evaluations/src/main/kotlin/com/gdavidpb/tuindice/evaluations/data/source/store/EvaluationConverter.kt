package com.gdavidpb.tuindice.evaluations.data.source.store

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.data.mapper.toLocalEvaluation
import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluation
import com.gdavidpb.tuindice.evaluations.data.model.RemoteEvaluation
import org.mobilenativefoundation.store.store5.Converter

class EvaluationConverter
	: Converter<List<RemoteEvaluation>, List<LocalEvaluation>, List<Evaluation>> {
	override fun fromNetworkToLocal(network: List<RemoteEvaluation>): List<LocalEvaluation> {
		return network.map { evaluation -> evaluation.toLocalEvaluation() }
	}

	override fun fromOutputToLocal(output: List<Evaluation>): List<LocalEvaluation> {
		return output.map { evaluation -> evaluation.toLocalEvaluation() }
	}
}