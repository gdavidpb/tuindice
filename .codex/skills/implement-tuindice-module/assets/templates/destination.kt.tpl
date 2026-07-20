package $PACKAGE.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import kotlinx.serialization.Serializable

@Serializable
sealed class $DESTINATION_NAME : Destination() {
	@Serializable
	data object $FEATURE_NAME : $DESTINATION_NAME()
}
