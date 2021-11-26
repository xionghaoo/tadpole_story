package xh.zero.tadpolestory.repo

import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import com.example.android.uamp.media.extensions.*
import com.example.android.uamp.media.library.*
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import xh.zero.tadpolestory.repo.data.Album
import xh.zero.tadpolestory.repo.data.Track
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.util.concurrent.TimeUnit

class TadpoleMusicSource(
    private val repo: Repository
) : AbstractMusicSource() {

    private var catalog: List<MediaMetadataCompat> = emptyList()

//    init {
//        state = STATE_INITIALIZING
//    }

    override fun iterator(): Iterator<MediaMetadataCompat> = catalog.iterator()

    override suspend fun load(mediaId: String) {
        state = STATE_INITIALIZING
        Timber.d("mediaId: $mediaId")
        // 根据albumId来查询音频
        // Uri.parse("https://storage.googleapis.com/uamp/catalog.json")
        updateCatalog(mediaId)?.let { updatedCatalog ->
            catalog = updatedCatalog
            state = STATE_INITIALIZED
        } ?: run {
            catalog = emptyList()
            state = STATE_ERROR
        }

//        repo.getVoiceList("")
    }

    /**
     * Function to connect to a remote URI and download/process the JSON file that corresponds to
     * [MediaMetadataCompat] objects.
     */
    private suspend fun updateCatalog(albumId: String): List<MediaMetadataCompat>? {
        return withContext(Dispatchers.IO) {
            val musicCat = try {
//                downloadJson(catalogUri)
                getAlbumVoices(albumId)
            } catch (ioException: IOException) {
                return@withContext null
            }

            // Get the base URI to fix up relative references later.
//            val baseUri = catalogUri.toString().removeSuffix(catalogUri.lastPathSegment ?: "")

            val mediaMetadataCompats = musicCat?.tracks?.map { song ->
                // The JSON may have paths that are relative to the source of the JSON
                // itself. We need to fix them up here to turn them into absolute paths.
//                catalogUri.scheme?.let { scheme ->
//                    if (!song.source.startsWith(scheme)) {
//                        song.source = baseUri + song.source
//                    }
//                    if (!song.image.startsWith(scheme)) {
//                        song.image = baseUri + song.image
//                    }
//                }
//                song.play_url_32 =
                val imageUri = AlbumArtContentProvider.mapUri(Uri.parse(song.cover_url_middle))

                MediaMetadataCompat.Builder()
                    .from(song)
                    .apply {
                        displayIconUri = imageUri.toString() // Used by ExoPlayer and Notification
                        albumArtUri = imageUri.toString()
                    }
                    .build()
            }?.toList()
            // Add description keys to be used by the ExoPlayer MediaSession extension when
            // announcing metadata changes.
            mediaMetadataCompats?.forEach { it.description.extras?.putAll(it.bundle) }
            mediaMetadataCompats
        }
    }

    @Throws(IOException::class)
    private fun downloadJson(catalogUri: Uri): JsonCatalog {
        val catalogConn = URL(catalogUri.toString())
        val reader = BufferedReader(InputStreamReader(catalogConn.openStream()))
        return Gson().fromJson(reader, JsonCatalog::class.java)
    }

    @Throws(IOException::class)
    private fun getAlbumVoices(albumId: String) : Album? {
        val response = repo.getVoiceListFormAlbum(albumId, page = 1).execute()
        if (response.isSuccessful) {
            return response.body()
        } else {
            return null
        }
    }
}


/**
 * Extension method for [MediaMetadataCompat.Builder] to set the fields from
 * our JSON constructed object (to make the code a bit easier to see).
 */
fun MediaMetadataCompat.Builder.from(jsonMusic: Track): MediaMetadataCompat.Builder {
    // The duration from the JSON is given in seconds, but the rest of the code works in
    // milliseconds. Here's where we convert to the proper units.
    val durationMs = TimeUnit.SECONDS.toMillis(jsonMusic.duration)

    id = jsonMusic.id.toString()
    title = jsonMusic.track_title
    artist = jsonMusic.announcer?.nickname
    album = jsonMusic.subordinated_album?.album_title
    duration = durationMs
    genre = "none"
    mediaUri = jsonMusic.play_url_32
    albumArtUri = jsonMusic.cover_url_middle
    trackNumber = 0
    trackCount = 0
    flag = MediaBrowserCompat.MediaItem.FLAG_PLAYABLE

    // To make things easier for *displaying* these, set the display properties as well.
    displayTitle = jsonMusic.track_title
    displaySubtitle = jsonMusic.announcer?.nickname
    displayDescription = jsonMusic.subordinated_album?.album_title
    displayIconUri = jsonMusic.cover_url_middle

    // Add downloadStatus to force the creation of an "extras" bundle in the resulting
    // MediaMetadataCompat object. This is needed to send accurate metadata to the
    // media session during updates.
    downloadStatus = MediaDescriptionCompat.STATUS_NOT_DOWNLOADED

    // Allow it to be used in the typical builder style.
    return this
}

/**
 * Wrapper object for our JSON in order to be processed easily by GSON.
 */
class JsonCatalog {
    var music: List<JsonMusic> = ArrayList()
}

/**
 * An individual piece of music included in our JSON catalog.
 * The format from the server is as specified:
 * ```
 *     { "music" : [
 *     { "title" : // Title of the piece of music
 *     "album" : // Album title of the piece of music
 *     "artist" : // Artist of the piece of music
 *     "genre" : // Primary genre of the music
 *     "source" : // Path to the music, which may be relative
 *     "image" : // Path to the art for the music, which may be relative
 *     "trackNumber" : // Track number
 *     "totalTrackCount" : // Track count
 *     "duration" : // Duration of the music in seconds
 *     "site" : // Source of the music, if applicable
 *     }
 *     ]}
 * ```
 *
 * `source` and `image` can be provided in either relative or
 * absolute paths. For example:
 * ``
 *     "source" : "https://www.example.com/music/ode_to_joy.mp3",
 *     "image" : "ode_to_joy.jpg"
 * ``
 *
 * The `source` specifies the full URI to download the piece of music from, but
 * `image` will be fetched relative to the path of the JSON file itself. This means
 * that if the JSON was at "https://www.example.com/json/music.json" then the image would be found
 * at "https://www.example.com/json/ode_to_joy.jpg".
 */
@Suppress("unused")
class JsonMusic {
    var id: String = ""
    var title: String = ""
    var album: String = ""
    var artist: String = ""
    var genre: String = ""
    var source: String = ""
    var image: String = ""
    var trackNumber: Long = 0
    var totalTrackCount: Long = 0
    var duration: Long = -1
    var site: String = ""
}