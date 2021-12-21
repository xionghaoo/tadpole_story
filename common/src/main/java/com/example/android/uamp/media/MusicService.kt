/*
 * Copyright 2017 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.uamp.media

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.MediaBrowserServiceCompat.BrowserRoot.EXTRA_RECENT
import com.example.android.uamp.media.extensions.*
import com.example.android.uamp.media.library.*
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.ext.cast.SessionAvailabilityListener
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSource
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.framework.CastContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit


/**
 * This class is the entry point for browsing and playback commands from the APP's UI
 * and other apps that wish to play music via UAMP (for example, Android Auto or
 * the Google Assistant).
 *
 * Browsing begins with the method [MusicService.onGetRoot], and continues in
 * the callback [MusicService.onLoadChildren].
 *
 * For more information on implementing a MediaBrowserService,
 * visit [https://developer.android.com/guide/topics/media-apps/audio-app/building-a-mediabrowserservice.html](https://developer.android.com/guide/topics/media-apps/audio-app/building-a-mediabrowserservice.html).
 *
 * This class also handles playback for Cast sessions.
 * When a Cast session is active, playback commands are passed to a
 * [CastPlayer](https://exoplayer.dev/doc/reference/com/google/android/exoplayer2/ext/cast/CastPlayer.html),
 * otherwise they are passed to an ExoPlayer for local playback.
 *
 * 媒体服务
 */
abstract class MusicService : MediaBrowserServiceCompat() {

    private lateinit var notificationManager: UampNotificationManager
    private lateinit var mediaSource: MusicSource
    private lateinit var packageValidator: PackageValidator

    // The current player will either be an ExoPlayer (for local playback) or a CastPlayer (for
    // remote playback through a Cast device).
    private lateinit var currentPlayer: Player

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    protected lateinit var mediaSession: MediaSessionCompat
    protected lateinit var mediaSessionConnector: MediaSessionConnector
    private var currentPlaylistItems: List<MediaMetadataCompat> = emptyList()

    private lateinit var storage: PersistentStorage

    /**
     * This must be `by lazy` because the source won't initially be ready.
     * See [MusicService.onLoadChildren] to see where it's accessed (and first
     * constructed).
     *
     * browseTree的get方法在mediaSource whenReady时调用，确保mediaSource已被初始化
     */
    private val browseTree: BrowseTree by lazy {
        BrowseTree(applicationContext, mediaSource)
    }

//    private val dataSourceFactory: DefaultDataSourceFactory by lazy {
//        DefaultDataSourceFactory(
//            /* context= */ this,
//            Util.getUserAgent(/* context= */ this, UAMP_USER_AGENT), /* listener= */
//            null
//        )
//    }

    private lateinit var cacheDataSourceFactory: CacheDataSourceFactory
    private val extractorsFactory = DefaultExtractorsFactory()
        .setConstantBitrateSeekingEnabled(true)
//        .setConstantBitrateSeekingAlwaysEnabled(true)


    private var isForegroundService = false

    private val uAmpAudioAttributes = AudioAttributes.Builder()
        .setContentType(C.CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    private val playerListener = PlayerEventListener()

    /**
     * Configure ExoPlayer to handle audio focus for us.
     * See [Player.AudioComponent.setAudioAttributes] for details.
     */
    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(this).build().apply {
            setAudioAttributes(uAmpAudioAttributes, true)
            setHandleAudioBecomingNoisy(true)
            addListener(playerListener)
        }
    }

    /**
     * 自定义http请求，解决音频加载报416错误的问题
     */
    private val okhttpDataSourceFactory: DefaultDataSource.Factory by lazy {
        DefaultDataSource.Factory(
            this,
            OkHttpDataSource.Factory(
                OkHttpClient.Builder()
                    .connectTimeout(30L, TimeUnit.SECONDS)
                    .readTimeout(30L, TimeUnit.SECONDS)
                    .addInterceptor { chain ->
                        val request = chain.request()
                        val newRequestBuilder = request.newBuilder()
                            // 缓存加载时请求头出现range，服务器有可能会报416
                            .removeHeader("Range")
                        val newRequest = newRequestBuilder.build()
//                        Log.d("CacheDataSourceFactory", "------> ${newRequest.url}")
//                        Log.d("CacheDataSourceFactory", "${newRequest.headers}")
                        val response = chain.proceed(newRequest)
//                        Log.d("CacheDataSourceFactory", "<------${response.code}")
//                        Log.d("CacheDataSourceFactory", "${response.headers}")
                        return@addInterceptor response
                    }
                    .build()
            ).apply {
                setUserAgent(Util.getUserAgent(this@MusicService, packageName))
            }
        )
    }

    /**
     * If Cast is available, create a CastPlayer to handle communication with a Cast session.
     */
    private val castPlayer: CastPlayer? by lazy {
        try {
            val castContext = CastContext.getSharedInstance(this)
            CastPlayer(castContext).apply {
                setSessionAvailabilityListener(UampCastSessionAvailabilityListener())
                addListener(playerListener)
            }
        } catch (e : Exception) {
            // We wouldn't normally catch the generic `Exception` however
            // calling `CastContext.getSharedInstance` can throw various exceptions, all of which
            // indicate that Cast is unavailable.
            // Related internal bug b/68009560.
            Log.i(TAG, "Cast is not available on this device. " +
                    "Exception thrown when attempting to obtain CastContext. " + e.message)
            null
        }
    }

    override fun onCreate() {
        super.onCreate()

        cacheDataSourceFactory = CacheDataSourceFactory(this, 100 * 1024 * 1024, 5 * 1024 * 1024)

        // Create a new MediaSession.
        mediaSession = MediaSessionCompat(this, "MusicService")
            .apply {
                setSessionActivity(createPendingIntent())
                isActive = true
            }

        /**
         * In order for [MediaBrowserCompat.ConnectionCallback.onConnected] to be called,
         * a [MediaSessionCompat.Token] needs to be set on the [MediaBrowserServiceCompat].
         *
         * It is possible to wait to set the session token, if required for a specific use-case.
         * However, the token *must* be set by the time [MediaBrowserServiceCompat.onGetRoot]
         * returns, or the connection will fail silently. (The system will not even call
         * [MediaBrowserCompat.ConnectionCallback.onConnectionFailed].)
         */
        sessionToken = mediaSession.sessionToken

        /**
         * The notification manager will use our player and media session to decide when to post
         * notifications. When notifications are posted or removed our listener will be called, this
         * allows us to promote the service to foreground (required so that we're not killed if
         * the main UI is not visible).
         */
        notificationManager = UampNotificationManager(
            this,
            mediaSession.sessionToken,
            PlayerNotificationListener()
        )

        // The media library is built from a remote JSON file. We'll create the source here,
        // and then use a suspend function to perform the download off the main thread.
        mediaSource = createMusicSource()

        // ExoPlayer will manage the MediaSession for us.
        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(UampPlaybackPreparer())
        mediaSessionConnector.setQueueNavigator(UampQueueNavigator(mediaSession))

        switchToPlayer(
            previousPlayer = null,
            newPlayer = if (castPlayer?.isCastSessionAvailable == true) castPlayer!! else exoPlayer
        )
        notificationManager.showNotificationForPlayer(currentPlayer)

        packageValidator = PackageValidator(this, R.xml.allowed_media_browser_callers)

        storage = PersistentStorage.getInstance(applicationContext)
    }

    /**
     * This is the code that causes UAMP to stop playing when swiping the activity away from
     * recents. The choice to do this is app specific. Some apps stop playback, while others allow
     * playback to continue and allow users to stop it with the notification.
     */
    override fun onTaskRemoved(rootIntent: Intent) {
        saveRecentSongToStorage()
        super.onTaskRemoved(rootIntent)

        /**
         * By stopping playback, the player will transition to [Player.STATE_IDLE] triggering
         * [Player.EventListener.onPlayerStateChanged] to be called. This will cause the
         * notification to be hidden and trigger
         * [PlayerNotificationManager.NotificationListener.onNotificationCancelled] to be called.
         * The service will then remove itself as a foreground service, and will call
         * [stopSelf].
         */
        currentPlayer.stop(/* reset= */true)
    }

    override fun onDestroy() {
        mediaSession.run {
            isActive = false
            release()
        }

        // Cancel coroutines when the service is going away.
        serviceJob.cancel()

        // Free ExoPlayer resources.
        exoPlayer.removeListener(playerListener)
        exoPlayer.release()
    }

    /**
     * Returns the "root" media ID that the client should request to get the list of
     * [MediaItem]s to browse/play.
     * 构建媒体根目录
     */
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        Log.d(TAG, "onGetRoot: $clientPackageName")
        /*
         * By default, all known clients are permitted to search, but only tell unknown callers
         * about search if permitted by the [BrowseTree].
         */
        val isKnownCaller = packageValidator.isKnownCaller(clientPackageName, clientUid)
        val rootExtras = Bundle().apply {
            putBoolean(
                MEDIA_SEARCH_SUPPORTED,
                isKnownCaller || browseTree.searchableByUnknownCaller
            )
            putBoolean(CONTENT_STYLE_SUPPORTED, true)
            putInt(CONTENT_STYLE_BROWSABLE_HINT, CONTENT_STYLE_GRID)
            putInt(CONTENT_STYLE_PLAYABLE_HINT, CONTENT_STYLE_LIST)
        }

        return if (isKnownCaller) {
            /**
             * By default return the browsable root. Treat the EXTRA_RECENT flag as a special case
             * and return the recent root instead.
             */
            val isRecentRequest = rootHints?.getBoolean(EXTRA_RECENT) ?: false
            val browserRootPath = if (isRecentRequest) UAMP_RECENT_ROOT else UAMP_BROWSABLE_ROOT
            BrowserRoot(browserRootPath, rootExtras)
        } else {
            /**
             * Unknown caller. There are two main ways to handle this:
             * 1) Return a root without any content, which still allows the connecting client
             * to issue commands.
             * 2) Return `null`, which will cause the system to disconnect the app.
             *
             * UAMP takes the first approach for a variety of reasons, but both are valid
             * options.
             */
            BrowserRoot(UAMP_EMPTY_ROOT, rootExtras)
        }
    }

    /**
     * Returns (via the [result] parameter) a list of [MediaItem]s that are child
     * items of the provided [parentMediaId]. See [BrowseTree] for more details on
     * how this is build/more details about the relationships.
     *
     * 每次browser subscribe到服务时，都会调用loadChildren方法，这里面可以根据订阅时的mediaId来构建目录树。
     * rootMediaId是固定的，但是其他的id可以自己约定，根据不同的id的异步加载不同媒体目录。
     */
    override fun onLoadChildren(
        parentMediaId: String,
        result: Result<List<MediaItem>>
    ) {
        /**
         * If the caller requests the recent root, return the most recently played song.
         */
        if (parentMediaId == UAMP_RECENT_ROOT) {
            result.sendResult(storage.loadRecentSong()?.let { song -> listOf(song) })
        } else {
            // If the media source is ready, the results will be set synchronously here.
            val resultsSent = mediaSource.whenReady { successfullyInitialized ->
                if (successfullyInitialized) {
                    val children = mediaSource.map { item ->
                        item.description.extras?.putLong("duration", item.duration)
                        item.description.extras?.putLong("trackNumber", item.trackNumber)
                        // MediaMetaData -> MediaItem
                        MediaItem(item.description, item.flag)
                    }
                    result.sendResult(children)
                } else {
                    mediaSession.sendSessionEvent(NETWORK_FAILURE, null)
                    result.sendResult(null)
                }
            }

            // If the results are not ready, the service must "detach" the results before
            // the method returns. After the source is ready, the lambda above will run,
            // and the caller will be notified that the results are ready.
            //
            // See [MediaItemFragmentViewModel.subscriptionCallback] for how this is passed to the
            // UI/displayed in the [RecyclerView].
            if (!resultsSent) {
                result.detach()
            }

            /**
             * 这里需要重置MediaSource的state状态，不然下次用户订阅时，会先直调用loadChildren方法，
             * State此时为STATE_INITIALIZED，whenReady回调会直接调用，把上一次加载的数据返回给客户端
             * 需要等到下次请求完成才会刷新客户端数据。
             */
            mediaSource.reset()
        }
    }

    /**
     * Returns a list of [MediaItem]s that match the given search query
     */
    override fun onSearch(
        query: String,
        extras: Bundle?,
        result: Result<List<MediaItem>>
    ) {
        Log.d(TAG, "onSearch")
        val resultsSent = mediaSource.whenReady { successfullyInitialized ->
            if (successfullyInitialized) {
                val resultsList = mediaSource.search(query, extras ?: Bundle.EMPTY)
                    .map { mediaMetadata ->
                        MediaItem(mediaMetadata.description, mediaMetadata.flag)
                    }
                result.sendResult(resultsList)
            }
        }

        if (!resultsSent) {
            result.detach()
        }
    }

    abstract fun createPendingIntent() : PendingIntent?

    abstract fun createMusicSource() : MusicSource

    /**
     * Load the supplied list of songs and the song to play into the current player.
     */
    private fun preparePlaylist(
        metadataList: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        playWhenReady: Boolean,
        playbackStartPositionMs: Long
    ) {
        Log.d(TAG, "preparePlaylist, ${metadataList.size}, $playWhenReady")

        // Since the playlist was probably based on some ordering (such as tracks
        // on an album), find which window index to play first so that the song the
        // user actually wants to hear plays first.
        val initialWindowIndex = if (itemToPlay == null) 0 else metadataList.indexOf(itemToPlay)
        currentPlaylistItems = metadataList

        currentPlayer.playWhenReady = playWhenReady
        currentPlayer.stop(/* reset= */ true)
        if (currentPlayer == exoPlayer) {
            metadataList.forEach {
                exoPlayer.addMediaSource(it.toMediaSource(okhttpDataSourceFactory))
            }
            exoPlayer.prepare()
            exoPlayer.seekTo(initialWindowIndex, playbackStartPositionMs)
        } else /* currentPlayer == castPlayer */ {
            val items: Array<MediaQueueItem> = metadataList.map {
                it.toMediaQueueItem()
            }.toTypedArray()
//            castPlayer!!.loadItems(
//                items,
//                initialWindowIndex,
//                playbackStartPositionMs,
//                Player.REPEAT_MODE_OFF
//            )
        }
    }

    private fun switchToPlayer(previousPlayer: Player?, newPlayer: Player) {
        if (previousPlayer == newPlayer) {
            return
        }
        currentPlayer = newPlayer
        if (previousPlayer != null) {
            val playbackState = previousPlayer.playbackState
            if (currentPlaylistItems.isEmpty()) {
                // We are joining a playback session. Loading the session from the new player is
                // not supported, so we stop playback.
                currentPlayer.stop(/* reset= */true)
            } else if (playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED) {
                preparePlaylist(
                    metadataList = currentPlaylistItems,
                    itemToPlay = currentPlaylistItems[previousPlayer.currentWindowIndex],
                    playWhenReady = previousPlayer.playWhenReady,
                    playbackStartPositionMs = previousPlayer.currentPosition
                )
            }
        }
        mediaSessionConnector.setPlayer(newPlayer)
        previousPlayer?.stop(/* reset= */true)
    }

    private fun saveRecentSongToStorage() {
        Log.d(TAG, "saveRecentSongToStorage")
        // Obtain the current song details *before* saving them on a separate thread, otherwise
        // the current player may have been unloaded by the time the save routine runs.
        val description = currentPlaylistItems[currentPlayer.currentWindowIndex].description
        val position = currentPlayer.currentPosition

        serviceScope.launch {
            storage.saveRecentSong(
                description,
                position
            )
        }
    }

    fun seekToPosition(posMs: Long) {
        currentPlayer.seekTo(posMs)
    }

    fun seekTo(index: Int) {
        currentPlayer.seekTo(index, 0)
    }

    fun toPrev() : Boolean {
        if (currentPlayer.hasPreviousMediaItem()) {
            currentPlayer.seekToPrevious()
            return true
        } else return false
    }

    fun toNext() : Boolean {
        if (currentPlayer.hasNextMediaItem()) {
            currentPlayer.seekToNext()
            return true
        } else return false
    }

    fun loadMedia(mediaId: String, page: Int, isRefresh: Boolean, isPaging: Boolean) {
        /**
         * 请求之前需要将请求重置为STATE_CREATED，不然loadChildren的whenReady回调会调用两次，
         * 导致sendResult()两次，直接报错
         */
        mediaSource.reset()
        serviceScope.launch {
            mediaSource.load(mediaId, page, isRefresh, isPaging)
            notifyChildrenChanged(mediaId)
        }
    }

    fun getCurrentTrackList(mediaId: String): List<MediaItem> {
        val children = mediaSource.map { item ->
            item.description.extras?.putLong("duration", item.duration)
            item.description.extras?.putLong("trackNumber", item.trackNumber)
            // MediaMetaData -> MediaItem
            MediaItem(item.description, item.flag)
        }
        return children
    }

    fun setPlaybackSpeed(speed: Float) {
        currentPlayer.setPlaybackSpeed(speed)
    }

    fun playForward15s() {
        if (currentPlayer.duration == C.TIME_UNSET) return
        var forwardPos = currentPlayer.currentPosition + 15_000
        if (forwardPos > currentPlayer.duration) forwardPos = currentPlayer.duration
        currentPlayer.seekTo(forwardPos)
    }

    fun playBackward15s() {
        var backPos = currentPlayer.currentPosition - 15_000
        if (backPos < 0) backPos = 0
        currentPlayer.seekTo(backPos)
    }

    fun pause() {
        currentPlayer.pause()
    }

    fun isPlayEnd() : Boolean {
        if (currentPlayer.duration > 0) {
            return currentPlayer.duration - currentPlayer.currentPosition < 500
        } else {
            return false
        }
    }

    abstract fun onPlayStateChange(playWhenReady: Boolean, playbackState: Int)

    private inner class UampCastSessionAvailabilityListener : SessionAvailabilityListener {

        /**
         * Called when a Cast session has started and the user wishes to control playback on a
         * remote Cast receiver rather than play audio locally.
         */
        override fun onCastSessionAvailable() {
            switchToPlayer(currentPlayer, castPlayer!!)
        }

        /**
         * Called when a Cast session has ended and the user wishes to control playback locally.
         */
        override fun onCastSessionUnavailable() {
            switchToPlayer(currentPlayer, exoPlayer)
        }
    }

    private inner class UampQueueNavigator(
        mediaSession: MediaSessionCompat
    ) : TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return currentPlaylistItems[windowIndex].description
        }
    }

    private inner class UampPlaybackPreparer : MediaSessionConnector.PlaybackPreparer {

        /**
         * UAMP supports preparing (and playing) from search, as well as media ID, so those
         * capabilities are declared here.
         *
         * TODO: Add support for ACTION_PREPARE and ACTION_PLAY, which mean "prepare/play something".
         */
        override fun getSupportedPrepareActions(): Long =
            PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH or
                    PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH

        override fun onPrepare(playWhenReady: Boolean) {
            Log.d(TAG, "UampPlaybackPreparer::onPrepare")
            val recentSong = storage.loadRecentSong() ?: return
            onPrepareFromMediaId(
                recentSong.mediaId!!,
                playWhenReady,
                recentSong.description.extras
            )
        }

        /**
         * 播放列表准备好，播放特定曲目时调用，这里不需要检查播放数据的加载状态
         */
        override fun onPrepareFromMediaId(
            mediaId: String,
            playWhenReady: Boolean,
            extras: Bundle?
        ) {
            Log.d(TAG, "UampPlaybackPreparer::onPrepareFromMediaId:$mediaId, $playWhenReady, $extras")
            val itemToPlay: MediaMetadataCompat? = mediaSource.find { item ->
                item.id == mediaId
            }
            if (itemToPlay == null) {
                Log.w(TAG, "Content not found: MediaID=$mediaId")
                // TODO: Notify caller of the error.
            } else {

                val playbackStartPositionMs =
                    extras?.getLong(MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS, C.TIME_UNSET)
                        ?: C.TIME_UNSET

                preparePlaylist(
                    buildPlaylist(itemToPlay),
                    itemToPlay,
                    playWhenReady,
                    playbackStartPositionMs
                )
            }
        }

        /**
         * This method is used by the Google Assistant to respond to requests such as:
         * - Play Geisha from Wake Up on UAMP
         * - Play electronic music on UAMP
         * - Play music on UAMP
         *
         * For details on how search is handled, see [AbstractMusicSource.search].
         */
        override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) {
            Log.d(TAG, "UampPlaybackPreparer::onPrepareFromSearch")
            mediaSource.whenReady {
                val metadataList = mediaSource.search(query, extras ?: Bundle.EMPTY)
                if (metadataList.isNotEmpty()) {
                    preparePlaylist(
                        metadataList,
                        metadataList[0],
                        playWhenReady,
                        playbackStartPositionMs = C.TIME_UNSET
                    )
                }
            }
        }

        override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) = Unit

        override fun onCommand(
            player: Player,
            command: String,
            extras: Bundle?,
            cb: ResultReceiver?
        ): Boolean {
            Log.d(TAG, "UampPlaybackPreparer::onCommand: $command, $extras")

            return false
        }

        /**
         * Builds a playlist based on a [MediaMetadataCompat].
         *
         * TODO: Support building a playlist by artist, genre, etc...
         *
         * @param item Item to base the playlist on.
         * @return a [List] of [MediaMetadataCompat] objects representing a playlist.
         */
        private fun buildPlaylist(item: MediaMetadataCompat): List<MediaMetadataCompat> =
            mediaSource.filter { it.album == item.album }.sortedBy { it.trackNumber }
    }

    /**
     * Listen for notification events.
     */
    private inner class PlayerNotificationListener :
        PlayerNotificationManager.NotificationListener {
        override fun onNotificationPosted(
            notificationId: Int,
            notification: Notification,
            ongoing: Boolean
        ) {
            if (ongoing && !isForegroundService) {
                ContextCompat.startForegroundService(
                    applicationContext,
                    Intent(applicationContext, this@MusicService.javaClass)
                )

                startForeground(notificationId, notification)
                isForegroundService = true
            }
        }

        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            stopForeground(true)
            isForegroundService = false
            stopSelf()
        }
    }

    /**
     * Listen for events from ExoPlayer.
     */
    private inner class PlayerEventListener : Player.Listener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            Log.d(TAG, "PlayerEventListener::onPlayerStateChanged, playbackState: $playWhenReady $playbackState,")
            onPlayStateChange(playWhenReady, playbackState)
            when (playbackState) {
                Player.STATE_BUFFERING,
                Player.STATE_READY -> {
                    notificationManager.showNotificationForPlayer(currentPlayer)
                    if (playbackState == Player.STATE_READY) {

                        // When playing/paused save the current media item in persistent
                        // storage so that playback can be resumed between device reboots.
                        // Search for "media resumption" for more information.
                        saveRecentSongToStorage()

                        if (!playWhenReady) {
                            // If playback is paused we remove the foreground state which allows the
                            // notification to be dismissed. An alternative would be to provide a
                            // "close" button in the notification which stops playback and clears
                            // the notification.
                            stopForeground(false)
                            isForegroundService = false
                        }
                    }
                }
                else -> {
                    notificationManager.hideNotification()
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            // 播放停止时 - 通知栏状态变更
//            notificationManager.hideNotification()
//            notificationManager.showNotificationForPlayer(currentPlayer)
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.d(TAG, "PlayerEventListener::onPlayerError ${error.errorCodeName}, ${error.errorCode}")
            Toast.makeText(
                applicationContext,
                error.message,
                Toast.LENGTH_LONG
            ).show()
            notificationManager.hideNotification()
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            Log.d(TAG, "onPositionDiscontinuity: ${oldPosition.positionMs}, ${newPosition.positionMs}")
            notificationManager.hideNotification()
            notificationManager.showNotificationForPlayer(currentPlayer)

            mediaSession.sendSessionEvent(PLAYER_TRACK_CHANGE, Bundle().apply {
                putInt("reason", reason)
            })
        }

    }
}

/*
 * (Media) Session events
 */
const val NETWORK_FAILURE = "com.example.android.uamp.media.session.NETWORK_FAILURE"
const val PLAYER_TRACK_CHANGE = "com.example.android.uamp.media.session.PLAYER_TRACK_CHANGE"

/** Content styling constants */
private const val CONTENT_STYLE_BROWSABLE_HINT = "android.media.browse.CONTENT_STYLE_BROWSABLE_HINT"
private const val CONTENT_STYLE_PLAYABLE_HINT = "android.media.browse.CONTENT_STYLE_PLAYABLE_HINT"
private const val CONTENT_STYLE_SUPPORTED = "android.media.browse.CONTENT_STYLE_SUPPORTED"
private const val CONTENT_STYLE_LIST = 1
private const val CONTENT_STYLE_GRID = 2

private const val UAMP_USER_AGENT = "uamp.next"

val MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS = "playback_start_position_ms"

private const val TAG = "MusicService"
