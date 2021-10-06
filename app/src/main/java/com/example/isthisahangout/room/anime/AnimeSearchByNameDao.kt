package com.example.isthisahangout.room.anime

import androidx.room.*
import com.example.isthisahangout.models.AnimeByNameResults
import com.example.isthisahangout.utils.Resource
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimeSearchByNameDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimeByName(anime: List<AnimeByNameResults.AnimeByName>)

    @Query("SELECT * FROM anime_by_name")
    fun getAnimeByName(): Flow<List<AnimeByNameResults.AnimeByName>>

    @Query("DELETE FROM anime_by_name")
    suspend fun deleteAnimeByName()
}