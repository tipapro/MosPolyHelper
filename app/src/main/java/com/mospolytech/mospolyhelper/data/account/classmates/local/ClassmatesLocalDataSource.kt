package com.mospolytech.mospolyhelper.data.account.classmates.local

import com.mospolytech.mospolyhelper.data.core.local.SharedPreferencesDataSource
import com.mospolytech.mospolyhelper.domain.account.classmates.model.Classmate
import com.mospolytech.mospolyhelper.utils.PreferenceKeys
import com.mospolytech.mospolyhelper.utils.Result2
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ClassmatesLocalDataSource(private val prefDataSource: SharedPreferencesDataSource) {
    fun get(classmates: String): Result2<List<Classmate>> {
        return try {
            Result2.success(Json.decodeFromString(classmates))
        } catch (e: Exception) {
            Result2.failure(e)
        }
    }

    fun set(classmates: List<Classmate>) {
        val currentInfo = Json.encodeToString(classmates)
        if (getJson() != currentInfo)
            prefDataSource.set(PreferenceKeys.Classmates, currentInfo)
    }

    fun getJson(): String {
        return prefDataSource.get(PreferenceKeys.Classmates, "")
    }
}