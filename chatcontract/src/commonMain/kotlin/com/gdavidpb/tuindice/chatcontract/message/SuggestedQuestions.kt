package com.gdavidpb.tuindice.chatcontract.message

import com.gdavidpb.tuindice.chatcontract.context.ScreenId

/**
 * The questions the assistant suggests, shared by the app and the service: the app offers them when a conversation
 * starts and in guided mode, and the service accepts in guided mode only a turn whose suggestion id is one of these,
 * answering with its [SuggestedReply.text]. Ids are stable; a question is retired by removing it, never by reusing
 * its id.
 */
public object SuggestedQuestions {
	private const val PER_SCREEN: Int = 3

	private val general: List<SuggestedReply> = listOf(
		reply("sq.general.term", "¿Cómo voy este trimestre?"),
		reply("sq.general.goal", "¿Qué índice necesito para llegar a mi meta?"),
		reply("sq.general.next", "¿Qué materias puedo inscribir el próximo trimestre?")
	)

	private val byScreen: Map<ScreenId, List<SuggestedReply>> = mapOf(
		ScreenId.SUMMARY to listOf(
			reply("sq.summary.term", "¿Cómo voy este trimestre?"),
			reply("sq.summary.attention", "¿Qué necesita mi atención?"),
			reply("sq.summary.pace", "¿Cuál es mi ritmo de créditos?")
		),
		ScreenId.STATISTICS to listOf(
			reply("sq.statistics.best", "¿Cuál fue mi mejor trimestre?"),
			reply("sq.statistics.history", "¿Cómo ha cambiado mi índice?"),
			reply("sq.statistics.pace", "¿Cuántos trimestres me faltan a este ritmo?")
		),
		ScreenId.RECORD to listOf(
			reply("sq.record.last", "¿Cómo me fue en el último trimestre?"),
			reply("sq.record.simulate", "Simula mis notas de este trimestre"),
			reply("sq.record.goal", "¿Qué índice necesito para llegar a mi meta?")
		),
		ScreenId.PENSUM to listOf(
			reply("sq.pensum.progress", "¿Cuánto me falta para graduarme?"),
			reply("sq.pensum.available", "¿Qué materias tengo disponibles?"),
			reply("sq.pensum.critical", "¿Qué materia me está retrasando más?")
		),
		ScreenId.EVALUATIONS to listOf(
			reply("sq.evaluations.week", "¿Qué evaluaciones tengo esta semana?"),
			reply("sq.evaluations.heavy", "¿Qué semanas vienen pesadas?"),
			reply("sq.evaluations.points", "¿Cuántos puntos llevo en cada materia?")
		),
		ScreenId.EVALUATION_DETAIL to listOf(
			reply("sq.evaluation.needed", "¿Cuánto necesito en lo que queda para pasar esta materia?"),
			reply("sq.evaluation.points", "¿Cuántos puntos llevo en esta materia?"),
			reply("sq.evaluation.week", "¿Qué más tengo esta semana?")
		),
		ScreenId.SUBJECT_DETAIL to listOf(
			reply("sq.subject.difficulty", "¿Qué tan difícil es esta materia?"),
			reply("sq.subject.requirements", "¿Qué requisitos tiene?"),
			reply("sq.subject.unlocks", "¿Qué materias desbloquea?")
		),
		ScreenId.SUBJECT_SEARCH to listOf(
			reply("sq.search.electives", "¿Qué electivas me recomiendas?"),
			reply("sq.search.available", "¿Qué materias tengo disponibles?"),
			reply("sq.search.difficulty", "¿Cuáles son las materias más difíciles de mi carrera?")
		),
		ScreenId.ABOUT to listOf(
			reply("sq.about.what", "¿Qué puedes hacer por mí?"),
			reply("sq.about.data", "¿Cómo se usan mis datos?"),
			reply("sq.about.memory", "¿Qué recuerdas de mí?")
		)
	)

	/** Every question, each once, for a lookup by id. */
	public val all: List<SuggestedReply> = (general + byScreen.values.flatten()).distinctBy { it.id }

	/** The questions offered on [screen], or the general ones when there is no screen or it has none of its own. */
	public fun forScreen(screen: ScreenId?): List<SuggestedReply> =
		(screen?.let(byScreen::get) ?: general).take(PER_SCREEN)

	/** The question with [id], or null when it is not one of these. */
	public fun byId(id: String): SuggestedReply? = all.firstOrNull { it.id == id }

	private fun reply(id: String, text: String): SuggestedReply = SuggestedReply(id = id, label = text, text = text)
}
