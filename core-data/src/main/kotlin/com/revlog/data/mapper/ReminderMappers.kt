package com.revlog.data.mapper

import com.revlog.data.local.entity.ReminderRuleEntity
import com.revlog.domain.DateParser
import com.revlog.domain.model.ReminderAnchor
import com.revlog.domain.model.ReminderRule
import com.revlog.domain.model.ServiceType

fun ReminderRuleEntity.toDomain(): ReminderRule = ReminderRule(
    id = id,
    vehicleId = vehicleId,
    serviceType = ServiceType.valueOf(serviceType),
    intervalMonths = intervalMonths,
    leadDays = leadDays,
    enabled = enabled,
    anchor = ReminderAnchor.valueOf(anchor),
    lastNotifiedDueDate = lastNotifiedDueDate?.let(DateParser::parseOrNull),
)

fun ReminderRule.toEntity(): ReminderRuleEntity = ReminderRuleEntity(
    id = id,
    vehicleId = vehicleId,
    serviceType = serviceType.name,
    intervalMonths = intervalMonths,
    leadDays = leadDays,
    enabled = enabled,
    anchor = anchor.name,
    lastNotifiedDueDate = lastNotifiedDueDate?.toString(),
)
