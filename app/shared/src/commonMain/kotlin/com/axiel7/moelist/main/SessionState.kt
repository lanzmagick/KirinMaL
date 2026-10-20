package com.axiel7.moelist.main

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

enum class SessionState {
    LOADING,
    LOGGED_OUT,
    LOGGED_IN,
}

internal fun <T> Flow<T?>.sessionStates(
    migrateLegacyData: suspend () -> Unit,
): Flow<SessionState> = map { tokens ->
    if (tokens == null) SessionState.LOGGED_OUT else SessionState.LOGGED_IN
}.distinctUntilChanged().onStart {
    emit(SessionState.LOADING)
    // Finish the existing migration before deciding whether there is a saved session.
    migrateLegacyData()
}
