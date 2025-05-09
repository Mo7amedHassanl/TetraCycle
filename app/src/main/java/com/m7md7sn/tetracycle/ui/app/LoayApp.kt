package com.m7md7sn.tetracycle.ui.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.m7md7sn.tetracycle.data.model.TimedSensorReading
import com.m7md7sn.tetracycle.ui.navigation.TopBar
import com.m7md7sn.tetracycle.ui.screen.home.MainScreen
import com.m7md7sn.tetracycle.ui.screen.monitoring.MonitoringScreen
import com.m7md7sn.tetracycle.ui.screen.sensor.SensorScreen
import com.m7md7sn.tetracycle.ui.screen.home.HomeViewModel
import com.m7md7sn.tetracycle.ui.screen.monitoring.MonitoringViewModel
import com.m7md7sn.tetracycle.ui.screen.splash.SplashScreen
import com.m7md7sn.tetracycle.ui.navigation.AnimatedBottomBar
import androidx.compose.runtime.collectAsState
import com.m7md7sn.tetracycle.ui.screen.sensor.SensorViewModel

data class SensorScreenData(
    val unit: String,
    val min: Float,
    val max: Float,
    val normalRange: ClosedFloatingPointRange<Float>,
    val readings: List<TimedSensorReading>
)

@Composable
fun LoayApp(
    modifier: Modifier = Modifier
) {
    val navController: NavHostController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Map routes to tab indices
    val tabRoutes = listOf(Screen.Home.route, Screen.Monitoring.route)
    // Use a derived value so it updates with navigation
    var selectedIndex = tabRoutes.indexOf(currentRoute)

    // Determine if the bottom bar should be visible
    val isBarVisible = remember(currentRoute) {
        currentRoute in tabRoutes ||
        (currentRoute?.startsWith("monitoring") == true) ||
        currentRoute == Screen.Sensor.route
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            AnimatedVisibility(
                visible = isBarVisible,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300))
            ) {
                if (currentRoute != Screen.Sensor.route) {
                    AnimatedBottomBar(
                        selectedIndex = selectedIndex,
                        onTabSelected = { selectedIndex = it },
                        navController = navController
                    )
                }
            }
        },
        topBar = {
            AnimatedVisibility(
                visible = isBarVisible,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                TopBar(
                    isBackButtonVisible = currentRoute != Screen.Home.route,
                    onBackButtonClick = {
                        navController.popBackStack()
                    },
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(onNavigate = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Home.route) {
                val homeViewModel: HomeViewModel = hiltViewModel()
                MainScreen(
                    viewModel = homeViewModel,
                    onSensorCardClick = { index ->
                        navController.navigate("monitoring?tab=$index")
                    },
                    onSystemPartClick = { index ->
                        if (index == 1) navController.navigate(Screen.Monitoring.route)
                    }
                )
            }
            composable(
                route = "monitoring?tab={tab}",
                arguments = listOf(navArgument("tab") { type = NavType.IntType; defaultValue = 0 })
            ) { backStackEntry ->
                val selectedTab = backStackEntry.arguments?.getInt("tab") ?: 0
                val monitoringViewModel: MonitoringViewModel = hiltViewModel()
                val sensorStatuses by monitoringViewModel.sensorStatuses.collectAsState()
                val isLoading by monitoringViewModel.isLoading.collectAsState()
                val error by monitoringViewModel.error.collectAsState()
                MonitoringScreen(
                    sensorStatuses = sensorStatuses,
                    isLoading = isLoading,
                    error = error,
                    initialTab = selectedTab,
                    onSensorCardClick = { index ->
                        val name = sensorStatuses.getOrNull(index)?.name ?: ""
                        navController.navigate(Screen.Sensor.createRoute(name))
                    }
                )
            }
            composable(
                route = Screen.Sensor.route,
                arguments = listOf(navArgument("sensorName") { type = NavType.StringType })
            ) { backStackEntry ->
                val sensorName = backStackEntry.arguments?.getString("sensorName") ?: ""
                val sensorViewModel: SensorViewModel = hiltViewModel()
                val readings by sensorViewModel.readings.collectAsState()
                val isLoading by sensorViewModel.isLoading.collectAsState()
                val error by sensorViewModel.error.collectAsState()
                // Use last reading for current value, or 0f if empty
                val currentValue = (readings as? List<*>)?.lastOrNull()?.let { (it as? TimedSensorReading)?.value } ?: 0f
                // Determine unit, min, max, normalRange based on sensorName
                val sensorParams = when (sensorName.lowercase()) {
                    "ph" -> Triple("", 0f, 14f) to (6.5f..8.5f)
                    "tds" -> Triple("ppm", 0f, 3000f) to (0f..500f)
                    "turbidity" -> Triple("NTU", 0f, 1000f) to (0f..50f)
                    else -> Triple("", 0f, 100f) to (0f..100f)
                }
                val unit = sensorParams.first.first
                val min = sensorParams.first.second
                val max = sensorParams.first.third
                val normalRange = sensorParams.second
                SensorScreen(
                    sensorName = sensorName,
                    currentValue = currentValue,
                    unit = unit,
                    minValue = min,
                    maxValue = max,
                    normalRange = normalRange,
                    viewModel = sensorViewModel,
                    modifier = Modifier
                )
            }
        }
    }
}

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Monitoring : Screen("monitoring")
    object Control : Screen("control")
    object Sensor : Screen("sensor/{sensorName}") {
        fun createRoute(sensorName: String) = "sensor/$sensorName"
    }
}