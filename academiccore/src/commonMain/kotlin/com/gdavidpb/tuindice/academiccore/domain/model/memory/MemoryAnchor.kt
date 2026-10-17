package com.gdavidpb.tuindice.academiccore.domain.model.memory

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * The fact a memory hangs from. [ref] identifies it (a career code, a term key, an attempt or an evaluation id) and
 * [date] is the date that bounds it, when it has one.
 */
@Serializable
data class MemoryAnchor(
	val kind: MemoryAnchorKind,
	val ref: String?,
	val date: LocalDate?
)
