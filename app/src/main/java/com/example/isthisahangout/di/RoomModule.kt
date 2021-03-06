package com.example.isthisahangout.di

import android.app.Application
import androidx.room.Room
import com.example.isthisahangout.room.anime.*
import com.example.isthisahangout.room.chat.ChatDao
import com.example.isthisahangout.room.chat.ChatDatabase
import com.example.isthisahangout.room.favourites.FavouritesDao
import com.example.isthisahangout.room.favourites.FavouritesDatabase
import com.example.isthisahangout.room.games.GameDatabase
import com.example.isthisahangout.room.games.GamesDao
import com.example.isthisahangout.room.games.GamesRemoteKeyDao
import com.example.isthisahangout.room.manga.MangaDao
import com.example.isthisahangout.room.manga.MangaDatabase
import com.example.isthisahangout.room.manga.MangaRemoteKeyDao
import com.example.isthisahangout.room.posts.PostDatabase
import com.example.isthisahangout.room.posts.PostsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    ///Anime
    @Provides
    @Singleton
    fun providesAnimeDatabase(app: Application): AnimeDatabase =
        Room.databaseBuilder(app, AnimeDatabase::class.java, "Anime Database")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun providesGamingDatabase(app: Application): GameDatabase =
        Room.databaseBuilder(app, GameDatabase::class.java, "Game Database")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun providesFavDatabase(app: Application): FavouritesDatabase =
        Room.databaseBuilder(app, FavouritesDatabase::class.java, "Fav Database")
            .fallbackToDestructiveMigration()
            .build()


    @Provides
    @Singleton
    fun providesUpcomingAnimeDao(db: AnimeDatabase): UpcomingAnimeDao = db.getUpcomingAnimeDao()

    @Provides
    @Singleton
    fun providesAiringAnimeDao(db: AnimeDatabase): AiringAnimeDao = db.getAiringAnimeDoa()

    @Provides
    @Singleton
    fun providesUpcomingAnimeDoa(db: AnimeDatabase): UpcomingAnimeRemoteKeyDao =
        db.getUpcomingAnimeRemoteKeyDao()

    @Provides
    @Singleton
    fun providesAiringAnimeDoa(db: AnimeDatabase): AiringAnimeRemoteKeyDao =
        db.getAiringAnimeRemoteKeyDao()

    @Provides
    @Singleton
    fun providesAnimeByGenreDao(db: AnimeDatabase): AnimeGenreDao = db.getAnimeByGenreDao()

    @Provides
    @Singleton
    fun providesAnimeByGenreKeyDoa(db: AnimeDatabase): AnimeGenreKeyDao = db.getAnimeByGenreKeyDao()

    @Provides
    @Singleton
    fun providesAnimeBySeasonDao(db: AnimeDatabase): AnimeBySeasonDao = db.getAnimeBySeasonDao()

    @Provides
    @Singleton
    fun providesAnimeQuoteDao(db: AnimeDatabase): AnimeQuoteDao = db.getAnimeQuoteDao()

    @Provides
    @Singleton
    fun providesAnimeByNameDao(db: AnimeDatabase): AnimeSearchByNameDao = db.getAnimeByNameDao()

    @Provides
    @Singleton
    fun providesAnimeByDayDao(db: AnimeDatabase): AnimeByDayDao = db.getAnimeByDayDao()

    @Provides
    @Singleton
    fun providesAnimePicsDao(db: AnimeDatabase): AnimePicsDao = db.getAnimePicsDao()

    @Provides
    @Singleton
    fun providesAnimeNewsDao(db: AnimeDatabase): AnimeNewsDao = db.getAnimeNewsDao()

    ////Games
    @Provides
    @Singleton
    fun providesGamingDao(db: GameDatabase): GamesDao = db.getGamesDao()

    @Provides
    @Singleton
    fun providesGamingRemoteKeyDao(db: GameDatabase): GamesRemoteKeyDao = db.getGamesRemoteKeyDao()

    ////Manga
    @Provides
    @Singleton
    fun providesMangaDatabase(app: Application): MangaDatabase =
        Room.databaseBuilder(app, MangaDatabase::class.java, "manga_database")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun providesMangaDao(db: MangaDatabase): MangaDao = db.getMangaDao()

    @Provides
    @Singleton
    fun providesMangaRemoteKeyDao(db: MangaDatabase): MangaRemoteKeyDao = db.getMangaRemoteKeyDao()


    ///Posts
    @Provides
    @Singleton
    fun providesPostsDatabase(app: Application): PostDatabase =
        Room.databaseBuilder(app, PostDatabase::class.java, "posts_database")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun providesPostsDao(db: PostDatabase): PostsDao = db.getPostsDao()

    //Favourites
    @Provides
    @Singleton
    fun providesPFavDoa(db: FavouritesDatabase): FavouritesDao = db.getFavouritesDao()


    ///Chat
    @Provides
    @Singleton
    fun providesChatDatabase(app: Application): ChatDatabase =
        Room.databaseBuilder(app, ChatDatabase::class.java, "chat_database")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun providesChatDao(db: ChatDatabase): ChatDao = db.getMessagesDao()

}