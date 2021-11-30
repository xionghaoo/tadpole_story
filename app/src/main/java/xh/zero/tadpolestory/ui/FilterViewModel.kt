package xh.zero.tadpolestory.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import xh.zero.tadpolestory.repo.Repository
import javax.inject.Inject

@HiltViewModel
class FilterViewModel @Inject constructor(
    private val repo: Repository
) : ViewModel() {
    fun getTagList() = repo.getTagList()
    fun getCategoriesList() = repo.getCategoriesList()
    fun searchAlbums(page: Int, tags: String) = repo.searchAlbums(page = page, tags = tags)

}