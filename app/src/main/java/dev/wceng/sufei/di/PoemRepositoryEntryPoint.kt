package dev.wceng.sufei.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.wceng.sufei.data.repository.PoemRepository

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PoemRepositoryEntryPoint {
    val poemRepository: PoemRepository
}
