package com.example.socialbatterymanager.di

import android.content.Context
import com.example.socialbatterymanager.data.database.AppDatabase
import com.example.socialbatterymanager.data.repository.SecurityManager
import com.example.socialbatterymanager.shared.preferences.PreferencesManager
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideSecurityManager(@ApplicationContext context: Context): SecurityManager =
        SecurityManager.getInstance(context)

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        securityManager: SecurityManager
    ): AppDatabase {
        val passphrase = if (securityManager.isEncryptionEnabled()) {
            securityManager.getDatabasePassphrase() ?: securityManager.generateDatabasePassphrase()
        } else {
            null
        }
        return AppDatabase.getDatabase(context, passphrase)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager =
        PreferencesManager(context)
}
