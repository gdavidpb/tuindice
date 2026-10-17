package com.gdavidpb.tuindice.assistant.ui.model

import com.gdavidpb.tuindice.chatcontract.abuse.AbuseReasonCode
import org.jetbrains.compose.resources.StringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_reason_academic_dishonesty
import tuindice.assistant.generated.resources.assistant_reason_automation
import tuindice.assistant.generated.resources.assistant_reason_harassment
import tuindice.assistant.generated.resources.assistant_reason_jailbreak
import tuindice.assistant.generated.resources.assistant_reason_off_domain
import tuindice.assistant.generated.resources.assistant_reason_prompt_extraction
import tuindice.assistant.generated.resources.assistant_reason_spam
import tuindice.assistant.generated.resources.assistant_reason_third_party_data

// The sentence that says why the assistant warned, paused or suspended the student.
fun AbuseReasonCode.textResource(): StringResource =
	when (this) {
		AbuseReasonCode.JAILBREAK -> Res.string.assistant_reason_jailbreak
		AbuseReasonCode.PROMPT_EXTRACTION -> Res.string.assistant_reason_prompt_extraction
		AbuseReasonCode.HARASSMENT -> Res.string.assistant_reason_harassment
		AbuseReasonCode.THIRD_PARTY_DATA -> Res.string.assistant_reason_third_party_data
		AbuseReasonCode.OFF_DOMAIN -> Res.string.assistant_reason_off_domain
		AbuseReasonCode.ACADEMIC_DISHONESTY -> Res.string.assistant_reason_academic_dishonesty
		AbuseReasonCode.SPAM -> Res.string.assistant_reason_spam
		AbuseReasonCode.AUTOMATION -> Res.string.assistant_reason_automation
	}
