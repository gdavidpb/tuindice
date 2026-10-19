package com.gdavidpb.tuindice.base.domain.model

/**
 * Who the signed-in student is, as the shell and the Settings screen show it: the name they go by, their career, their
 * carnet and the profile picture the app already holds. [pictureUrl] is empty when the student has no picture, and
 * [pictureVersion] rolls the image cache identity when a new picture keeps the same URL.
 */
data class ProfileIdentity(
	val name: String,
	val careerName: String,
	val carnet: String,
	val pictureUrl: String,
	val pictureVersion: Int
)
