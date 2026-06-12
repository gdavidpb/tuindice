package com.gdavidpb.tuindice.base.presentation.statemachine

import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.utils.extension.toSnakeCase
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
		val fromClass = state::class
		val changesState = spec.to != null && spec.to != fromClass

		if (changesState) {
			exitActions[fromClass]?.invoke(state)
		}

		val next = spec.output(state, event)

		if (spec.to != null) {
			check(spec.to.isInstance(next)) {
				"Transition on ${event::class.simpleName} from ${fromClass.simpleName} " +
					"declared target ${spec.to.simpleName} but produced ${next::class.simpleName}"
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

		return TransitionResult.Transitioned(
			toState = next,
			transition = spec
		)
	}

	fun exportToMermaid(machineName: String, initialState: KClass<out S>): String {
		val states = buildList {
			add(initialState)
			transitions.forEach { spec ->
				spec.from?.let(::add)
				spec.to?.let(::add)
			}
		}.distinct()

		return buildString {
			appendLine("stateDiagram-v2")
			appendLine("state $machineName {")
			states.forEach { state ->
				appendLine("    state ${state.stateName()}")
			}
			appendLine()
			appendLine("    [*] --> ${initialState.stateName()}")
			transitions.forEach { spec ->
				val event = spec.on.simpleName
				if (spec.from == null) {
					appendLine("    $machineName --> $machineName : $event")
				} else {
					val from = spec.from.stateName()
					val to = (spec.to ?: spec.from).stateName()
					appendLine("    $from --> $to : $event")
				}
			}
			append("}")
		}
	}

	private fun resolve(state: S, event: Any): TransitionSpec<S>? {
		return transitions.firstOrNull { spec ->
			spec.from != null && spec.from.isInstance(state) && spec.on.isInstance(event)
		} ?: transitions.firstOrNull { spec ->
			spec.from == null && spec.on.isInstance(event)
		}
	}

	private fun KClass<*>.stateName(): String {
		return simpleName?.toSnakeCase() ?: "unknown"
	}
}
