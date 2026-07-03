package com.opensplit.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.opensplit.DataDir
import okio.Path.Companion.toPath

fun createDataStore(dataDir: DataDir): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { "${dataDir.dir}/preferences.preferences_pb".toPath() }
    )
