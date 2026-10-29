package com.gdavidpb.tuindice.chatcontract.component

/** Catalogs this build renders, as `<catalog>/<version>`. A new catalog or version is listed here. */
public object ChatCatalogs {
	public val supported: List<String> = listOf(
		"sistema/1", "nav/1", "perfil/1", "informe/1", "horario/1", "evaluaciones/1",
		"resumen/1", "pensum/1", "materias/1", "plan/1", "conocimiento/1"
	)
}
