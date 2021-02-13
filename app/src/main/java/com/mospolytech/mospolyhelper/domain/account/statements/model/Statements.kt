package com.mospolytech.mospolyhelper.domain.account.statements.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

@Serializable
data class Statements(
    val semester: String,
    val semesterList: List<String>,
    val sheets: List<Statement>
)