package com.sleeponit.data.local

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PurchaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PurchaseDatabase =
        Room.databaseBuilder(context, PurchaseDatabase::class.java, "sleep_on_it.db").build()

    @Provides
    fun provideDao(db: PurchaseDatabase): PurchaseDao = db.purchaseDao()
}
