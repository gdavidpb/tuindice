package com.gdavidpb.tuindice.data.source.assistant

import com.gdavidpb.tuindice.pensum.domain.model.PensumNodeType
import com.gdavidpb.tuindice.pensum.domain.model.PensumObservation
import com.gdavidpb.tuindice.pensum.domain.repository.PensumRepository
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import kotlinx.coroutines.flow.first

/**
 * Resolves the subject codes an intent names to the course nodes of the student's pensum: the name and the credits a
 * planned subject carries, as the record's own suggestions take them. A code that is no course of the pensum is absent
 * from the answer. The answer is null while the pensum is not loaded, because no code can be judged then: the caller
 * refuses the intent for that reason, not as if its codes did not exist. A request that names no code needs nothing
 * from the pensum, so it is answered with no subject even then. The pensum is read at the time of the call.
 */
class PensumSubjectCatalogDataSource(
	private val pensumRepository: PensumRepository
) {
	suspend fun resolve(subjectCodes: List<String>): Map<String, SyntheticTermSubject>? {
		// Nothing is named, so the pensum is not read: an intent without subjects does not depend on it.
		if (subjectCodes.isEmpty()) return emptyMap()

		val observation = pensumRepository.observePensumFlow().first()

		// Any other observation (waiting for the record, unavailable, missing) leaves every code unjudged.
		return if (observation is PensumObservation.Content) coursesOf(subjectCodes, observation) else null
	}

	// The courses of the loaded pensum that the codes name. A code that is no course of it is absent from the answer.
	private fun coursesOf(
		subjectCodes: List<String>,
		observation: PensumObservation.Content
	): Map<String, SyntheticTermSubject> {
		val courseByCode = observation.pensum.pensum.nodes
			.filter { node -> node.nodeType == PensumNodeType.COURSE }
			.mapNotNull { node -> node.subjectCode?.let { code -> normalizeSubjectCode(code) to node } }
			.distinctBy { (code, _) -> code }
			.toMap()

		return subjectCodes
			.map(::normalizeSubjectCode)
			.mapNotNull { code ->
				courseByCode[code]?.let { node ->
					code to SyntheticTermSubject(subjectCode = code, name = node.name, credits = node.credits)
				}
			}
			.toMap()
	}
}
