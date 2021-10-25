package com.example.isthisahangout.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.isthisahangout.api.AnimeAPI
import com.example.isthisahangout.models.RoomMangaByGenre
import com.example.isthisahangout.remotemediator.MangaByGenreRemoteMediator
import com.example.isthisahangout.room.manga.MangaDatabase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MangaRepository @Inject constructor(
    private val animeAPI: AnimeAPI,
    private val mangaDB: MangaDatabase
) {
    private val mangaByGenreDao = mangaDB.getMangaDao()
    fun getMangaByGenre(genre: String): Flow<PagingData<RoomMangaByGenre>> =
        Pager(
            config = PagingConfig(
                pageSize = 50,
                maxSize = 200
            ),
            remoteMediator = MangaByGenreRemoteMediator(
                genre = genre,
                animeAPI = animeAPI,
                mangaDb = mangaDB
            ),
            pagingSourceFactory = { mangaByGenreDao.getRoomMangaByGenre(genre) }
        ).flow
}