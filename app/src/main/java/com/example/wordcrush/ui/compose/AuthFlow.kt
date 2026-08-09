package com.example.wordcrush.ui.compose

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wordcrush.ui.viewmodel.LoginAction
import com.example.wordcrush.ui.viewmodel.LoginEffect
import com.example.wordcrush.ui.viewmodel.LoginViewModel
import com.example.wordcrush.ui.viewmodel.RegisterAction
import com.example.wordcrush.ui.viewmodel.RegisterEffect
import com.example.wordcrush.ui.viewmodel.RegisterViewModel
import kotlinx.coroutines.launch

@Composable
internal fun AuthFlow(
    snackbarHostState: SnackbarHostState,
    onRequestSessionRefresh: () -> Unit
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = AuthRoute.Login
    ) {
        composable(AuthRoute.Login) {
            LoginRoute(
                onRegister = { navController.navigate(AuthRoute.Register) },
                onLoginSuccess = onRequestSessionRefresh,
                onShowMessage = { message ->
                    scope.launch { snackbarHostState.showSnackbar(message) }
                }
            )
        }
        composable(AuthRoute.Register) {
            RegisterRoute(
                onBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    scope.launch {
                        snackbarHostState.showSnackbar("Registration complete. Please log in.")
                    }
                    navController.popBackStack()
                },
                onShowMessage = { message ->
                    scope.launch { snackbarHostState.showSnackbar(message) }
                }
            )
        }
    }
}

@Composable
private fun LoginRoute(
    onRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val dims = appDimens()
    val viewModel: LoginViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                LoginEffect.LoginSucceeded -> onLoginSuccess()
                is LoginEffect.ShowMessage -> onShowMessage(effect.message)
            }
        }
    }
    LaunchedEffect(Unit) {
        viewModel.onAction(LoginAction.Initialize)
    }

    AuthScreen(
        title = "Word Crush",
        subtitle = "Sign in to continue your vocabulary training."
    ) {
        OutlinedTextField(
            value = uiState.username,
            onValueChange = { viewModel.onAction(LoginAction.UsernameChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Username") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )
        Spacer(modifier = Modifier.height(dims.controlSpacing))
        OutlinedTextField(
            value = uiState.password,
            onValueChange = { viewModel.onAction(LoginAction.PasswordChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )
        uiState.error?.let { message ->
            Spacer(modifier = Modifier.height(dims.controlSpacing))
            ErrorText(message)
        }
        Spacer(modifier = Modifier.height(dims.cardPaddingLarge))
        Button(
            onClick = { viewModel.onAction(LoginAction.Submit) },
            modifier = Modifier
                .fillMaxWidth()
                .height(dims.inputHeight),
            enabled = !uiState.isLoading
        ) {
            Text("Log in")
        }
        Spacer(modifier = Modifier.height(dims.compactSpacing))
        TextButton(
            onClick = onRegister,
            modifier = Modifier.align(Alignment.End),
            enabled = !uiState.isLoading
        ) {
            Text("Create account")
        }
    }
}

@Composable
private fun RegisterRoute(
    onBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val dims = appDimens()
    val viewModel: RegisterViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is RegisterEffect.RegistrationSucceeded -> onRegisterSuccess()
                is RegisterEffect.ShowMessage -> onShowMessage(effect.message)
            }
        }
    }

    AuthScreen(
        title = "Create account",
        subtitle = "A single activity host now drives the full app flow."
    ) {
        OutlinedTextField(
            value = uiState.username,
            onValueChange = { viewModel.onAction(RegisterAction.UsernameChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Username") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(dims.controlSpacing))
        OutlinedTextField(
            value = uiState.password,
            onValueChange = { viewModel.onAction(RegisterAction.PasswordChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(dims.controlSpacing))
        OutlinedTextField(
            value = uiState.confirmPassword,
            onValueChange = { viewModel.onAction(RegisterAction.ConfirmPasswordChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Confirm password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )
        uiState.error?.let { message ->
            Spacer(modifier = Modifier.height(dims.controlSpacing))
            ErrorText(message)
        }
        Spacer(modifier = Modifier.height(dims.cardPaddingLarge))
        Button(
            onClick = { viewModel.onAction(RegisterAction.Submit) },
            modifier = Modifier
                .fillMaxWidth()
                .height(dims.inputHeight),
            enabled = !uiState.isLoading
        ) {
            Text("Register")
        }
        Spacer(modifier = Modifier.height(dims.compactSpacing))
        TextButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.End),
            enabled = !uiState.isLoading
        ) {
            Text("Back")
        }
    }
}
