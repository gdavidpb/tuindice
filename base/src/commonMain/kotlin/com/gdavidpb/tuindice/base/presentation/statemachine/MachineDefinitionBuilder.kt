package com.gdavidpb.tuindice.base.presentation.statemachine

import com.gdavidpb.tuindice.base.presentation.ViewState
import kotlin.reflect.KClass

@MachineDsl
class MachineDefinitionBuilder<S : ViewState> {
	@PublishedApi
	internal val transitions = mutableListOf<TransitionSpec<S>>()

	@PublishedApi
	internal val enterActions = mutableMapOf<KClass<out S>, suspend (S) -> Unit>()

	@PublishedApi
	internal val exitActions = mutableMapOf<KClass<out S>, suspend (S) -> Unit>()

	inline fun <reified F : S> from(block: StateTransitions<S, F>.() -> Unit) {
		StateTransitions(
			fromClass = F::class,
			builder = this
		).block()
	}

	fun fromAny(block: AnyStateTransitions<S>.() -> Unit) {
		AnyStateTransitions(builder = this).block()
	}

	fun build(): MachineDefinition<S> {
		val duplicated = transitions
			.groupBy { spec -> spec.from to spec.on }
			.filterValues { rows -> rows.size > 1 }
			.keys

		require(duplicated.isEmpty()) {
			"Duplicate transition rows (only the first would ever run): " +
				duplicated.joinToString { (from, on) ->
					"${from?.simpleName ?: "*"} × ${on.simpleName}"
				}
		}

		return MachineDefinition(
			transitions = transitions.toList(),
			enterActions = enterActions.toMap(),
			exitActions = exitActions.toMap()
		)
	}

	@MachineDsl
	class StateTransitions<S : ViewState, F : S>(
		@PublishedApi
		internal val fromClass: KClass<F>,
		@PublishedApi
		internal val builder: MachineDefinitionBuilder<S>
	) {
		/**
		 * Internal transition: f must return the same state class, enforced at
		 * compile time by the (F, E) -> F signature.
		 */
		inline fun <reified E : Any> on(noinline output: suspend (F, E) -> F) {
			builder.transitions += TransitionSpec(
				from = fromClass,
				on = E::class,
				to = null,
				output = { state, event ->
					@Suppress("UNCHECKED_CAST")
					output(state as F, event as E)
				}
			)
		}

		/**
		 * State-changing transition: the declared target [To] is enforced at
		 * compile time by the (F, E) -> To signature.
		 */
		inline fun <reified E : Any, reified To : S> onTo(noinline output: suspend (F, E) -> To) {
			builder.transitions += TransitionSpec(
				from = fromClass,
				on = E::class,
				to = To::class,
				output = { state, event ->
					@Suppress("UNCHECKED_CAST")
					output(state as F, event as E)
				}
			)
		}

		fun onEnter(action: suspend (F) -> Unit) {
			require(fromClass !in builder.enterActions) {
				"onEnter already declared for ${fromClass.simpleName}"
			}

			builder.enterActions[fromClass] = { state ->
				@Suppress("UNCHECKED_CAST")
				action(state as F)
			}
		}

		fun onExit(action: suspend (F) -> Unit) {
			require(fromClass !in builder.exitActions) {
				"onExit already declared for ${fromClass.simpleName}"
			}

			builder.exitActions[fromClass] = { state ->
				@Suppress("UNCHECKED_CAST")
				action(state as F)
			}
		}
	}

	@MachineDsl
	class AnyStateTransitions<S : ViewState>(
		@PublishedApi
		internal val builder: MachineDefinitionBuilder<S>
	) {
		/**
		 * Machine-level internal transition, valid from any state; the same-class
		 * invariant is enforced at runtime by [MachineDefinition.process].
		 */
		inline fun <reified E : Any> on(noinline output: suspend (S, E) -> S) {
			builder.transitions += TransitionSpec(
				from = null,
				on = E::class,
				to = null,
				output = { state, event ->
					@Suppress("UNCHECKED_CAST")
					output(state, event as E)
				}
			)
		}

		/**
		 * Machine-level state-changing transition, valid from any state; the declared
		 * target [To] is enforced at compile time by the (S, E) -> To signature.
		 */
		inline fun <reified E : Any, reified To : S> onTo(noinline output: suspend (S, E) -> To) {
			builder.transitions += TransitionSpec(
				from = null,
				on = E::class,
				to = To::class,
				output = { state, event ->
					@Suppress("UNCHECKED_CAST")
					output(state, event as E)
				}
			)
		}
	}
}
