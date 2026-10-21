package com.example.androidsmartpredictparkinggihan.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.androidsmartpredictparkinggihan.SmartParkingApp
import com.example.androidsmartpredictparkinggihan.data.model.UserRole
import com.example.androidsmartpredictparkinggihan.ui.screens.AdminBookingsScreen
import com.example.androidsmartpredictparkinggihan.ui.screens.AdminHomeScreen
import com.example.androidsmartpredictparkinggihan.ui.screens.AdminStatisticsScreen
import com.example.androidsmartpredictparkinggihan.ui.screens.AdminUserManagementScreen
import com.example.androidsmartpredictparkinggihan.ui.screens.LoginScreen
import com.example.androidsmartpredictparkinggihan.ui.screens.OwnerHomeScreen
import com.example.androidsmartpredictparkinggihan.ui.screens.PredictionScreen
import com.example.androidsmartpredictparkinggihan.ui.screens.RegisterOwnerScreen
import com.example.androidsmartpredictparkinggihan.ui.screens.RegisterScreen
import com.example.androidsmartpredictparkinggihan.ui.screens.UserHomeScreen
import com.example.androidsmartpredictparkinggihan.ui.viewmodel.AdminDashboardViewModel
import com.example.androidsmartpredictparkinggihan.ui.viewmodel.AppViewModelFactory
import com.example.androidsmartpredictparkinggihan.ui.viewmodel.AuthViewModel
import com.example.androidsmartpredictparkinggihan.ui.viewmodel.OwnerHomeViewModel
import com.example.androidsmartpredictparkinggihan.ui.viewmodel.PredictionViewModel
import com.example.androidsmartpredictparkinggihan.ui.viewmodel.UserHomeViewModel
import com.example.androidsmartpredictparkinggihan.ui.viewmodel.AdminBookingsViewModel
import com.example.androidsmartpredictparkinggihan.ui.viewmodel.UserManagementViewModel

@Composable
fun SmartParkingNavHost(app: SmartParkingApp) {
    val navController = rememberNavController()
    val factory = AppViewModelFactory(app)
    val authViewModel: AuthViewModel = viewModel(factory = factory)

    LaunchedEffect(authViewModel.uiState.loggedInUser) {
        val user = authViewModel.uiState.loggedInUser ?: return@LaunchedEffect
        val destination = when (user.role) {
            UserRole.ADMIN -> Routes.ADMIN_HOME
            UserRole.HOUSE_OWNER -> Routes.OWNER_HOME
            UserRole.USER -> Routes.USER_HOME
        }
        navController.navigate(destination) {
            popUpTo(Routes.LOGIN) { inclusive = true }
        }
    }

    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(
                uiState = authViewModel.uiState,
                onLogin = authViewModel::login,
                onNavigateRegister = { navController.navigate(Routes.REGISTER) },
                onNavigateRegisterOwner = { navController.navigate(Routes.REGISTER_OWNER) },
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                uiState = authViewModel.uiState,
                onRegisterDriver = authViewModel::registerDriver,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.REGISTER_OWNER) {
            RegisterOwnerScreen(
                uiState = authViewModel.uiState,
                onRegister = authViewModel::registerHouseOwner,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.USER_HOME) {
            val user = authViewModel.uiState.loggedInUser ?: return@composable
            val homeFactory = AppViewModelFactory(app, user)
            val homeViewModel: UserHomeViewModel = viewModel(factory = homeFactory)
            UserHomeScreen(
                userName = user.fullName,
                uiState = homeViewModel.uiState,
                onAdvancedPredict = { navController.navigate(Routes.PREDICT) },
                onLogout = {
                    authViewModel.clearSession()
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                },
                onLocationChange = homeViewModel::updateLocationQuery,
                onMaxPriceChange = homeViewModel::updateMaxPrice,
                onHourChange = homeViewModel::updateHour,
                onDayChange = homeViewModel::updateDay,
                onHolidayToggle = homeViewModel::toggleHoliday,
                onEventToggle = homeViewModel::toggleEvent,
                onOnlyAvailableToggle = homeViewModel::toggleOnlyAvailable,
                onSearch = homeViewModel::search,
                onBook = homeViewModel::openBooking,
                onCloseBooking = homeViewModel::closeBooking,
                onBookHoursChange = homeViewModel::updateBookHours,
                onPayAndBook = homeViewModel::payAndBook,
            )
        }
        composable(Routes.OWNER_HOME) {
            val user = authViewModel.uiState.loggedInUser ?: return@composable
            val ownerFactory = AppViewModelFactory(app, user)
            val ownerViewModel: OwnerHomeViewModel = viewModel(factory = ownerFactory)
            OwnerHomeScreen(
                ownerName = user.fullName,
                uiState = ownerViewModel.uiState,
                onLogout = {
                    authViewModel.clearSession()
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                },
                onParkingIdChange = ownerViewModel::updateParkingId,
                onTitleChange = ownerViewModel::updateTitle,
                onLocationChange = ownerViewModel::updateLocation,
                onPriceChange = ownerViewModel::updatePrice,
                onSecurityChange = ownerViewModel::updateSecurity,
                onAddSpace = ownerViewModel::addSpace,
                onRemoveSpace = ownerViewModel::removeSpace,
                onMarkNotificationsRead = ownerViewModel::markNotificationsRead,
            )
        }
        composable(Routes.PREDICT) {
            val user = authViewModel.uiState.loggedInUser ?: return@composable
            val predictFactory = AppViewModelFactory(app, user)
            val predictionViewModel: PredictionViewModel = viewModel(factory = predictFactory)
            PredictionScreen(
                uiState = predictionViewModel.uiState,
                onBack = { navController.popBackStack() },
                onParkingSelected = predictionViewModel::updateParking,
                onHourChange = predictionViewModel::updateHour,
                onDayChange = predictionViewModel::updateDay,
                onHolidayToggle = predictionViewModel::toggleHoliday,
                onEventToggle = predictionViewModel::toggleEvent,
                onOccupancyChange = predictionViewModel::updateOccupancy,
                onDurationHoursChange = predictionViewModel::updateDurationHours,
                onCancellationCountChange = predictionViewModel::updateCancellationCount,
                onPredict = predictionViewModel::runPrediction,
                onApplyLowDemandPreset = predictionViewModel::applyLowDemandPreset,
                onApplyHighDemandPreset = predictionViewModel::applyHighDemandPreset,
            )
        }
        composable(Routes.ADMIN_HOME) {
            val user = authViewModel.uiState.loggedInUser ?: return@composable
            val adminViewModel: AdminDashboardViewModel = viewModel(factory = factory)
            AdminHomeScreen(
                adminName = user.fullName,
                uiState = adminViewModel.uiState,
                onOpenStatistics = { navController.navigate(Routes.ADMIN_STATISTICS) },
                onOpenUserManagement = { navController.navigate(Routes.ADMIN_USER_MANAGEMENT) },
                onOpenBookings = { navController.navigate(Routes.ADMIN_BOOKINGS) },
                onRefresh = adminViewModel::refresh,
                onLogout = {
                    authViewModel.clearSession()
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                },
            )
        }
        composable(Routes.ADMIN_STATISTICS) {
            val user = authViewModel.uiState.loggedInUser ?: return@composable
            val adminViewModel: AdminDashboardViewModel = viewModel(factory = factory)
            AdminStatisticsScreen(
                adminName = user.fullName,
                uiState = adminViewModel.uiState,
                onRefresh = adminViewModel::refresh,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.ADMIN_BOOKINGS) {
            val adminViewModel: AdminBookingsViewModel = viewModel(factory = factory)
            AdminBookingsScreen(
                uiState = adminViewModel.uiState,
                onBack = { navController.popBackStack() },
                onRefresh = adminViewModel::refresh,
            )
        }
        composable(Routes.ADMIN_USER_MANAGEMENT) {
            val user = authViewModel.uiState.loggedInUser ?: return@composable
            val adminFactory = AppViewModelFactory(app, user)
            val userMgmtViewModel: UserManagementViewModel = viewModel(factory = adminFactory)
            val state = userMgmtViewModel.uiState
            AdminUserManagementScreen(
                uiState = state,
                onBack = { navController.popBackStack() },
                onRefresh = userMgmtViewModel::refresh,
                onOpenCreate = userMgmtViewModel::openCreate,
                onOpenEdit = userMgmtViewModel::openEdit,
                onCloseEditor = userMgmtViewModel::closeEditor,
                onSave = userMgmtViewModel::saveUser,
                onDelete = userMgmtViewModel::deleteUser,
                onNameChange = userMgmtViewModel::updateFormName,
                onEmailChange = userMgmtViewModel::updateFormEmail,
                onPasswordChange = userMgmtViewModel::updateFormPassword,
                onRoleChange = userMgmtViewModel::updateFormRole,
            )
        }
    }
}
