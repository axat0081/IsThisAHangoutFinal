package com.example.isthisahangout.room.anime

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.isthisahangout.models.*

@Database(
    entities = [UpcomingAnimeResponse.UpcomingAnime::class, AiringAnimeResponse.AiringAnime::class,
        AiringAnimeRemoteKey::class,
        UpcomingAnimeRemoteKey::class,
        RoomAnimeByGenres::class,
        AnimeByGenresRemoteKey::class,
        AnimeSeasonResults.RoomAnimeBySeason::class,
        RoomAnimeQuote::class],
    version = 7
)
abstract class AnimeDatabase : RoomDatabase() {
    abstract fun getUpcomingAnimeDao(): UpcomingAnimeDao
    abstract fun getAiringAnimeDoa(): AiringAnimeDao
    abstract fun getUpcomingAnimeRemoteKeyDao(): UpcomingAnimeRemoteKeyDao
    abstract fun getAiringAnimeRemoteKeyDao(): AiringAnimeRemoteKeyDao
    abstract fun getAnimeByGenreDao(): AnimeGenreDao
    abstract fun getAnimeByGenreKeyDao(): AnimeGenreKeyDao
    abstract fun getAnimeBySeasonDao(): AnimeBySeasonDao
    abstract fun getAnimeQuoteDao(): AnimeQuoteDao
}