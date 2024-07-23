package io.github.annaharri89.stitchcounter.library

import android.database.Cursor
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LibraryViewModel : ViewModel() {

    val dbCursor: LiveData<MutableState<Cursor?>>
        get() = dbCursorItem

    private val dbCursorItem = MutableLiveData<MutableState<Cursor?>>()
    private val dbCursorItemImpl = mutableStateOf<Cursor?>(null)

    fun setDBCursor(cursor: Cursor?){
        dbCursorItemImpl.value = cursor
        dbCursorItem.postValue(dbCursorItemImpl)
    }
}