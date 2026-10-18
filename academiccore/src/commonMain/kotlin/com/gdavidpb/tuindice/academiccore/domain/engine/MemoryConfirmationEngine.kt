package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.memory.MemoryAnchorKind
import com.gdavidpb.tuindice.academiccore.domain.model.memory.MemoryCategory
import com.gdavidpb.tuindice.academiccore.domain.model.memory.MemoryValidityInput
import com.gdavidpb.tuindice.academiccore.domain.model.memory.MemoryValiditySnapshot

/**
 * Names what a confirmation of a memory answers. When [MemoryValidityEngine] asks the user whether a memory still
 * holds, the caller keeps the fact the question read with it; once the user confirms the memory, it is asked about
 * again only after that fact changes. It is a pure function: it reads and writes nothing.
 */
object MemoryConfirmationEngine {
	/**
	 * The fact of [snapshot] that the rule of the memory's category and anchor reads when it asks for a
	 * confirmation: the career, whether a goal's date passed, or whether a term is active. Null for the rules that
	 * never ask.
	 */
	fun confirmationFactOf(input: MemoryValidityInput, snapshot: MemoryValiditySnapshot): String? {
		val anchor = input.anchor

		return when (input.category to anchor.kind) {
			MemoryCategory.INTEREST to MemoryAnchorKind.CAREER ->
				"career:${snapshot.careerCode}"
			MemoryCategory.CAREER_GOAL to MemoryAnchorKind.CAREER ->
				"career:${snapshot.careerCode};datePassed:${anchor.date != null && anchor.date < snapshot.today}"
			MemoryCategory.TERM to MemoryAnchorKind.TERM ->
				"termActive:${anchor.ref != null && anchor.ref in snapshot.activeTermKeys}"
			else -> null
		}
	}
}
