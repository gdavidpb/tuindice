package com.gdavidpb.tuindice.scenarios.catalog

/**
 * The actions no scenario has to cover, each with its reason: internal, platform edge or pending. Every other
 * action of every `presentation/contract` must be listed in some scenario's `covers`
 * (`ActionCoverageTest`).
 */
object ActionDispositions {
	val all: List<ActionDisposition> = listOf(
		ActionDisposition.Internal(
			action = "about.About.LoadVersion",
			reason = "arranque de la pantalla"
		),
		ActionDisposition.Internal(
			action = "about.Settings.LoadSettings",
			reason = "arranque de la pantalla"
		),
		ActionDisposition.PlatformEdge(
			action = "about.Settings.ReportBug",
			reason =
				"el disparador abre la app de correo y no hay nada que el escenario pueda comprobar después: en Android, " +
				"con Gmail sin cuenta en el emulador, abre su pantalla de redacción y la cierra sola en unos 100 ms, así " +
				"que la app salió del primer plano en 0 de 10 ejecuciones; en iOS la app se quedó al frente en 3 de 3 " +
				"(docs/e2e-mediciones.md, sección 3). `about-platform-edge-triggers` lo toca y comprueba que Preferencias es " +
				"usable al volver"
		),
		ActionDisposition.PlatformEdge(
			action = "assistant.AssistantChat.ActivateNotifications",
			reason =
				"Lanza el diálogo de permiso de notificaciones del sistema, que el E2E no controla; la tarjeta solo se " +
				"ofrece en Android 13 o posterior sin el permiso (y en iOS con la autorización sin decidir), y ningún " +
				"escenario fija ese estado del sistema"
		),
		ActionDisposition.PlatformEdge(
			action = "assistant.AssistantChat.OpenExternalLink",
			reason =
				"Abre un enlace o un teléfono con el sistema, que lo lleva al navegador o al marcador, fuera de la app: " +
				"el escenario no puede comprobar lo que abre el sistema, y si no se abre, el chat lo dice en su aviso"
		),
		ActionDisposition.PlatformEdge(
			action = "assistant.AssistantChat.AttachPhoto",
			reason =
				"Abre la cámara o la galería del sistema; se prueba a mano " +
				"(ver e2e/platform/*/assistant-photo-picker.md)"
		),
		ActionDisposition.PlatformEdge(
			action = "assistant.AssistantChat.DismissNotificationOffer",
			reason =
				"La tarjeta solo se ofrece donde el sistema todavía pregunta por el permiso (Android 13 o posterior " +
				"sin concederlo; iOS con la autorización sin decidir), y ningún escenario fija ese estado del sistema"
		),
		ActionDisposition.Internal(
			action = "assistant.AssistantChat.Open",
			reason = "la hoja se abre desde una pantalla con el contexto que esa pantalla declara; la ruta la despacha, " +
				"no la hace ningún usuario"
		),
		ActionDisposition.PlatformEdge(
			action = "assistant.AssistantChat.RequestPhoto",
			reason =
				"Abre la cámara o la galería del sistema; se prueba a mano " +
				"(ver e2e/platform/*/assistant-photo-picker.md)"
		),
		ActionDisposition.Internal(
			action = "assistant.AssistantComponentSheet.Open",
			reason = "arranque del estado de la ruta: la ruta lo despacha cuando se abre la hoja"
		),
		ActionDisposition.Internal(
			action = "assistant.AssistantComponentSheet.Unhandled",
			reason = "the sheet route hands the sheet a component's action that is not a link; the sheet keeps it " +
				"without a change (AssistantComponentSheetBehaviorTest)"
		),
		ActionDisposition.Internal(
			action = "assistant.AssistantMemory.Load",
			reason = "arranque de la pantalla: la pantalla de memoria lo despacha como su acción inicial"
		),
		ActionDisposition.Internal(
			action = "auth.SignOut.Initialize",
			reason = "arranque del estado de la ruta a partir de los cambios pendientes"
		),
		ActionDisposition.Internal(
			action = "enrollmentproof.Enrollment.OpenEnrollmentProofCompleted",
			reason = "la ruta vuelve a entrar en la máquina con si el visor del dispositivo abrió el comprobante"
		),
		ActionDisposition.Internal(
			action = "evaluations.Evaluation.LoadAvailableAttempts",
			reason = "arranque del formulario"
		),
		ActionDisposition.Internal(
			action = "evaluations.Evaluation.LoadEvaluation",
			reason = "arranque del formulario de edición"
		),
		ActionDisposition.Internal(
			action = "evaluations.Evaluations.LoadEvaluations",
			reason = "arranque de la lista"
		),
		ActionDisposition.Internal(
			action = "maincore.Browser.SetLoading",
			reason = "callback del renderizador"
		),
		ActionDisposition.Internal(
			action = "maincore.Browser.SetLoadFailed",
			reason = "callback del renderizador"
		),
		ActionDisposition.Internal(
			action = "maincore.Main.RequestSync",
			reason = "el host lo despacha cada vez que la app vuelve al frente con contenido; no lo hace ningún usuario"
		),
		ActionDisposition.Internal(
			action = "maincore.Main.SessionEndHandled",
			reason = "la ruta del host informa que atendió el fin de una sesión que terminó sin que el usuario lo viera"
		),
		ActionDisposition.Internal(
			action = "maincore.Main.ShowOutdatedApp",
			reason = "el host muestra la pantalla global de app desactualizada a partir del evento HTTP 426 compartido"
		),
		ActionDisposition.PlatformEdge(
			action = "maincore.Main.ClickUpdateApp",
			reason = "El paso a la tienda abre Google Play o App Store y requiere comprobación del host o del sistema."
		),
		ActionDisposition.Internal(
			action = "maincore.Main.UpdateFlowCompleted",
			reason =
				"la ruta vuelve a entrar en la máquina con el resultado del lanzamiento de la actualización de la " +
				"plataforma"
		),
		ActionDisposition.PlatformEdge(
			action = "maincore.Main.RequestReview",
			reason = "la hoja de valoración de la tienda requiere comprobación del host o del sistema"
		),
		ActionDisposition.PlatformEdge(
			action = "maincore.Main.RequestUpdateCheck",
			reason = "el aviso de actualización del sistema requiere comprobación del host o del sistema"
		),
		ActionDisposition.Internal(
			action = "maincore.Main.NoteSyncUnavailable",
			reason = "marca de telemetría que la ruta despacha cuando la sincronización pasa a no disponible"
		),
		ActionDisposition.Internal(
			action = "maincore.Main.NoteSyncFailed",
			reason = "marca de telemetría que la ruta despacha cuando la sincronización pasa a fallida"
		),
		ActionDisposition.Internal(
			action = "pensum.Pensum.ObservePensum",
			reason = "arranque de la pantalla"
		),
		ActionDisposition.Internal(
			action = "record.CreateSyntheticTerm.Observe",
			reason = "arranque de la pantalla y observación del estado local"
		),
		ActionDisposition.Internal(
			action = "record.Record.ObserveRecord",
			reason = "arranque de la pantalla"
		),
		ActionDisposition.Internal(
			action = "record.Record.EnsureRecordLoaded",
			reason = "la entrada a la ruta conserva la recarga inicial del historial mientras gana el contenido local"
		),
		ActionDisposition.Internal(
			action = "record.Schedule.Resumed",
			reason = "la hoja lo despacha cada vez que vuelve al frente, para leer otra vez el reloj; no lo hace ningún usuario"
		),
		ActionDisposition.Internal(
			action = "record.Schedule.ObserveSchedule",
			reason = "arranque de la pantalla"
		),
		ActionDisposition.Internal(
			action = "subjects.SubjectDetail.LoadSubjectDetail",
			reason = "arranque de la ruta de detalle tras elegir una materia"
		),
		ActionDisposition.Internal(
			action = "subjects.SubjectSearch.ObserveSubjectSearch",
			reason = "observación de la búsqueda"
		),
		ActionDisposition.Internal(
			action = "summary.ShareTerm.ObserveShareTerm",
			reason =
				"la hoja pide sus trimestres al abrirse, sobre el trimestre que nombra quien la abre; no lo hace " +
				"ningún usuario"
		),
		ActionDisposition.Internal(
			action = "summary.Summary.ObserveSummary",
			reason = "arranque de la pantalla"
		),
		ActionDisposition.Internal(
			action = "summary.Summary.Resumed",
			reason =
				"la pantalla lo despacha cada vez que vuelve al frente, para leer otra vez el reloj; no lo hace " +
				"ningún usuario"
		),
		ActionDisposition.PlatformEdge(
			action = "summary.Summary.UploadProfilePicture",
			reason =
				"se ejecuta cuando el selector o la cámara del sistema devuelven un archivo, y ningún escenario puede " +
				"poner un archivo en ellos; la subida necesita una fuente de ese archivo solo para depuración"
		),
		ActionDisposition.Internal(
			action = "summary.SummaryStatistics.ObserveStatistics",
			reason = "arranque de la pantalla"
		),
		ActionDisposition.Internal(
			action = "summary.SummaryStatistics.Resumed",
			reason =
				"la pantalla lo despacha cada vez que vuelve al frente, para leer otra vez el reloj; no lo hace ningún " +
				"usuario"
		),
		ActionDisposition.Internal(
			action = "wizard.CoachmarkOverlay.SurfaceChanged",
			reason = "puente del host desde el ViewState real activo hasta la elegibilidad contextual del recorrido"
		)
	)
}
