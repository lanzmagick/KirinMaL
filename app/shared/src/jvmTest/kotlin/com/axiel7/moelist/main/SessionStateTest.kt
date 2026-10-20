package com.axiel7.moelist.main

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class SessionStateTest {
    @Test
    fun slowSavedSessionNeverBrieflyReportsLoggedOut() = runBlocking {
        withTimeout(5_000) {
            val storageReady = CompletableDeferred<Unit>()
            val states = mutableListOf<SessionState>()
            val observer = async(start = CoroutineStart.UNDISPATCHED) {
                flow {
                    storageReady.await()
                    emit("saved-session")
                }.sessionStates {}.toList(states)
            }

            assertEquals(listOf(SessionState.LOADING), states)
            storageReady.complete(Unit)
            observer.await()
            assertEquals(listOf(SessionState.LOADING, SessionState.LOGGED_IN), states)
        }
    }

    @Test
    fun loginIsAvailableOnlyAfterAnEmptyStoreFinishesLoading() = runBlocking {
        withTimeout(5_000) {
            val storageReady = CompletableDeferred<Unit>()
            val states = mutableListOf<SessionState>()
            val observer = async(start = CoroutineStart.UNDISPATCHED) {
                flow<String?> {
                    storageReady.await()
                    emit(null)
                }.sessionStates {}.toList(states)
            }

            assertEquals(listOf(SessionState.LOADING), states)
            storageReady.complete(Unit)
            observer.await()
            assertEquals(listOf(SessionState.LOADING, SessionState.LOGGED_OUT), states)
        }
    }

    @Test
    fun legacyMigrationFinishesBeforeCheckingForASavedSession() = runBlocking {
        withTimeout(5_000) {
            val migrationReady = CompletableDeferred<Unit>()
            var savedToken: String? = null
            var storeWasRead = false
            val states = mutableListOf<SessionState>()
            val observer = async(start = CoroutineStart.UNDISPATCHED) {
                flow {
                    storeWasRead = true
                    emit(savedToken)
                }.sessionStates {
                    migrationReady.await()
                    savedToken = "migrated-session"
                }.toList(states)
            }

            assertEquals(listOf(SessionState.LOADING), states)
            assertFalse(storeWasRead)
            migrationReady.complete(Unit)
            observer.await()
            assertEquals(listOf(SessionState.LOADING, SessionState.LOGGED_IN), states)
        }
    }

    @Test
    fun refreshLogoutAndManualLoginKeepWorkingWithoutAnotherStartup() = runBlocking {
        val tokens = Channel<String?>(Channel.UNLIMITED)
        tokens.trySend("saved-session")
        tokens.trySend("refreshed-session")
        tokens.trySend(null)
        tokens.trySend("manual-login")
        tokens.close()
        var migrationCount = 0

        val states = tokens.receiveAsFlow().sessionStates { migrationCount++ }.toList()

        assertEquals(
            listOf(
                SessionState.LOADING,
                SessionState.LOGGED_IN,
                SessionState.LOGGED_OUT,
                SessionState.LOGGED_IN,
            ),
            states,
        )
        assertEquals(1, migrationCount)
    }

    @Test
    fun aFreshInstallReachesTheNormalLoginState() = runBlocking {
        assertEquals(
            listOf(SessionState.LOADING, SessionState.LOGGED_OUT),
            flowOf<String?>(null).sessionStates {}.toList(),
        )
    }
}
