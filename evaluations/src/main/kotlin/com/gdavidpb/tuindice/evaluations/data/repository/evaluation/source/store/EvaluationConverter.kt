package com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.store

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.model.LocalEvaluation
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.model.RemoteEvaluation
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.database.mapper.toLocalEvaluation
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