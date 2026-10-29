package com.gdavidpb.tuindice.chatcontract.component

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Screens the assistant can open. The keys are the ones of the backend's help manual. */
@Serializable
public enum class NavTarget {
	@SerialName("summary")
	SUMMARY,

	@SerialName("statistics")
	STATISTICS,

	@SerialName("share_term")
	SHARE_TERM,

	@SerialName("record")
	RECORD,

	@SerialName("record.projection")
	RECORD_PROJECTION,

	@SerialName("record.schedule")
	RECORD_SCHEDULE,

	@SerialName("record.create_term")
	RECORD_CREATE_TERM,

	@SerialName("evaluations")
	EVALUATIONS,

	@SerialName("evaluations.add")
	EVALUATIONS_ADD,

	@SerialName("evaluations.detail")
	EVALUATIONS_DETAIL,

	@SerialName("pensum")
	PENSUM,

	@SerialName("subjects.search")
	SUBJECTS_SEARCH,

	@SerialName("subjects.detail")
	SUBJECTS_DETAIL,

	@SerialName("enrollment_proof")
	ENROLLMENT_PROOF,

	@SerialName("about")
	ABOUT,

	@SerialName("sign_in")
	SIGN_IN,

	@SerialName("update_password")
	UPDATE_PASSWORD
}
