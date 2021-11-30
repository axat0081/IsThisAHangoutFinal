package com.example.isthisahangout.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import com.example.isthisahangout.api.AnimeAPI
import com.example.isthisahangout.api.AnimePicsAPI
import com.example.isthisahangout.api.AnimeQuoteAPI
import com.example.isthisahangout.models.*
import com.example.isthisahangout.remotemediator.AiringAnimeRemoteMediator
import com.example.isthisahangout.remotemediator.AnimeByGenreRemoteMediator
import com.example.isthisahangout.remotemediator.UpcomingAnimeRemoteMediator
import com.example.isthisahangout.room.anime.*
import com.example.isthisahangout.utils.Resource
import com.example.isthisahangout.utils.networkBoundResource
import com.example.isthisahangout.utils.normalNetworkBoundResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.Jsoup
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnimeRepository @Inject constructor(
    val api: AnimeAPI,
    val db: AnimeDatabase,
    private val quoteAPI: AnimeQuoteAPI,
    private val animePicsAPI: AnimePicsAPI,
    private val upcomingAnimeDao: UpcomingAnimeDao,
    private val airingAnimeDao: AiringAnimeDao,
    private val animeGenreDao: AnimeGenreDao,
    private val animeBySeasonDao: AnimeBySeasonDao,
    private val animeQuoteDoa: AnimeQuoteDao,
    private val animeByNameDao: AnimeSearchByNameDao,
    private val animeByDayDao: AnimeByDayDao,
    private val animePicsDao: AnimePicsDao,
    private val animeNewsDao: AnimeNewsDao
) {


    fun getUpcomingAnime(): Flow<PagingData<UpcomingAnimeResponse.UpcomingAnime>> =
        Pager(
            config = PagingConfig(
                pageSize = 10,
                maxSize = 40
            ),
            remoteMediator = UpcomingAnimeRemoteMediator(api, db),
            pagingSourceFactory = { upcomingAnimeDao.getAnime() }
        ).flow

    fun getAiringAnime(): Flow<PagingData<AiringAnimeResponse.AiringAnime>> =
        Pager(
            config = PagingConfig(
                pageSize = 10,
                maxSize = 40
            ),
            remoteMediator = AiringAnimeRemoteMediator(api, db),
            pagingSourceFactory = { airingAnimeDao.getAnime() }
        ).flow

    fun getAnimeByGenres(query: String): Flow<PagingData<RoomAnimeByGenres>> =
        Pager(
            config = PagingConfig(
                pageSize = 5,
                maxSize = 200
            ),
            remoteMediator = AnimeByGenreRemoteMediator(query = query, api = api, db = db),
            pagingSourceFactory = { animeGenreDao.getAnimeByGenre(query) }
        ).flow

    fun getAnimeBySeason(
        season: String,
        year: String,
        forceRefresh: Boolean,
        onFetchSuccess: () -> Unit,
        onFetchFailed: (Throwable) -> Unit
    ): Flow<Resource<List<AnimeSeasonResults.RoomAnimeBySeason>>> =
        networkBoundResource(
            query = {
                animeBySeasonDao.getAnimeBySeason(season, year)
            },
            fetch = {
                val serverList = api.getAnimeBySeason(season, year).anime
                val animeList = serverList.map {
                    AnimeSeasonResults.RoomAnimeBySeason(
                        id = it.id,
                        imageUrl = it.imageUrl,
                        title = it.title,
                        url = it.url,
                        synopsis = it.synopsis,
                        season = season,
                        year = year
                    )
                }
                Log.e("Error", animeList.size.toString())
                animeList
            },
            saveFetchResult = {
                db.withTransaction {
                    animeBySeasonDao.deleteAll(season, year)
                    animeBySeasonDao.insertAll(it)
                    Log.e("Error1", it.size.toString())
                }
            },
            shouldFetch = { cachedAnime ->
                if (forceRefresh) {
                    true
                } else {
                    val sortedList = cachedAnime.sortedBy {
                        it.updatedAt
                    }
                    val oldestTimestamp = sortedList.firstOrNull()?.updatedAt
                    val needsRefresh = oldestTimestamp == null ||
                            oldestTimestamp < System.currentTimeMillis() -
                            TimeUnit.MINUTES.toMillis(60)
                    needsRefresh
                }
            },
            onFetchSuccess = onFetchSuccess,
            onFetchFailed = { t ->
                if (t !is HttpException && t !is IOException) {
                    throw t
                }
                onFetchFailed(t)
            }
        )

    fun getAnimeQuote(
        forceRefresh: Boolean,
        onFetchSuccess: () -> Unit,
        onFetchFailed: (Throwable) -> Unit
    ): Flow<Resource<List<RoomAnimeQuote>>> =
        networkBoundResource(
            query = {
                animeQuoteDoa.getQuotes()
            },
            fetch = {
                val serverList = quoteAPI.getAnimeQuotes()
                val animeQuotes = serverList.map {
                    RoomAnimeQuote(
                        quote = it.quote,
                        character = it.character,
                        anime = it.anime
                    )
                }
                animeQuotes
            },
            saveFetchResult = {
                db.withTransaction {
                    animeQuoteDoa.delete()
                    animeQuoteDoa.insert(it)
                }
            },
            shouldFetch = { cachedQuotes ->
                if (forceRefresh) {
                    true
                } else {
                    val sortedList = cachedQuotes.sortedBy {
                        it.updatedAt
                    }
                    val oldestTimestamp = sortedList.firstOrNull()?.updatedAt
                    val needsRefresh = oldestTimestamp == null ||
                            oldestTimestamp < System.currentTimeMillis() -
                            TimeUnit.MINUTES.toMillis(60)
                    needsRefresh
                }
            },
            onFetchSuccess = onFetchSuccess,
            onFetchFailed = { t ->
                if (t !is HttpException && t !is IOException) {
                    throw t
                }
                onFetchFailed(t)
            }
        )

    fun getAnimeByName(
        query: String
    ): Flow<Resource<List<AnimeByNameResults.AnimeByName>>> = normalNetworkBoundResource(
        query = {
            animeByNameDao.getAnimeByName()
        },
        fetch = {
            api.getAnimebyName(query)
        },
        saveFetchResult = { animeList ->
            db.withTransaction {
                animeByNameDao.deleteAnimeByName()
                animeByNameDao.insertAnimeByName(animeList.results)
            }
        }
    )

    fun getAnimeByDay(
        query: String
    ) = normalNetworkBoundResource(
        query = {
            animeByDayDao.getAnimeByDay(query)
        },
        fetch = {
            api.getAnimeByDay(query)
        },
        saveFetchResult = { animeResults ->
            Log.e("Day", query)
            val animeList = when (query) {
                "monday" -> animeResults.monday
                "tuesday" -> animeResults.tuesday
                "wednesday" -> animeResults.wednesday
                "thursday" -> animeResults.thursday
                "friday" -> animeResults.friday
                "saturday" -> animeResults.saturday
                "sunday" -> animeResults.sunday
                else -> animeResults.sunday
            }
            db.withTransaction {
                animeByDayDao.deleteAnimeByDay(query)
                animeByDayDao.insertAnimeByDay(animeList.map { anime ->
                    RoomAnimeByDay(
                        id = anime.id,
                        title = anime.title,
                        imageUrl = anime.imageUrl,
                        url = anime.url,
                        synopsis = anime.synopsis,
                        day = query
                    )
                })
            }
        }
    )

    fun getAnimePics(): Flow<List<AnimeImage>> = flow {
        while (true) {
            val animeImages = animePicsAPI.getAnimePics(FieldHolder()).images.map { imageUrl ->
                AnimeImage(imageUrl)
            }
            emit(animeImages)
            kotlinx.coroutines.delay(60000)
        }
    }

    fun getAnimeNews(): Flow<Resource<List<AnimeNews>>> = flow {
        emit(Resource.Loading(data = emptyList<AnimeNews>()))
        val serverData = scrapNews()
        try {
            val serverData = scrapNews()
            val div =
                serverData.getElementsByClass("sentinel-tag-latestArticles browse-half clip-half")
                    .first()
            val articles = div.getElementsByTag("article").map { article ->
                val linkTag = article.getElementsByClass("bc-img-link")
                val url = linkTag.attr("href")
                val imageTag = article.getElementsByTag("source").first()
                val image: String =
                    if (imageTag.hasAttr("srcset"))
                        imageTag.attr("srcset")
                    else
                        imageTag.attr("data-srcset")
                val title = article.getElementsByClass("bc-title-link").text().trim()
                val author = article.getElementsByClass("bc-author").text().trim()
                val desc = article.getElementsByClass("bc-excerpt").text().trim()
                AnimeNews(
                    title = title,
                    image = image,
                    url = url,
                    author = author,
                    desc = desc
                )
            }
            emit(Resource.Success(data = articles))
        } catch (exception: Exception) {
            emit(Resource.Error(throwable = exception, data = emptyList<AnimeNews>()))
        }
    }

    suspend fun scrapNews() = Jsoup.connect("https://www.cbr.com/tag/anime/").get()
}