package xh.zero.tadpolestory.ui.album

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import xh.zero.tadpolestory.repo.Repository
import xh.zero.tadpolestory.repo.paging.RecommendAlbumRepository
import javax.inject.Inject

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    val repo: Repository
) : ViewModel() {

    fun subscribeAlbum(id: Int) = repo.subscribeAlbum(id)

    fun unsubscribe(id: Int) = repo.unsubscribe(id)
    fun isSubscribe(id: Int) = repo.isSubscribe(id)

}