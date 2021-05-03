package com.mospolytech.mospolyhelper.data.account.marks.local

import com.mospolytech.mospolyhelper.data.core.local.SharedPreferencesDataSource
import com.mospolytech.mospolyhelper.domain.account.marks.model.Marks
import com.mospolytech.mospolyhelper.utils.PreferenceKeys
import com.mospolytech.mospolyhelper.utils.Result
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MarksLocalDataSource(private val prefDataSource: SharedPreferencesDataSource) {
    fun get(marks: String): Result<Marks> {
        return try {
            Result.success(Json.decodeFromString<Marks>(marks))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun set(marks: Marks) {
        val currentInfo = Json.encodeToString(marks)
        if (getJson() != currentInfo)
            prefDataSource.set(PreferenceKeys.Marks, currentInfo)
    }

    fun getJson(): String {
        return prefDataSource.get(PreferenceKeys.Marks, "")
    }
}