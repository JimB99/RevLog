package com.revlog.app.di

import android.content.Context
import com.revlog.data.local.DatabaseBootstrap
import com.revlog.data.local.RevLogDatabase
import com.revlog.data.repository.SettingsRepository
import com.revlog.data.repository.VehicleRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// TODO(reminders): ReminderScheduler — see docs/reminders.md
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RevLogDatabase =
        DatabaseBootstrap.open(context)

    @Provides
    @Singleton
    fun provideVehicleRepository(database: RevLogDatabase): VehicleRepository =
        VehicleRepository(database)

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository =
        SettingsRepository(context)
}
