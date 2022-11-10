package com.aa.android.pokedex.repository

import com.aa.android.pokedex.api.PokemonApi
import com.aa.android.pokedex.api.entity.PokemonDTO
import javax.inject.Inject

interface PokemonRepository{
    suspend fun getAllPokemon(): List<String>
    suspend fun getPokemonDetail (pokemonName: String): PokemonDTO
}

class PokemonRepositoryI @Inject constructor(
    private val api: PokemonApi
):PokemonRepository {

    override suspend fun getAllPokemon(): List<String> {
        val response = api.getAllPokemon()
        if (response.isSuccessful) {
            response.body()?.let {
                return it.results.map { result ->
                    result.name
                }
            }
        }
        return listOf()
    }

    override suspend fun getPokemonDetail(pokemonName: String): PokemonDTO {
        val response = api.getPokemon(pokemonName)
        if (response.isSuccessful){
            response.body()?.let {
                return it
            } ?: throw Exception("Empty body")
        }else
            throw Exception("No response")
    }
}