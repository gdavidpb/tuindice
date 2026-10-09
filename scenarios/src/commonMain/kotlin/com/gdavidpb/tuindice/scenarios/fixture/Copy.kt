package com.gdavidpb.tuindice.scenarios.fixture

/**
 * The Spanish texts the scenarios look up with `Query.Text`, named once. Each constant is bound in
 * [bindings] to the string resource it comes from; `CopyTest` fails when a text no longer matches
 * its resource, or when a constant has no binding.
 */
object Copy {
	// auth
	const val UsbEmailHint = "Correo USB"
	const val InvalidUsbIdCredentials = "Revisa tu USBID y contraseña"
	const val AccountDisabled = "Cuenta inhabilitada. Escríbenos a info@tuindice.app para recuperarla."
	const val SignInServiceUnavailable = "Servicios de la universidad no disponibles. Vuelve a intentarlo en un momento."
	const val SignOutPendingOne =
		"Tienes 1 cambio pendiente de sincronización. Intentaremos enviarlo antes de cerrar sesión."
	const val SignOutFlushFailedOne = "No pudimos enviar 1 cambio pendiente. Puedes reintentar o cerrar sesión igualmente."
	const val SignOutAndSyncButton = "Enviar y cerrar sesión"
	const val SignOutAnywayButton = "Cerrar igualmente"

	// maincore
	const val ExternalLinkDialogTitle = "Abrir enlace externo"

	/** The link of the privacy page the mock serves (`mocks/__files/e2e/privacy.html`), not the dialog it opens. */
	const val PrivacyPageExternalLink = "Abrir enlace externo"

	const val NoticeTitle = "Servicio no disponible"
	const val NoticeMessage = "Estamos realizando mantenimiento. Intenta nuevamente más tarde."

	// summary
	const val NewStudentNoRecordTitle = "Aún no tienes expediente"
	const val RecordAccessDeniedTitle = "No pudimos consultar tu expediente"
	const val RecordAccessDeniedMessage =
		"La universidad no nos permite consultar tu expediente ahora. Mantenemos tus datos anteriores."
	const val RecordUnavailableSyncMessage =
		"No pudimos actualizar tus notas porque el servicio de la universidad no está disponible. " +
			"Tus datos anteriores se mantienen y volveremos a intentar más tarde."
	const val EnrollmentUnavailableSyncMessage =
		"No pudimos actualizar tu inscripción porque el servicio de la universidad no está disponible. " +
			"Tus datos anteriores se mantienen y volveremos a intentar más tarde."

	// record
	const val EnrollmentAnnulledProvisionalTitle = "Tu inscripción aparece anulada"
	const val EnrollmentAnnulledFinalTitle = "Tu inscripción fue anulada"
	const val StaleEnrollmentMessage = "No pudimos actualizar tu inscripción. Mostramos la última guardada."
	const val AttemptRetired = "Retirada"
	const val ScheduleUnscheduled = "Sin horario"
	const val ScheduleUnscheduledEg1114 = "Sin horario: EG1114"
	const val ScheduleSectionMys116 = "Sec. 1 · MYS-116"
	const val EditTermButton = "Modificar"
	const val EditTermTitle = "Modificar trimestre"
	const val TermSelectedOne = "1 materia seleccionada"
	const val TermSelectedTwo = "2 materias seleccionadas"
	const val SearchSuggestedTitle = "Sugeridas por tu pensum"
	const val RecordSearchNoResults = "No encontramos materias"
	const val SubjectStatusApproved = "Aprobada"
	const val SubjectStatusAvailable = "Disponible"
	const val SubjectStatusBlocked = "Bloqueada"
	const val SubjectCountsAsElective = "Cuenta como electiva"
	const val SubjectCountsAsGeneralStudies = "Cuenta como Estudios Generales"
	const val TooltipApprovedIn = "Cursada en Sep - Dic 2021"
	const val TooltipPlannedIn = "Planificada en Jul - Ago 2026"
	const val TooltipMissingRequirements = "Faltan requisitos: EP1308, EP5855"
	const val TermSepDec2026 = "Sep - Dic 2026"
	const val TermJanMar2027 = "Ene - Mar 2027"

	// enrollmentproof
	const val EnrollmentProofAnnulled = "Tu inscripción aparece anulada, por eso no hay comprobante."

	// pensum
	const val PensumFulfilledBy = "Cursada como"
	const val PensumViewStats = "Ver estadísticas"
	const val PensumSubjectLanguage1 = "Lenguaje I"

	// subjects
	const val SubjectsSearchNoResults = "No encontramos materias"
	const val StudentsTooltipLine1 = "Cantidad de estudiantes"
	const val StudentsTooltipLine2 = "que cursaron esta materia"
	const val AttemptsTooltipLine1 = "Cantidad de veces"
	const val AttemptsTooltipLine2 = "que fue cursada esta materia"

	// evaluations
	const val EvaluationsEnrollmentUnavailableTitle = "Servicio de inscripción no disponible"
	const val EvaluationsEnrollmentUnavailableMessage =
		"El servicio de inscripción de la universidad no responde. " +
			"Cuando se restablezca, tus evaluaciones aparecerán aquí."
	const val EvaluationsNotEnrolledTitle = "No estás inscrito en este trimestre"
	const val EvaluationsNoSubjectsTitle = "Sin trimestre en curso"

	/** The binding of every constant above. */
	val bindings: List<CopyBinding> = listOf(
		CopyBinding.Resource(UsbEmailHint, "auth", "hint_usb_email"),
		CopyBinding.Resource(InvalidUsbIdCredentials, "auth", "error_invalid_usb_id_credentials"),
		CopyBinding.Resource(AccountDisabled, "auth", "error_account_disabled", listOf("info@tuindice.app")),
		CopyBinding.Resource(SignInServiceUnavailable, "auth", "sign_in_service_unavailable"),
		CopyBinding.Resource(SignOutPendingOne, "auth", "dialog_message_sign_out_pending[one]", listOf("1")),
		CopyBinding.Resource(SignOutFlushFailedOne, "auth", "dialog_message_sign_out_flush_failed[one]", listOf("1")),
		CopyBinding.Resource(SignOutAndSyncButton, "auth", "dialog_button_sign_out_and_sync"),
		CopyBinding.Resource(SignOutAnywayButton, "auth", "dialog_button_sign_out_anyway"),
		CopyBinding.Resource(ExternalLinkDialogTitle, "maincore", "dialog_title_warning_external"),
		CopyBinding.Supplied(NoticeTitle, "AVAILABILITY_NOTICE_TITLE launch argument"),
		CopyBinding.Supplied(NoticeMessage, "AVAILABILITY_NOTICE_MESSAGE launch argument"),
		CopyBinding.Resource(NewStudentNoRecordTitle, "base", "new_student_no_record_title"),
		CopyBinding.Resource(RecordAccessDeniedTitle, "summary", "dialog_title_record_access_denied"),
		CopyBinding.Resource(RecordAccessDeniedMessage, "summary", "dialog_message_record_access_denied"),
		CopyBinding.Resource(
			RecordUnavailableSyncMessage,
			"summary",
			"dialog_message_sync_sources_record_unavailable"
		),
		CopyBinding.Resource(
			EnrollmentUnavailableSyncMessage,
			"summary",
			"dialog_message_sync_sources_enrollment_unavailable"
		),
		CopyBinding.Resource(EnrollmentAnnulledProvisionalTitle, "base", "enrollment_annulled_provisional_title"),
		CopyBinding.Resource(EnrollmentAnnulledFinalTitle, "base", "enrollment_annulled_final_title"),
		CopyBinding.Resource(StaleEnrollmentMessage, "record", "record_notice_stale_message_unknown"),
		CopyBinding.Resource(AttemptRetired, "record", "attempt_retired"),
		CopyBinding.Resource(ScheduleUnscheduled, "record", "schedule_table_unscheduled"),
		CopyBinding.Resource(ScheduleUnscheduledEg1114, "record", "schedule_unscheduled", listOf("EG1114")),
		CopyBinding.Resource(ScheduleSectionMys116, "record", "schedule_table_section_classroom", listOf("1", "MYS-116")),
		CopyBinding.Resource(EditTermButton, "record", "edit_term_button"),
		CopyBinding.Resource(EditTermTitle, "record", "top_bar_edit_synthetic_term"),
		CopyBinding.Resource(TermSelectedOne, "record", "create_term_selected_count[one]", listOf("1")),
		CopyBinding.Resource(TermSelectedTwo, "record", "create_term_selected_count[other]", listOf("2")),
		CopyBinding.Resource(SearchSuggestedTitle, "record", "create_term_suggested_title"),
		CopyBinding.Resource(RecordSearchNoResults, "record", "create_term_search_no_results_title"),
		CopyBinding.Resource(SubjectStatusApproved, "record", "create_term_subject_approved"),
		CopyBinding.Resource(SubjectStatusAvailable, "record", "create_term_subject_available"),
		CopyBinding.Resource(SubjectStatusBlocked, "record", "create_term_subject_requirement_pending"),
		CopyBinding.Resource(SubjectCountsAsElective, "record", "create_term_subject_counts_as_elective"),
		CopyBinding.Resource(
			SubjectCountsAsGeneralStudies,
			"record",
			"create_term_subject_counts_as_general_studies"
		),
		CopyBinding.Resource(
			TooltipApprovedIn,
			"record",
			"create_term_subject_tooltip_approved",
			listOf("Sep - Dic 2021")
		),
		CopyBinding.Resource(
			TooltipPlannedIn,
			"record",
			"create_term_subject_tooltip_already_planned",
			listOf("Jul - Ago 2026")
		),
		CopyBinding.Resource(
			TooltipMissingRequirements,
			"record",
			"create_term_subject_tooltip_unavailable",
			listOf("EP1308, EP5855")
		),
		CopyBinding.Derived(TermSepDec2026, "term label the app formats from the SEP_DEC period and the year 2026"),
		CopyBinding.Derived(TermJanMar2027, "term label the app formats from the JAN_MAR period and the year 2027"),
		CopyBinding.Resource(EnrollmentProofAnnulled, "enrollmentproof", "error_enrollment_annulled"),
		CopyBinding.Resource(PensumFulfilledBy, "pensum", "pensum_subject_detail_fulfilled_by"),
		CopyBinding.Resource(PensumViewStats, "pensum", "pensum_subject_detail_stats"),
		CopyBinding.MockData(PrivacyPageExternalLink, "mocks/__files/e2e/privacy.html"),
		CopyBinding.MockData(PensumSubjectLanguage1, "mocks/__files/pensums/get-pensum-equivalence-success.json"),
		CopyBinding.Resource(SubjectsSearchNoResults, "subjects", "subjects_search_no_results_title"),
		CopyBinding.Resource(StudentsTooltipLine1, "subjects", "subjects_segment_students_tooltip_line_1"),
		CopyBinding.Resource(StudentsTooltipLine2, "subjects", "subjects_segment_students_tooltip_line_2"),
		CopyBinding.Resource(AttemptsTooltipLine1, "subjects", "subjects_segment_attempts_tooltip_line_1"),
		CopyBinding.Resource(AttemptsTooltipLine2, "subjects", "subjects_segment_attempts_tooltip_line_2"),
		CopyBinding.Resource(
			EvaluationsEnrollmentUnavailableTitle,
			"evaluations",
			"title_enrollment_unavailable_evaluations"
		),
		CopyBinding.Resource(
			EvaluationsEnrollmentUnavailableMessage,
			"evaluations",
			"message_enrollment_unavailable_evaluations"
		),
		CopyBinding.Resource(EvaluationsNotEnrolledTitle, "evaluations", "title_not_enrolled_evaluations"),
		CopyBinding.Resource(EvaluationsNoSubjectsTitle, "evaluations", "title_no_subjects_evaluations")
	)
}
