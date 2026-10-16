package com.axiel7.moelist.screens.magic

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.axiel7.moelist.data.model.ui.MagicPreferences
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import java.io.File
import java.nio.file.Files
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MagicPreferencesTest {
    private suspend fun withRepository(
        file: File,
        block: suspend (DefaultPreferencesRepository) -> Unit,
    ) {
        val job = SupervisorJob()
        try {
            val store = PreferenceDataStoreFactory.create(
                scope = CoroutineScope(Dispatchers.IO + job),
                produceFile = { file },
            )
            block(DefaultPreferencesRepository(store))
        } finally { job.cancelAndJoin() }
    }

    private fun withFile(block: suspend (File) -> Unit) = runBlocking {
        val directory = Files.createTempDirectory("kirinmal-magic-preferences").toFile()
        try { block(File(directory, "settings.preferences_pb")) }
        finally { directory.deleteRecursively() }
    }

    @Test
    fun newSettingsKeepMagicEnabledWithoutChangingTheRandomDefault() = withFile { file ->
        withRepository(file) { repository ->
            assertEquals(MagicPreferences(), repository.magicPreferences.first())
            assertFalse(repository.randomListEntryEnabled.first())
        }
    }

    @Test
    fun listAndEffectsChoicesAreIndependentAndSurviveReopeningTheStore() = withFile { file ->
        withRepository(file) { repository ->
            repository.setMagicOnLists(false)
            assertEquals(MagicPreferences(showOnLists = false), repository.magicPreferences.first())
            repository.setMagicEffectsEnabled(false)
            assertEquals(MagicPreferences(showOnLists = false, effectsEnabled = false),
                repository.magicPreferences.first())
            repository.setRandomListEntryEnabled(true)
        }
        withRepository(file) { repository ->
            assertEquals(MagicPreferences(showOnLists = false, effectsEnabled = false),
                repository.magicPreferences.first())
            assertTrue(repository.randomListEntryEnabled.first())
            repository.setMagicOnLists(true)
            assertEquals(MagicPreferences(effectsEnabled = false), repository.magicPreferences.first())
        }
    }

    @Test
    fun masterAndRandomChangesPreserveTheSavedListAndEffectsChoices() = withFile { file ->
        withRepository(file) { repository ->
            repository.setMagicOnLists(false)
            repository.setMagicEffectsEnabled(false)
            repository.setMagicEnabled(false)
            assertEquals(MagicPreferences(enabled = false, showOnLists = false, effectsEnabled = false),
                repository.magicPreferences.first())
            repository.setRandomListEntryEnabled(true)
            repository.setRandomListEntryEnabled(false)
            repository.setMagicEnabled(true)
            assertEquals(MagicPreferences(showOnLists = false, effectsEnabled = false),
                repository.magicPreferences.first())
            assertFalse(repository.randomListEntryEnabled.first())
        }
    }
}
