package com.example.isthisahangout.room.manga

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.isthisahangout.models.MangaResults
import com.example.isthisahangout.models.RoomMangaByGenre
import com.example.isthisahangout.models.RoomMangaByGenreRemoteKey

@Database(
    entities = [
        MangaResults.Manga::class,
        RoomMangaByGenre::class,
        RoomMangaByGenreRemoteKey::class
    ], version = 1
)
abstract class MangaDatabase : RoomDatabase() {
    abstract fun getMangaDao(): MangaDao
    abstract fun getMangaRemoteKeyDao(): MangaRemoteKeyDao
}