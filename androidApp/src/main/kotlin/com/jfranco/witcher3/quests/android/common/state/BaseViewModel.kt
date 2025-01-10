package com.jfranco.witcher3.quests.android.common.state

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// TODO: add error handling
abstract class BaseViewModel<State, Action>(
    initialState: State,
) : ViewModel() {

    private val actions =
        MutableSharedFlow<Action>(
            replay = 0,
            extraBufferCapacity = Int.MAX_VALUE,
            onBufferOverflow = BufferOverflow.SUSPEND
        )

    private val ref = atomic(initialState)
    private val mutex = Mutex()

    protected open suspend fun initialization(): Flow<State> = emptyFlow()

    protected abstract suspend fun handle(state: State, action: Action): State

    @OptIn(ExperimentalCoroutinesApi::class)
    private val stateFlow =
        flow {
            val initialization = initialization()
            val actions = actions.map { action ->
                state { handle(it, action) }
            }
            emitAll(listOf(initialization, actions).asFlow().flattenMerge())
        }
            .distinctUntilChanged()

    @Composable
    fun collectStateWithLifecycle() = stateFlow.collectAsStateWithLifecycle(ref.value)

    protected suspend fun state(block: suspend (State) -> State): State {
        return mutex.withLock {
            val ste = ref.value
            block(ste).also {
                ref.value = it
            }
        }
    }

    fun emit(action: Action) {
        actions.tryEmit(action)
            .not().also { if (it) Log.e("APP", "Failed to emit action: $action") }
    }
}