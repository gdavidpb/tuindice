package com.gdavidpb.tuindice.summary.data.mapper

import com.gdavidpb.tuindice.base.domain.model.ProfileIdentity
import com.gdavidpb.tuindice.base.domain.model.User

// The name is the short one the summary shows under the picture: the first of the first names and of the last names.
fun User.toProfileIdentity(): ProfileIdentity = ProfileIdentity(
	name = "${firstNames.substringBefore(' ')} ${lastNames.substringBefore(' ')}",
	careerName = careerName,
	carnet = usbId,
	pictureUrl = pictureUrl,
	pictureVersion = pictureVersion
)
