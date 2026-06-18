package com.cubicserenity.esobuildmanager.di

import android.content.Context
import androidx.room.Room
import com.cubicserenity.esobuildmanager.data.local.AppDatabase
import com.cubicserenity.esobuildmanager.data.local.dao.BuildDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "eso_builds.db").build()

    @Provides
    fun provideBuildDao(db: AppDatabase): BuildDao = db.buildDao()
}
