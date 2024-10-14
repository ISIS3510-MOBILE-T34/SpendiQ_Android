package com.isis3510.spendiq

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import com.isis3510.spendiq.views.theme.SpendiQTheme
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.isis3510.spendiq.views.landing.LandingScreen
import com.isis3510.spendiq.views.login.LogInScreen
import com.isis3510.spendiq.views.signup.SignUpScreen
import com.isis3510.spendiq.views.start.StartScreen

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SpendApp()
{
    SpendiQTheme {
        Surface(color = MaterialTheme.colorScheme.background){
            val appState = rememberAppState()

            Scaffold { innerPaddingModifier ->
                NavHost(
                    navController = appState.navController,
                    startDestination = START_SCREEN,
                    modifier = Modifier.padding(innerPaddingModifier)
                ){
                    spendGraph(appState)
                }
            }
        }
    }
}


@Composable
fun rememberAppState(navController: NavHostController = rememberNavController()) =
    remember(navController) {
        SpendAppState(navController)
    }

fun NavGraphBuilder.spendGraph(appState: SpendAppState){
    composable(LANDING_SCREEN){
        LandingScreen()
    }

    composable(LOG_IN_SCREEN){
        LogInScreen(openAndPopUp = {route, popUp->appState.navigateAndPopUp(route, popUp)})
    }

    composable(SIGN_UP_SCREEN){
        SignUpScreen(openAndPopUp = {route, popUp->appState.navigateAndPopUp(route, popUp)})
    }

    composable(START_SCREEN){
        StartScreen(openAndPopUp = {route, popUp->appState.navigateAndPopUp(route, popUp)})
    }
}
