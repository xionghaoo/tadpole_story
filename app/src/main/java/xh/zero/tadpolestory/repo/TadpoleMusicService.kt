package xh.zero.tadpolestory.repo

import com.example.android.uamp.media.MusicService
import com.example.android.uamp.media.library.MusicSource
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TadpoleMusicService : MusicService() {

    @Inject
    lateinit var repo: Repository

    override fun createMusicSource(): MusicSource {
        return TadpoleMusicSource(repo)
    }
}