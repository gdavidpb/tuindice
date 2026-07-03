package com.gdavidpb.tuindice.subjects.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.GradingMode
import com.gdavidpb.tuindice.academiccore.domain.utils.SubjectCatalogSearchNormalizer

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
			subject("EC5201", "SISTEMAS DE COMUNICACIONES", 3),
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
			subject("EL3104", "CAMPOS ELECTROMAGNÉTICOS", 3),
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
		subject("PS5315", "LOGICA Y CONTROLADORES PROGRAMABLES", 3),
		subject("CE3114", "ECONOMÍA DE LA EMPRESA", 3),
		subject("CI2511", "LÓGICA SIMBÓLICA", 4),
		subject("CI2525", "ESTRUCTURAS DISCRETAS I", 4),
		subject("CI2526", "ESTRUCTURAS DISCRETAS II", 4),
		subject("CI2527", "ESTRUCTURAS DISCRETAS III", 4),
		subject("CI2611", "ALGORITMOS Y ESTRUCTURAS I", 3),
		subject("CI2612", "ALGORITMOS Y ESTRUCTURAS II", 3),
		subject("CI2613", "ALGORITMOS Y ESTRUCTURAS III", 3),
		subject("CI2691", "LABORATORIO DE ALGORITMOS Y ESTRUCTURAS I", 2),
		subject("CI2692", "LABORATORIO DE ALGORITMOS Y ESTRUCTURAS II", 2),
		subject("CI2693", "LABORATORIO DE ALGORITMOS Y ESTRUCTURAS III", 2),
		subject("CI3311", "SISTEMAS DE BASE DE DATOS I", 3),
		subject("CI3391", "LABORATORIO DE SISTEMAS DE BASE DE DATOS I", 2),
		subject("CI3641", "LENGUAJES DE PROGRAMACIÓN", 3),
		subject("CI3661", "LABORATORIO DE LENGUAJES DE PROGRAMACIÓN", 2),
		subject("CI3715", "INGENIERÍA DE SOFTWARE I", 5),
		subject("CI3725", "TRADUCTORES E INTERPRETADORES", 5),
		subject("CI3815", "ORGANIZACIÓN DEL COMPUTADOR", 5),
		subject("CI3825", "SISTEMAS DE OPERACIÓN I", 5),
		subject("CI4210", "MINERÍA DE DATOS", 4),
		subject("CI4251", "TÓPICOS ESPECIALES EN INFORMÁTICA", 4),
		subject("CI4325", "INTERFACES CON EL USUARIO", 5),
		subject("CI4712", "INGENIERÍA DE SOFTWARE II", 4),
		subject("CI4835", "REDES DE COMPUTADORAS", 5),
		subject("CI4852", "TÓPICOS ESPECIALES EN COMPUTACIÓN", 4),
		subject("CI5311", "PARADIGMAS EN MODELAJE DE BASE DE DATOS I", 4),
		subject("CI5437", "INTELIGENCIA ARTIFICIAL I", 4),
		subject("CI5832", "REDES II", 4),
		subject("CO3211", "CÁLCULO NUMÉRICO", 4),
		subject("CO3321", "ESTADÍSTICA", 4),
		subject("EG1111", "HISTORIA DE LA CIENCIA", 3),
		subject("EG1112", "ÉTICA Y SOCIEDAD", 3),
		subject("EG1113", "ARTE Y CULTURA CONTEMPORÁNEA", 3),
		subject("EG1114", "PENSAMIENTO CRÍTICO", 3),
		subject("EG1115", "AMBIENTE Y DESARROLLO", 3),
		subject("EG1116", "CULTURA Y TECNOLOGÍA", 3),
		subject("EP1308", "PROYECTO DE GRADO I", 3, gradingMode = GradingMode.QUALITATIVE_PASS_FAIL),
		subject("EP2308", "PROYECTO DE GRADO II", 3, gradingMode = GradingMode.QUALITATIVE_PASS_FAIL),
		subject("EP3308", "PROYECTO DE GRADO III", 3, gradingMode = GradingMode.QUALITATIVE_PASS_FAIL),
		subject("EP5855", "TÓPICOS ESPECIALES EN INGENIERÍA DE COMPUTACIÓN I", 4),
		subject("EP5856", "TÓPICOS ESPECIALES EN INGENIERÍA DE COMPUTACIÓN II", 4),
		subject("MOCK101", "MOCK SUBJECT", 4),
		subject("CO3121", "FUNDAMENTOS DE PROBABILIDADES PARA INGENIEROS", 3),
		subject("CSA211", "VENEZUELA ANTE EL SIGLO XXI I", 3),
		subject("CSA212", "VENEZUELA ANTE EL SIGLO XXI II", 3),
		subject("CSA213", "VENEZUELA ANTE EL SIGLO XXI III", 3),
		subject("FS1111", "FÍSICA I", 3),
		subject("FS1112", "FÍSICA II", 3),
		subject("ID1111", "INGLÉS I", 3),
		subject("ID1112", "INGLÉS II", 3),
		subject("ID1113", "INGLÉS III", 3),
		subject("LLA111", "LENGUAJE I", 3),
		subject("LLA112", "LENGUAJE II", 3),
		subject("LLA113", "LENGUAJE III", 3),
		subject("MA1111", "MATEMÁTICAS I", 4),
		subject("MA1112", "MATEMÁTICAS II", 4),
		subject("MA1116", "MATEMÁTICAS III", 4),
		subject("MA2112", "MATEMÁTICAS V", 4),
		subject("MA2115", "MATEMÁTICAS IV", 4),
		subject("PS1111", "MODELOS LINEALES I", 4),
		subject("PS1115", "SISTEMAS DE INFORMACIÓN I", 4),
		subject("QA", "REMOTE UNAVAILABLE SUBJECT", 3),
		subject("QB", "REMOTE RETRY SUBJECT", 3),
		subject("RX", "DEBUG RETRY SUBJECT", 3)
	).associateBy(SubjectMetadata::code)

	fun normalize(subjectCode: String): String {
		return subjectCode.trim().uppercase()
	}

	fun resolve(subjectCode: String): Resolution? {
		val normalizedSubjectCode = normalize(subjectCode)
		if (DebugSubjectRemoteMockCodes.matches(normalizedSubjectCode)) return null

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

	fun search(query: String, limit: Int): List<SubjectMetadata> {
		val normalizedQuery = SubjectCatalogSearchNormalizer.normalize(query)
		if (normalizedQuery.length < 2) return emptyList()

		return recordSubjects.values
			.filter { metadata ->
				SubjectCatalogSearchNormalizer.normalize(metadata.code).contains(normalizedQuery) ||
					SubjectCatalogSearchNormalizer.normalize(metadata.name).contains(normalizedQuery)
			}
			.sortedWith(
				compareBy<SubjectMetadata> { metadata ->
					val normalizedCode = SubjectCatalogSearchNormalizer.normalize(metadata.code)
					val normalizedName = SubjectCatalogSearchNormalizer.normalize(metadata.name)
					when {
						normalizedCode == normalizedQuery -> 0
						normalizedCode.startsWith(normalizedQuery) -> 1
						normalizedName.startsWith(normalizedQuery) -> 2
						else -> 3
					}
				}.thenBy(SubjectMetadata::code)
			)
			.take(limit.coerceAtMost(50))
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
