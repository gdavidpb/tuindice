package com.gdavidpb.tuindice.subjects.data.source

import com.gdavidpb.tuindice.base.domain.model.GradingMode

internal object DebugSubjectScenarioResolver {
	data class Resolution(
		val scenario: DebugSubjectScenario,
		val metadata: SubjectMetadata? = null
	)

	data class SubjectMetadata(
		val code: String,
		val name: String,
		val credits: Int,
		val gradingMode: GradingMode
	)

	private val aliases = mapOf(
		"EC5745" to DebugSubjectScenario.EC5745,
		"DBG-NUM" to DebugSubjectScenario.EC5745,
		"MAT2230" to DebugSubjectScenario.MAT2230,
		"DBG-RET" to DebugSubjectScenario.MAT2230,
		"FIS2105" to DebugSubjectScenario.FIS2105,
		"DBG-FIS" to DebugSubjectScenario.FIS2105,
		"EL2001" to DebugSubjectScenario.EL2001,
		"DBG-EASY" to DebugSubjectScenario.EL2001,
		"EP3421" to DebugSubjectScenario.EP3421,
		"DBG-QUAL" to DebugSubjectScenario.EP3421,
		"QUI100" to DebugSubjectScenario.QUI100,
		"DBG-GLOBAL" to DebugSubjectScenario.QUI100,
		"MAT404" to DebugSubjectScenario.UNAVAILABLE,
		"DBG-NODATA" to DebugSubjectScenario.UNAVAILABLE,
		"ERR500" to DebugSubjectScenario.ERROR,
		"DBG-ERROR" to DebugSubjectScenario.ERROR
	)

	private val recordSubjects = listOf(
		subject("BCB218", "LAS MOLÉCULAS DE LA VIDA", 3),
		subject("CE2562", "GERENCIA ESTRATEGICA DE PROYECTOS", 3),
		subject("CE3114", "ECONOMIA DE LA EMPRESA", 3),
		subject("CE3122", "EVALUACION DE PROYECTOS", 3),
		subject("CE3419", "INTRODUCCION A LA GERENCIA", 3),
		subject("CE3422", "EL EMPRENDEDOR Y EL DESARROLLO DE NUEVOS NEGOCIOS", 3),
		subject("CI2125", "COMPUTACION I", 3),
		subject("CI2126", "COMPUTACION II", 3),
		subject("CO3121", "FUNDAMENTOS DE PROBABILIDADES PARA INGENIEROS", 3),
		subject("CSA211", "VENEZUELA ANTE EL SIGLO XXI: CULTURA Y SOCIEDAD", 3),
		subject("CSA212", "VENEZUELA ANTE EL SIGLO XXI: ECONOMIA Y SOCIEDAD", 3),
		subject("CSA213", "VENEZUELA ANTE EL SIGLO XXI: EL CUERPO POLITICO Y SU PRESENC", 3),
		subject("CSB312", "POL.ESTRA.ERA NUC.", 3),
		subject("EAD213", "APROVECHAMIENTO: AMBIENTES TERRESTRES", 3),
		subject("EAD215", "EL SISTEMA DE PARQUES NAC.VENEZOLANOS", 3),
		subject("EAD228", "ASTROBIOLOGÍA", 3),
		subject("EC1177", "CIRCUITOS ELECTRONICOS I", 4),
		subject("EC1251", "ANALISIS DE CIRCUITOS ELECTRICOS I", 3),
		subject("EC1281", "LABORATORIO DE  MEDICIONES  ELECTRICAS", 2),
		subject("EC1311", "TEORIA ELECTROMAGNETICA", 4),
		subject("EC1421", "SEÑALES Y SISTEMAS", 4),
		subject("EC1723", "CIRCUITOS DIGITALES", 3),
		subject("EC2178", "CIRCUITOS ELECTRONICOS II", 4),
		subject("EC2272", "ANALISIS DE CIRCUITOS ELECTRICOS II", 3),
		subject("EC2322", "TEORIA DE ONDAS", 3),
		subject("EC2422", "COMUNICACIONES I", 4),
		subject("EC2721", "ARQUITECTURA DEL COMPUTADOR I", 3),
		subject("EC3173", "DISPOSITIVOS ELECTRONICOS", 3),
		subject("EC3179", "ELECTRONICA DE LOS SIST. DE ADQUISICION,PROCESAMIENTO Y CONT", 3),
		subject("EC3423", "COMUNICACIONES II", 4),
		subject("EC3731", "ARQUITECTURA DEL COMPUTADOR II", 5),
		subject("EC3881", "LABORATORIO DE PROYECTOS I", 4),
		subject("EC3882", "LABORATORIO DE PROYECTOS II", 4),
		subject("EC3883", "LABORATORIO DE PROYECTOS III", 4),
		subject("EC4179", "ELECTRONICA DE SIST.ADQUISICION, PROCESAM Y CONTROL AMBIENT", 3),
		subject("EC4432", "COMUNICACIONES MOVILES", 3),
		subject("EC4434", "FUNDAMENTOS DE RADIOCOMUNICACIONES", 4),
		subject("EC5333", "INT. A LAS MICROONDAS Y SUS APLICACIONES", 3),
		subject("EC5344", "RADIACION Y ANTENAS", 3),
		subject("EC5745", "PROCESAMIENTO CONCURRENTE ASINCRONO", 4),
		subject("EC5751", "REDES DE COMPUTADORAS I", 3),
		subject("EC5811", "FUNDAMENTOS DE MECATRONICA I", 4),
		subject("EP1206", "PROYECTO DE GRADO I", 3),
		subject("EP2206", "PROYECTO DE GRADO II", 3, gradingMode = GradingMode.QUALITATIVE_PASS_FAIL),
		subject("EP3206", "PROYECTO DE  GRADO III", 3, gradingMode = GradingMode.QUALITATIVE_PASS_FAIL),
		subject("EP5406", "PROYECTO DE GRADO A DEDICACION EXCLUSIVA", 9, gradingMode = GradingMode.QUALITATIVE_PASS_FAIL),
		subject("EP5801", "TÓPICOS ESPECIALES I", 3),
		subject("FLX445", "QUE ES LA VERDAD?", 3),
		subject("FS1111", "FISICA I", 3),
		subject("FS1112", "FISICA.II", 3),
		subject("FS2181", "LABORATORIO BASICO DE FISICA I", 2),
		subject("FS2211", "FISICA III", 3),
		subject("FS2212", "FISICA IV", 3),
		subject("FS2213", "FISICA V", 3),
		subject("FS2282", "LABORATORIO BASICO DE FISICA  III", 2),
		subject("ID1111", "INGLÉS", 3),
		subject("ID1112", "INGLES", 3),
		subject("ID1113", "INGLES", 3),
		subject("LLA111", "LENGUAJE I", 3),
		subject("LLA112", "LENGUAJE", 3),
		subject("LLA113", "LENGUAJE", 3),
		subject("MA1111", "MATEMATICAS I", 4),
		subject("MA1112", "MATEMATICAS II", 4),
		subject("MA1116", "MATEMATICAS 3", 4),
		subject("MA2112", "MATEMATICAS V", 4),
		subject("MA2113", "MATEMATICAS VI", 4),
		subject("MA2115", "MATEMATICAS IV", 4),
		subject("MA3111", "MATEMATICAS VII", 4),
		subject("PB5611", "INTRODUCCION A LA BIOINGENIERIA", 3),
		subject("PBG214", "NUTRICION, ACTIVIDAD FISICA Y CONTRO, DE PESO", 3),
		subject("PS2315", "SISTEMAS", 3),
		subject("PS2322", "SISTEMAS DE CONTROL I", 4),
		subject("PS2323", "SISTEMAS DE CONTROL II", 4),
		subject("PS4326", "SISTEMAS  PARA LA AUTOMATIZACION INDUSTRIAL", 3),
		subject("PS5315", "LOGICA Y CONTROLADORES PROGRAMABLES", 3)
	).associateBy(SubjectMetadata::code)

	fun normalize(subjectCode: String): String {
		return subjectCode.trim().uppercase()
	}

	fun resolve(subjectCode: String): Resolution? {
		val normalizedSubjectCode = normalize(subjectCode)
		recordSubjects[normalizedSubjectCode]?.let { metadata ->
			return Resolution(
				scenario = metadata.toScenario(),
				metadata = metadata
			)
		}

		return aliases[normalizedSubjectCode]?.let { scenario ->
			Resolution(scenario = scenario)
		}
	}

	private fun SubjectMetadata.toScenario(): DebugSubjectScenario {
		return when {
			gradingMode == GradingMode.QUALITATIVE_PASS_FAIL -> DebugSubjectScenario.EP3421
			code.startsWith("MA") || code == "CO3121" -> DebugSubjectScenario.MAT2230
			code.startsWith("FS") -> DebugSubjectScenario.FIS2105
			code.startsWith("EC") || code.startsWith("CI") || code.startsWith("PS") -> DebugSubjectScenario.EC5745
			else -> DebugSubjectScenario.EL2001
		}
	}

	private fun subject(
		code: String,
		name: String,
		credits: Int,
		gradingMode: GradingMode = GradingMode.NUMERIC
	): SubjectMetadata {
		return SubjectMetadata(
			code = code,
			name = name,
			credits = credits,
			gradingMode = gradingMode
		)
	}
}
