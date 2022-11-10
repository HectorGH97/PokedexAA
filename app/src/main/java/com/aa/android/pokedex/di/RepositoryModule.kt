package com.aa.android.pokedex.di

import com.aa.android.pokedex.repository.PokemonRepository
import com.aa.android.pokedex.repository.PokemonRepositoryI
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun providesRepository(
        pokemonRepositoryI: PokemonRepositoryI
    ):PokemonRepository
}