package com.gdavidpb.tuindice.data.source.assistant

import com.gdavidpb.tuindice.pensum.domain.model.PensumNodeType
import com.gdavidpb.tuindice.pensum.domain.model.PensumObservation
import com.gdavidpb.tuindice.pensum.domain.repository.PensumRepository
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import kotlinx.coroutines.flow.first

/**
 * Resolves the subject codes an intent names to the course nodes of the student's pensum: the name and the credits a
 * planned subject carries, as the record's own suggestions take them. A code that is no course of the pensum is absent
 * from the answer, and so is every code while the pensum is not loaded. The pensum is read at the time of the call.
 */
class PensumSubjectCatalog(
	private val pensumRepository: PensumRepository
) {
	suspend fun resolve(subjectCodes: List<String>): Map<String, SyntheticTermSubject> {
		val observation = pensumRepository.observePensumFlow().first()
		val nodes = if (observation is PensumObservation.Content) observation.pensum.pensum.nodes else emptyList()

		val courseByCode = nodes
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
