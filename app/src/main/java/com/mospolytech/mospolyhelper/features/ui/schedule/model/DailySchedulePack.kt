package com.mospolytech.mospolyhelper.features.ui.schedule.model

import com.mospolytech.mospolyhelper.domain.schedule.model.*
import com.mospolytech.mospolyhelper.domain.schedule.model.tag.LessonTag
import com.mospolytech.mospolyhelper.domain.schedule.utils.ScheduleUtils
import java.time.DayOfWeek
import java.time.LocalDate

class DailySchedulePack(
    val lessons: List<ScheduleItemPacked>
) {
    class Builder {
        private var showEmptyLessons = false
        private var showLessonWindows = false

        fun withEmptyLessons(showEmptyLessons: Boolean): Builder {
            this.showEmptyLessons = showEmptyLessons
            return this
        }

        fun withLessonWindows(showLessonWindows: Boolean): Builder {
            this.showLessonWindows = showLessonWindows
            return this
        }

        fun build(
            schedule: Schedule,
            date: LocalDate,
            dateFilter: LessonDateFilter,
            featuresSettings: LessonFeaturesSettings,
            lessonTagProvider: (lesson: Lesson, dayOfWeek: DayOfWeek, order: Int) -> List<LessonTag>
        ): DailySchedulePack {
            var rawDailySchedule = schedule.getLessons(
                date,
                dateFilter
            )
            if (showEmptyLessons) rawDailySchedule = ScheduleUtils.getEmptyPairsDecorator(rawDailySchedule)
            val dailySchedule: List<ScheduleItem> = if (showLessonWindows) {
                ScheduleUtils.getWindowsDecorator(rawDailySchedule)
            } else {
                rawDailySchedule
            }
            return DailySchedulePack(dailySchedule.flatMap { scheduleItem ->
                return@flatMap when (scheduleItem) {
                    is LessonPlace -> listOf<ScheduleItemPacked>(LessonPlacePack(scheduleItem)) +
                            scheduleItem.lessons.map {
                                LessonPack(
                                    it,
                                    LessonTime.fromLessonPlace(scheduleItem),
                                    lessonTagProvider(it, date.dayOfWeek, scheduleItem.order),
                                    emptyList(),
                                    dateFilter,
                                    featuresSettings
                                )
                            }
                    is LessonWindow -> listOf<ScheduleItemPacked>(LessonWindowPack(scheduleItem))
                    else -> emptyList()
                }
            })
        }
    }
}