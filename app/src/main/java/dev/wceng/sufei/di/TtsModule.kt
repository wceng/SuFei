package dev.wceng.sufei.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.wceng.sufei.data.tts.TtsManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TtsModule {
    
    @Provides
    @Singleton
    fun provideTtsManager(@ApplicationContext context: Context): TtsManager {
        return TtsManager(context)
    }
}
