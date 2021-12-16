package xh.zero.tadpolestory.ui.more

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import xh.zero.core.paging.PagingViewModel
import xh.zero.tadpolestory.repo.Repository
import xh.zero.tadpolestory.repo.data.Album
import xh.zero.tadpolestory.repo.paging.SubscribeAlbumRepository
import javax.inject.Inject

@HiltViewModel
class MoreViewModel @Inject constructor(
    val repo: Repository,
    private val subscribeRepo: SubscribeAlbumRepository
) : PagingViewModel<List<Album>, Album>(subscribeRepo) {
    fun getSubscribeAlbumsIds() = repo.getSubscribeAlbumsIds()
    fun getRecentAlbumsIds() = repo.getRecentAlbumsIds()
    fun getAlbumsForIds(ids: String) = repo.getAlbumsForIds(ids)
}