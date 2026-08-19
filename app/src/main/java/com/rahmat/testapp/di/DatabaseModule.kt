package com.rahmat.testapp.di

import android.content.Context
import androidx.room.Room
import com.rahmat.testapp.data.local.AppDatabase
import com.rahmat.testapp.data.local.dao.CartDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Menjamin database hidup selama aplikasi berjalan
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        // Membuat instance database Room
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "test_app_db"
        ).build()
    }

    @Provides
    fun provideCartDao(database: AppDatabase): CartDao {
        // Mengambil Dao dari instance database yang sudah dibuat di atas
        return database.cartDao()
    }
}