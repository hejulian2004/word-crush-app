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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.example.wordcrush.ui.viewmodel.LoginResult
import com.example.wordcrush.ui.viewmodel.LoginViewModel
import com.example.wordcrush.ui.viewmodel.RegisterResult
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
    val loginResult by viewModel.loginResult.collectAsStateWithLifecycle()
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.checkLocalLoginState()
    }

    LaunchedEffect(loginResult) {
        when (val result = loginResult) {
            is LoginResult.Success -> onLoginSuccess()
            LoginResult.AlreadyLoggedIn -> onLoginSuccess()
            is LoginResult.Error -> onShowMessage(result.message)
            null -> Unit
        }
        if (loginResult != null) {
            viewModel.resetLoginResult()
        }
    }

    AuthScreen(
        title = "Word Crush",
        subtitle = "Sign in to continue your vocabulary training."
    ) {
        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                viewModel.clearError()
            },
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
            value = password,
            onValueChange = {
                password = it
                viewModel.clearError()
            },
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
            onClick = { viewModel.login(username.trim(), password.trim()) },
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
    val registerResult by viewModel.registerResult.collectAsStateWithLifecycle()
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(registerResult) {
        when (val result = registerResult) {
            is RegisterResult.Success -> onRegisterSuccess()
            is RegisterResult.Error -> onShowMessage(result.message)
            null -> Unit
        }
        if (registerResult != null) {
            viewModel.resetRegisterResult()
        }
    }

    AuthScreen(
        title = "Create account",
        subtitle = "A single activity host now drives the full app flow."
    ) {
        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                viewModel.clearError()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Username") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(dims.controlSpacing))
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                viewModel.clearError()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(dims.controlSpacing))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                viewModel.clearError()
            },
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
            onClick = { viewModel.register(username.trim(), password.trim(), confirmPassword.trim()) },
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
