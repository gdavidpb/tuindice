package com.gdavidpb.tuindice.base.presentation.statemachine

import com.gdavidpb.tuindice.base.presentation.ViewState
import kotlin.reflect.KClass

/**
 * The formal machine: an introspectable transition table plus enter/exit actions.
 *
 * Resolution is hierarchical: state-specific transitions win over machine-level
 * ([TransitionSpec.from] == null) ones. Events with no matching row are rejected,
 * never partially applied.
 */
class MachineDefinition<S : ViewState>(
	private val transitions: List<TransitionSpec<S>>,
	private val enterActions: Map<KClass<out S>, suspend (S) -> Unit>,
	private val exitActions: Map<KClass<out S>, suspend (S) -> Unit>
) {
	companion object {
		fun <S : ViewState> define(
			block: MachineDefinitionBuilder<S>.() -> Unit
		): MachineDefinition<S> {
			return MachineDefinitionBuilder<S>().apply(block).build()
		}
	}

	val table: List<TransitionSpec<S>>
		get() = transitions

	suspend fun process(state: S, event: Any): TransitionResult<S> {
		val spec = resolve(state, event) ?: return TransitionResult.Rejected()

		return TransitionResult.Transitioned(
			toState = apply(transition = spec, state = state, event = event),
			transition = spec
		)
	}

	fun resolve(state: S, event: Any): TransitionSpec<S>? {
		return transitions.firstOrNull { spec ->
			spec.from != null && spec.from.isInstance(state) && spec.on.isInstance(event)
		} ?: transitions.firstOrNull { spec ->
			spec.from == null && spec.on.isInstance(event)
		}
	}

	suspend fun apply(transition: TransitionSpec<S>, state: S, event: Any): S {
		val fromClass = state::class
		val changesState = transition.to != null && transition.to != fromClass

		if (changesState) {
			exitActions[fromClass]?.invoke(state)
		}

		val next = transition.output(state, event)

		if (transition.to != null) {
			check(transition.to.isInstance(next)) {
				"Transition on ${event::class.simpleName} from ${fromClass.simpleName} " +
					"declared target ${transition.to.simpleName} but produced ${next::class.simpleName}"
			}
		} else {
			check(next::class == fromClass) {
				"Internal transition on ${event::class.simpleName} must stay in " +
					"${fromClass.simpleName} but produced ${next::class.simpleName}"
			}
		}

		if (changesState) {
			enterActions[next::class]?.invoke(next)
		}

		return next
	}
}
