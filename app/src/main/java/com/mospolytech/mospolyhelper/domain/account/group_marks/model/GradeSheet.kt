package com.mospolytech.mospolyhelper.domain.account.group_marks.model

data class GradeSheet(
    val id: String,
    val documentType: String,
    val examType: String,
    val department: String,
    val school: String,
    val examDate: String,
    val examTime: String,
    val closeDate: String,
    val year: String,
    val course: String,
    val semester: String,
    val group: String,
    val disciplineName: String,
    val direction: String,
    val directionCode: String,
    val specialization: String,
    val teachers: List<Teacher>,
    val students: List<Student>,
    val fixed: Boolean,
    val modifiedDate: String
)
