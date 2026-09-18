package com.revlog.domain.model

enum class ReminderAnchor {
    LAST_SERVICE_DATE,
}

data class ReminderRule(
    val id: Long,
    val vehicleId: Long,
    val serviceType: ServiceType,
    val intervalMonths: Int,
    val leadDays: Int = 14,
    val enabled: Boolean = true,
    val anchor: ReminderAnchor = ReminderAnchor.LAST_SERVICE_DATE,
    val lastNotifiedDueDate: java.time.LocalDate? = null,
)
