package com.example.isthisahangout.room.manga

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.isthisahangout.models.RoomMangaByGenreRemoteKey

@Dao
interface MangaRemoteKeyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMangaByGenreRemoteKey(key: RoomMangaByGenreRemoteKey)

    @Query("SELECT * FROM manga_by_genre_remote_key WHERE genre = :query")
    suspend fun getMangaByGenreRemoteKey(query: String): RoomMangaByGenreRemoteKey

}