package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.chatcontract.event.RunErrorCode
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_chat_evaluation_type_attendance
import tuindice.assistant.generated.resources.assistant_chat_evaluation_type_essay
import tuindice.assistant.generated.resources.assistant_chat_evaluation_type_interventions
import tuindice.assistant.generated.resources.assistant_chat_evaluation_type_laboratory
import tuindice.assistant.generated.resources.assistant_chat_evaluation_type_model
import tuindice.assistant.generated.resources.assistant_chat_evaluation_type_other
import tuindice.assistant.generated.resources.assistant_chat_evaluation_type_presentation
import tuindice.assistant.generated.resources.assistant_chat_evaluation_type_project
import tuindice.assistant.generated.resources.assistant_chat_evaluation_type_quiz
import tuindice.assistant.generated.resources.assistant_chat_evaluation_type_report
import tuindice.assistant.generated.resources.assistant_chat_evaluation_type_test
import tuindice.assistant.generated.resources.assistant_chat_evaluation_type_workshop
import tuindice.assistant.generated.resources.assistant_chat_evaluation_type_written_work
import tuindice.assistant.generated.resources.assistant_chat_guided
import tuindice.assistant.generated.resources.assistant_chat_max_grade
import tuindice.assistant.generated.resources.assistant_chat_message_too_long
import tuindice.assistant.generated.resources.assistant_chat_quota_exhausted
import tuindice.assistant.generated.resources.assistant_chat_quota_exhausted_wait
import tuindice.assistant.generated.resources.assistant_chat_screen_about
import tuindice.assistant.generated.resources.assistant_chat_screen_evaluation_detail
import tuindice.assistant.generated.resources.assistant_chat_screen_evaluations
import tuindice.assistant.generated.resources.assistant_chat_screen_pensum
import tuindice.assistant.generated.resources.assistant_chat_screen_record
import tuindice.assistant.generated.resources.assistant_chat_screen_statistics
import tuindice.assistant.generated.resources.assistant_chat_screen_subject_detail
import tuindice.assistant.generated.resources.assistant_chat_screen_subject_search
import tuindice.assistant.generated.resources.assistant_chat_screen_summary
import tuindice.assistant.generated.resources.assistant_chat_screen_term_report
import tuindice.assistant.generated.resources.assistant_chat_send_failed
import tuindice.assistant.generated.resources.assistant_chat_turn_failed_cancelled
import tuindice.assistant.generated.resources.assistant_chat_turn_failed_disabled
import tuindice.assistant.generated.resources.assistant_chat_turn_failed_other
import tuindice.assistant.generated.resources.assistant_chat_turn_failed_other_final
import tuindice.assistant.generated.resources.assistant_chat_turn_failed_quota
import tuindice.assistant.generated.resources.assistant_chat_turn_failed_refused
import tuindice.assistant.generated.resources.assistant_chat_turn_failed_timeout
import tuindice.assistant.generated.resources.assistant_chat_turn_failed_transient
import tuindice.assistant.generated.resources.assistant_chat_undated
import tuindice.assistant.generated.resources.assistant_chat_wait_minutes
import tuindice.assistant.generated.resources.assistant_chat_wait_under_minute

// The chat texts read from the module's compose resources: the app's binding of AssistantChatTexts.
class AssistantChatResourceTexts : AssistantChatTexts {
	override suspend fun screenName(screen: AssistantScreen): String = getString(
		when (screen) {
			AssistantScreen.SUMMARY -> Res.string.assistant_chat_screen_summary
			AssistantScreen.STATISTICS -> Res.string.assistant_chat_screen_statistics
			AssistantScreen.RECORD -> Res.string.assistant_chat_screen_record
			AssistantScreen.PENSUM -> Res.string.assistant_chat_screen_pensum
			AssistantScreen.EVALUATIONS -> Res.string.assistant_chat_screen_evaluations
			AssistantScreen.EVALUATION_DETAIL -> Res.string.assistant_chat_screen_evaluation_detail
			AssistantScreen.SUBJECT_DETAIL -> Res.string.assistant_chat_screen_subject_detail
			AssistantScreen.SUBJECT_SEARCH -> Res.string.assistant_chat_screen_subject_search
			AssistantScreen.ABOUT -> Res.string.assistant_chat_screen_about
			AssistantScreen.TERM_REPORT -> Res.string.assistant_chat_screen_term_report
		}
	)

	override suspend fun evaluationTypeName(evaluationType: String): String {
		return getString(EVALUATION_TYPES[evaluationType] ?: Res.string.assistant_chat_evaluation_type_other)
	}

	override suspend fun maxGrade(maxGrade: Double): String {
		val grade = if (maxGrade % WHOLE_STEP == 0.0) maxGrade.toInt().toString() else maxGrade.toString()

		return getString(Res.string.assistant_chat_max_grade, grade)
	}

	override suspend fun undated(): String = getString(Res.string.assistant_chat_undated)

	override suspend fun guided(): String = getString(Res.string.assistant_chat_guided)

	override suspend fun quotaExhausted(retryAfterSeconds: Int?): String {
		if (retryAfterSeconds == null) return getString(Res.string.assistant_chat_quota_exhausted)

		return getString(Res.string.assistant_chat_quota_exhausted_wait, waitText(retryAfterSeconds))
	}

	override suspend fun turnFailure(code: RunErrorCode, retryable: Boolean): String = getString(
		when (code) {
			RunErrorCode.TRANSIENT -> Res.string.assistant_chat_turn_failed_transient
			RunErrorCode.QUOTA_EXHAUSTED -> Res.string.assistant_chat_turn_failed_quota
			RunErrorCode.TIMEOUT -> Res.string.assistant_chat_turn_failed_timeout
			RunErrorCode.REFUSED -> Res.string.assistant_chat_turn_failed_refused
			RunErrorCode.ASSISTANT_DISABLED -> Res.string.assistant_chat_turn_failed_disabled
			RunErrorCode.CANCELLED -> Res.string.assistant_chat_turn_failed_cancelled
			RunErrorCode.OTHER -> if (retryable) {
				Res.string.assistant_chat_turn_failed_other
			} else {
				Res.string.assistant_chat_turn_failed_other_final
			}
		}
	)

	override suspend fun sendFailed(): String = getString(Res.string.assistant_chat_send_failed)

	override suspend fun messageTooLong(maxLength: Int): String {
		return getString(Res.string.assistant_chat_message_too_long, maxLength)
	}

	private companion object {
		val EVALUATION_TYPES: Map<String, StringResource> = mapOf(
			"TEST" to Res.string.assistant_chat_evaluation_type_test,
			"ESSAY" to Res.string.assistant_chat_evaluation_type_essay,
			"ATTENDANCE" to Res.string.assistant_chat_evaluation_type_attendance,
			"INTERVENTIONS" to Res.string.assistant_chat_evaluation_type_interventions,
			"LABORATORY" to Res.string.assistant_chat_evaluation_type_laboratory,
			"MODEL" to Res.string.assistant_chat_evaluation_type_model,
			"PRESENTATION" to Res.string.assistant_chat_evaluation_type_presentation,
			"PROJECT" to Res.string.assistant_chat_evaluation_type_project,
			"QUIZ" to Res.string.assistant_chat_evaluation_type_quiz,
			"REPORT" to Res.string.assistant_chat_evaluation_type_report,
			"WORKSHOP" to Res.string.assistant_chat_evaluation_type_workshop,
			"WRITTEN_WORK" to Res.string.assistant_chat_evaluation_type_written_work,
			"OTHER" to Res.string.assistant_chat_evaluation_type_other
		)
	}
}

// "en 5 min", or "en menos de un minuto" for a wait under a minute.
private suspend fun waitText(seconds: Int): String {
	if (seconds <= SECONDS_PER_MINUTE) return getString(Res.string.assistant_chat_wait_under_minute)

	val minutes = (seconds + SECONDS_PER_MINUTE - 1) / SECONDS_PER_MINUTE

	return getString(Res.string.assistant_chat_wait_minutes, minutes)
}

private const val SECONDS_PER_MINUTE = 60
private const val WHOLE_STEP = 1.0
