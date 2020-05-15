package com.mospolytech.mospolyhelper.model

import android.util.Log
import com.mospolytech.mospolyhelper.TAG
import java.time.DayOfWeek
import java.util.*


data class Schedule(
    val dailySchedules: Array<Daily>,
    val lastUpdate: Calendar,
    val group: Group,
    val isSession: Boolean,
    val dateFrom: Calendar,
    val dateTo: Calendar
) {
    class Builder(
        private var dailySchedules: Array<Daily>,
        private var lastUpdate: Calendar? = null,
        private var group: Group = Group.empty,
        private var isSession: Boolean,
        private var dateFrom: Calendar? = null,
        private var dateTo: Calendar? = null
    ) {
        fun dailySchedules(dailySchedules: Array<Daily>) = apply { this.dailySchedules = dailySchedules }

        fun lastUpdate(lastUpdate: Calendar) = apply { this.lastUpdate = lastUpdate }

        fun group(group: Group) = apply { this.group = group }

        fun isSession(isSession: Boolean) = apply { this.isSession = isSession }

        fun dateFrom(dateFrom: Calendar) = apply { this.dateFrom = dateFrom }

        fun dateTo(dateTo: Calendar) = apply { this.dateTo = dateTo }

        fun build(): Schedule {
            var dateFrom = this.dateFrom ?: Calendar.getInstance().apply { time = Date(Long.MIN_VALUE) }
            var dateTo = this.dateTo ?: Calendar.getInstance().apply { time = Date(Long.MAX_VALUE) }
            if (this.dateFrom == null || this.dateTo == null) {
                for (dailySchedule in dailySchedules) {
                    for (lesson in dailySchedule) {
                        if (lesson.dateFrom < dateFrom)
                            dateFrom = lesson.dateFrom;
                        if (lesson.dateTo > dateTo)
                            dateTo = lesson.dateTo;
                    }
                }
            }
            val lastUpdate = lastUpdate ?: Calendar.getInstance()

            return  Schedule(
                dailySchedules,
                lastUpdate,
                group,
                isSession,
                dateFrom,
                dateTo
            )
        }
    }


    fun getSchedule(date: Calendar, filter: Filter) =
        filter.getFilteredSchedule(dailySchedules[date.get(Calendar.DAY_OF_WEEK)], date)

    data class Filter(val sessionFilter: Boolean, val dateFilter: DateFilter) {
        companion object {
            val default = Filter(true, DateFilter.Hide)
            val none = Filter(false, DateFilter.Show)
        }
        enum class DateFilter{
            Show,
            Desaturate,
            Hide
        }

        fun getFilteredSchedule(dailySchedule: Daily, date: Calendar) =
            Daily(dailySchedule.lessons.filter {
                ((dateFilter != DateFilter.Hide ||
                        ((it.dateFrom <= date || it.isImportant) && date <= it.dateTo)) &&
                        (!sessionFilter ||
                        !it.isImportant || (it.dateFrom <= date && date <= it.dateTo)))
            }.toTypedArray(), dailySchedule.dayOfWeek);
    }

    data class Daily(val lessons: Array<Lesson>, val dayOfWeek: DayOfWeek){
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Daily

            if (!lessons.contentEquals(other.lessons)) return false
            if (dayOfWeek != other.dayOfWeek) return false

            return true
        }

        override fun hashCode(): Int {
            var result = lessons.contentHashCode()
            result = 31 * result + dayOfWeek.hashCode()
            return result
        }

        operator fun iterator() = lessons.iterator()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Schedule

        if (!dailySchedules.contentEquals(other.dailySchedules)) return false
        if (lastUpdate != other.lastUpdate) return false
        if (group != other.group) return false
        if (isSession != other.isSession) return false
        if (dateFrom != other.dateFrom) return false
        if (dateTo != other.dateTo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = dailySchedules.contentHashCode()
        result = 31 * result + lastUpdate.hashCode()
        result = 31 * result + group.hashCode()
        result = 31 * result + isSession.hashCode()
        result = 31 * result + dateFrom.hashCode()
        result = 31 * result + dateTo.hashCode()
        return result
    }
}

data class Group(
    val title: String,
    val dateFrom: Calendar,
    val dateTo: Calendar,
    val isEvening: Boolean,
    val comment: String,
    val course: Int
    ) {
    companion object {
        val empty by lazy {
            Group(
                "",
                Calendar.getInstance().apply { time = Date(Long.MIN_VALUE) },
                Calendar.getInstance().apply { time = Date(Long.MAX_VALUE) },
                false,
                "",
                0
            )
        }
    }
}

data class Lesson(
    val order: Int,
    val title: String,
    val teachers: Array<Teacher>,
    val dateFrom: Calendar,
    val dateTo: Calendar,
    val auditoriums: Array<Auditorium>,
    val type: String,
    val group: Group
) : Comparable<Lesson> {
    companion object {
        const val COURSE_PROJECT = "кп"
        const val EXAM = "экзамен"
        const val CREDIT = "зачет"
        const val CREDIT_WITH_MARK = "зсо"
        const val EXAMINATION_SHOW = "эп"
        const val CONSULTATION = "консультация"
        const val LABORATORY = "лаб"
        const val PRACTICE = "практика"
        const val LECTURE = "лекция"
        const val OTHER = "другое"

        fun getEmpty(order: Int) =
            Lesson(
                order,
                "",
                arrayOf(),
                Calendar.getInstance().apply { time = Date(Long.MIN_VALUE) },
                Calendar.getInstance().apply { time = Date(Long.MAX_VALUE) },
                arrayOf(),
                "",
                Group.empty
            )
    }

    val isEmpty = title.isEmpty() && type.isEmpty()

    val isImportant =
        type.contains(EXAM, true) ||
                type.contains(CREDIT, true) ||
                type.contains(COURSE_PROJECT, true) ||
                type.contains(CREDIT_WITH_MARK, true) ||
                type.contains(EXAMINATION_SHOW, true)

    val time = when (order) {
        0 -> Pair("09:00", "10:30")
        1 -> Pair("10:40", "12:10")
        2 -> Pair("12:20", "13:50")
        3 -> Pair("14:30", "16:00")
        4 -> Pair("16:10", "17:40")
        5 -> if (group.isEvening) Pair("18:20", "19:40") else Pair("17:50", "19:20")
        6 -> if (group.isEvening) Pair("19:50", "21:10") else Pair("19:30", "21:00")
        else -> {
            Log.e(TAG, "Wrong order number of lesson")
            Pair("Ошибка", "номера занятия")
        }
    }

    fun equalsTime(lesson: Lesson) =
        order == lesson.order && group.isEvening == lesson.group.isEvening

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Lesson

        if (order != other.order) return false
        if (title != other.title) return false
        if (!teachers.contentEquals(other.teachers)) return false
        if (dateFrom != other.dateFrom) return false
        if (dateTo != other.dateTo) return false
        if (!auditoriums.contentEquals(other.auditoriums)) return false
        if (type != other.type) return false
        if (group != other.group) return false

        return true
    }

    override fun hashCode(): Int {
        var result = order
        result = 31 * result + title.hashCode()
        result = 31 * result + teachers.contentHashCode()
        result = 31 * result + dateFrom.hashCode()
        result = 31 * result + dateTo.hashCode()
        result = 31 * result + auditoriums.contentHashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + group.hashCode()
        return result
    }

    override fun compareTo(other: Lesson) = when {
        order != other.order -> order.compareTo(other.order)
        group.isEvening == other.group.isEvening -> group.title.compareTo(other.group.title)
        group.isEvening -> 1
        else -> -1
    }
}

data class Teacher(val names: Array<String>) {
    companion object {
        fun fromFullName(name: String) =
            Teacher(
                name.replace(" - ", "-")
                    .replace(" -", "-")
                    .replace("- ", "-")
                    .split(" ")
                    .filter { it.isNotEmpty() }
                    .toTypedArray()
            )
        }

    fun getFullName() = names.joinToString(" ")

    fun getShortName(): String {
        if (names.isEmpty())
            return ""

        val isVacancy = names.any { it.contains("вакансия", true) }

        return if (isVacancy || (names.first().length > 1) && (names.first().let { it[0].isLowerCase() == it[1].isLowerCase() })) {
            names.joinToString("\u00A0")
        } else {
            val shortName = StringBuilder(names.first())
            for (i in 1 until names.size) {
                shortName.append("\u00A0")
                    .append(names[i][0])
                    .append('.')
            }
            shortName.toString()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Teacher

        if (!names.contentEquals(other.names)) return false

        return true
    }

    override fun hashCode(): Int {
        return names.contentHashCode()
    }
}

data class Auditorium(val title: String, val color: String)