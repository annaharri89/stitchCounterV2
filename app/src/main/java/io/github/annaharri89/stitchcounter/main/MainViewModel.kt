package io.github.annaharri89.stitchcounter.main

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.github.annaharri89.stitchcounter.theme.AppColors

class MainViewModel : ViewModel() {

    val appDarkColors: LiveData<MutableState<AppColors?>>
        get() = appDarkColorsItem

    private val appDarkColorsItem = MutableLiveData<MutableState<AppColors?>>()
    private val appDarkColorsItemImpl = mutableStateOf<AppColors?>(null)

    fun addDarkColors(colors: AppColors?){
        appDarkColorsItemImpl.value = colors
        appDarkColorsItem.postValue(appDarkColorsItemImpl)
    }

    val appLightColors: LiveData<MutableState<AppColors?>>
        get() = appLightColorsItem

    private val appLightColorsItem = MutableLiveData<MutableState<AppColors?>>()
    private val appLightColorsItemImpl = mutableStateOf<AppColors?>(null)

    fun addLightColors(colors: AppColors?){
        appLightColorsItemImpl.value = colors
        appLightColorsItem.postValue(appLightColorsItemImpl)
    }
}