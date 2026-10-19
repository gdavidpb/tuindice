package com.gdavidpb.tuindice.summary.data.source

import com.gdavidpb.tuindice.base.domain.model.ProfileIdentity
import com.gdavidpb.tuindice.base.domain.repository.ProfileIdentityRepository
import com.gdavidpb.tuindice.summary.data.mapper.toProfileIdentity
import com.gdavidpb.tuindice.summary.data.repository.user.LocalDataRepository
import com.gdavidpb.tuindice.summary.data.repository.user.SettingsDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

// Who the student is, read from the stored user the summary observes: the shell and the Settings screen show the
// picture and the name the app already has, without a request of their own. The picture version rides along, as in
// the summary, so a re-upload behind the same URL still re-emits. With no stored user it says null instead of staying
// silent.
class ProfileIdentityDataSource(
	private val localDataSource: LocalDataRepository,
	private val settingsDataSource: SettingsDataRepository
) : ProfileIdentityRepository {
	override fun observeProfileIdentity(): Flow<ProfileIdentity?> {
		return combine(
			localDataSource.getUserFlow(),
			settingsDataSource.observeProfilePictureVersion()
		) { localUser, pictureVersion ->
			localUser?.copy(pictureVersion = pictureVersion)?.toProfileIdentity()
		}.distinctUntilChanged()
	}
}
