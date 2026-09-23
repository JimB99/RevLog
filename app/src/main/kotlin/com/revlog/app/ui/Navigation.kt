package com.revlog.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.revlog.app.ui.home.VehiclesHomeScreen
import com.revlog.app.ui.settings.ExportScreen
import com.revlog.app.ui.settings.ImportReviewScreen
import com.revlog.app.ui.settings.LanguageSettingsScreen
import com.revlog.app.ui.settings.SettingsHubScreen
import com.revlog.app.ui.vehicle.ServiceEntryScreen
import com.revlog.app.ui.vehicle.ServiceLogsScreen
import com.revlog.app.ui.vehicle.VehicleDataEditScreen
import com.revlog.app.ui.vehicle.VehicleDetailScreen
import com.revlog.domain.model.ServiceType

object Routes {
    const val HOME = "home"
    const val VEHICLE_DETAIL = "vehicle/{vehicleId}"
    const val VEHICLE_DATA_EDIT = "vehicle/{vehicleId}/data/edit"
    const val SERVICE_ADD = "vehicle/{vehicleId}/service/{serviceType}/add"
    const val SERVICE_LOGS = "vehicle/{vehicleId}/service/{serviceType}/logs"
    const val SETTINGS = "settings"
    const val SETTINGS_LANGUAGE = "settings/language"
    const val SETTINGS_EXPORT = "settings/export?ids={ids}"
    const val IMPORT_REVIEW = "settings/import/review"

    fun vehicleDetail(id: Long, serviceTab: Boolean = false) =
        if (serviceTab) "vehicle/$id?serviceTab=true" else "vehicle/$id"
    fun vehicleDataEdit(id: Long) = "vehicle/$id/data/edit"
    fun serviceAdd(id: Long, type: ServiceType) = "vehicle/$id/service/${type.name}/add"
    fun serviceLogs(id: Long, type: ServiceType) = "vehicle/$id/service/${type.name}/logs"
    fun exportPreselected(ids: List<Long>) = "settings/export?ids=${ids.joinToString(",")}"
}

@Composable
fun RevLogNavHost(
    importRequestId: Int = 0,
    startVehicleId: Long? = null,
    startOnServiceTab: Boolean = false,
) {
    val navController = rememberNavController()

    androidx.compose.runtime.LaunchedEffect(importRequestId) {
        if (importRequestId > 0) {
            navController.navigate(Routes.IMPORT_REVIEW) {
                launchSingleTop = true
            }
        }
    }

    androidx.compose.runtime.LaunchedEffect(startVehicleId, startOnServiceTab) {
        startVehicleId?.let { id ->
            navController.navigate(Routes.vehicleDetail(id, serviceTab = startOnServiceTab))
        }
    }

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            VehiclesHomeScreen(
                onNavigateSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateVehicle = { navController.navigate(Routes.vehicleDetail(it)) },
                onExportVehicle = { navController.navigate(Routes.exportPreselected(listOf(it))) },
            )
        }
        composable(
            route = "${Routes.VEHICLE_DETAIL}?serviceTab={serviceTab}",
            arguments = listOf(
                navArgument("vehicleId") { type = NavType.LongType },
                navArgument("serviceTab") { type = NavType.BoolType; defaultValue = false },
            ),
        ) {
            val vehicleId = it.arguments?.getLong("vehicleId") ?: 0L
            val serviceTab = it.arguments?.getBoolean("serviceTab") ?: false
            VehicleDetailScreen(
                vehicleId = vehicleId,
                initialTab = if (serviceTab) 1 else 0,
                onBack = { navController.popBackStack() },
                onEditData = { navController.navigate(Routes.vehicleDataEdit(vehicleId)) },
                onAddService = { type ->
                    navController.navigate(Routes.serviceAdd(vehicleId, type))
                },
                onViewLogs = { type ->
                    navController.navigate(Routes.serviceLogs(vehicleId, type))
                },
            )
        }
        composable(
            route = Routes.VEHICLE_DATA_EDIT,
            arguments = listOf(navArgument("vehicleId") { type = NavType.LongType }),
        ) {
            VehicleDataEditScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.SERVICE_ADD,
            arguments = listOf(
                navArgument("vehicleId") { type = NavType.LongType },
                navArgument("serviceType") { type = NavType.StringType },
            ),
        ) {
            ServiceEntryScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.SERVICE_LOGS,
            arguments = listOf(
                navArgument("vehicleId") { type = NavType.LongType },
                navArgument("serviceType") { type = NavType.StringType },
            ),
        ) {
            ServiceLogsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsHubScreen(
                onBack = { navController.popBackStack() },
                onNavigateLanguage = { navController.navigate(Routes.SETTINGS_LANGUAGE) },
                onNavigateExport = { navController.navigate("settings/export?ids=") },
                onNavigateImportReview = { navController.navigate(Routes.IMPORT_REVIEW) },
            )
        }
        composable(Routes.SETTINGS_LANGUAGE) {
            LanguageSettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.SETTINGS_EXPORT,
            arguments = listOf(navArgument("ids") { type = NavType.StringType; defaultValue = "" }),
        ) { entry ->
            val ids = entry.arguments?.getString("ids")
                ?.split(",")
                ?.filter { it.isNotBlank() }
                ?.mapNotNull { it.toLongOrNull() }
                ?: emptyList()
            ExportScreen(
                onBack = { navController.popBackStack() },
                preselectedIds = ids,
            )
        }
        composable(Routes.IMPORT_REVIEW) {
            ImportReviewScreen(
                onBack = { navController.popBackStack() },
                onDone = {
                    navController.popBackStack(Routes.HOME, false)
                },
            )
        }
    }
}
