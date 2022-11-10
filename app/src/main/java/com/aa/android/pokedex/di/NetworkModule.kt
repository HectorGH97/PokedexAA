package com.aa.android.pokedex.di

import com.aa.android.pokedex.api.PokemonApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject

const val CACHE_CONTROL_HEADER = "Cache-Control"
const val CACHE_CONTROL_NO_CACHE = "no-cache"

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    fun provideMoshi(): Moshi =
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @Provides
    fun provideOkHttpClient(): OkHttpClient =
        createClient()

    fun createClient(): OkHttpClient{
        return OkHttpClient.Builder()
            .addInterceptor {chain->
                val request = chain.request()
                val originalResponse = chain.proceed(request)
                val shouldUseCache = request.header(CACHE_CONTROL_HEADER) != CACHE_CONTROL_NO_CACHE
                if (!shouldUseCache)  originalResponse
                val cacheControl = CacheControl.Builder()
                    .maxAge(10, TimeUnit.MINUTES)
                    .build()
                originalResponse.newBuilder()
                    .header(CACHE_CONTROL_HEADER, cacheControl.toString())
                    .build()
            }.build()
    }

    @Provides
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi
    ): PokemonApi =
        Retrofit.Builder()
        .baseUrl("https://pokeapi.co/api/v2/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(okHttpClient)
        .build()
        .create(PokemonApi::class.java)
}