package com.gdavidpb.tuindice.academiccore.domain.model

/**
 * The two slot families a subject can fill. Area, free and professional electives are all
 * [ELECTIVE]; only a Estudios Generales slot is [GENERAL_STUDIES].
 */
enum class AcademicPensumSlotKind {
	ELECTIVE,
	GENERAL_STUDIES
}
