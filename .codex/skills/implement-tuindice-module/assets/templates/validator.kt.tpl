package $PACKAGE.domain.usecase.validator

import com.gdavidpb.tuindice.base.domain.usecase.base.ParamsValidator

data class $PARAMS_NAME(
	val value: String?
)

class $VALIDATOR_NAME : ParamsValidator<$PARAMS_NAME> {
	override fun validate(params: $PARAMS_NAME) {
		require(!params.value.isNullOrBlank()) {
			"$FEATURE_NAME params require a non-blank value."
		}
	}
}
