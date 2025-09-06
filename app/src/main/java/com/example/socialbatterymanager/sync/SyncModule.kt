package com.example.socialbatterymanager.sync

import android.content.Context
import androidx.work.WorkManager
import com.example.socialbatterymanager.shared.preferences.PreferencesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SyncModule {
    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)

    @Provides
    @Singleton
    fun provideSyncManager(
        workManager: WorkManager,
        preferencesManager: PreferencesManager
    ): SyncManager = SyncManager(workManager, preferencesManager)
}
