package com.example.isthisahangout.remotemediator

import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.isthisahangout.api.AnimeAPI
import com.example.isthisahangout.models.RoomMangaByGenre
import com.example.isthisahangout.models.RoomMangaByGenreRemoteKey
import com.example.isthisahangout.room.manga.MangaDatabase
import retrofit2.HttpException
import java.io.IOException

private const val START_INDEX = 1

class MangaByGenreRemoteMediator(
    private val genre: String,
    private val animeAPI: AnimeAPI,
    private val mangaDb: MangaDatabase
) : RemoteMediator<Int, RoomMangaByGenre>() {
    private val mangaDao = mangaDb.getMangaDao()
    private val mangaRemoteKeyDao = mangaDb.getMangaRemoteKeyDao()
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, RoomMangaByGenre>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> START_INDEX
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> mangaRemoteKeyDao.getMangaByGenreRemoteKey(genre).nextPageKey
        }
        try {
            val response = animeAPI.getMangaByGenre(page = page, genre = genre)
            val serverMangaResults = response.results
            val mangas = serverMangaResults.map { manga ->
                RoomMangaByGenre(
                    id = manga.id,
                    imageUrl = manga.imageUrl,
                    synopsis = manga.synopsis,
                    title = manga.title,
                    url = manga.url,
                    genre = genre
                )
            }
            mangaDb.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    mangaDao.deleteMangaByGenre(genre)
                }
                val nextPage = page + 1
                mangaRemoteKeyDao.insertMangaByGenreRemoteKey(
                    RoomMangaByGenreRemoteKey(
                        genre = genre,
                        nextPageKey = nextPage
                    )
                )
                mangaDao.insertRoomMangaByGenre(mangas)
            }
            return MediatorResult.Success(endOfPaginationReached = serverMangaResults.isEmpty())
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        }
    }
}