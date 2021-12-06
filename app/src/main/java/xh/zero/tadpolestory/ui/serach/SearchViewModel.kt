package xh.zero.tadpolestory.ui.serach

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import xh.zero.tadpolestory.repo.Repository
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repo: Repository
) : ViewModel() {
    fun getTagList() = repo.getTagList()
    fun getCategoriesList() = repo.getCategoriesList()
    fun searchAlbums(page: Int, tags: String) = repo.searchAlbums(page = page, tags = tags)
    fun getMetadataList() = repo.getMetadataList()
    fun getMetadataAlbums(attrs: String?, calcDimen: Int, page: Int) = repo.getMetadataAlbums(attrs, calcDimen, page)

}