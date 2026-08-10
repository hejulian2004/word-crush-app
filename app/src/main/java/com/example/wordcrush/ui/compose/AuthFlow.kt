package com.example.wordcrush.ui.compose

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wordcrush.constants.AppStrings
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
                        snackbarHostState.showSnackbar(AppStrings.Auth.REGISTRATION_COMPLETE)
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
    val viewModel: LoginViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var passwordVisible by remember { mutableStateOf(false) }

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
        title = AppStrings.Auth.APP_NAME,
        subtitle = AppStrings.Auth.LOGIN_SUBTITLE
    ) {
        AuthTextField(
            value = uiState.username,
            onValueChange = { viewModel.onAction(LoginAction.UsernameChanged(it)) },
            label = AppStrings.Auth.USERNAME,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )
        PasswordField(
            value = uiState.password,
            onValueChange = { viewModel.onAction(LoginAction.PasswordChanged(it)) },
            label = AppStrings.Auth.PASSWORD,
            visible = passwordVisible,
            onToggleVisibility = { passwordVisible = !passwordVisible },
            keyboardActions = KeyboardActions(
                onDone = { viewModel.onAction(LoginAction.Submit) }
            )
        )
        uiState.error?.let { ErrorText(it) }
        AuthSubmitButton(
            label = AppStrings.Auth.LOGIN,
            loading = uiState.isLoading,
            onClick = { viewModel.onAction(LoginAction.Submit) }
        )
        TextButton(
            onClick = onRegister,
            modifier = Modifier.align(Alignment.End),
            enabled = !uiState.isLoading
        ) {
            Text(AppStrings.Auth.CREATE_ACCOUNT)
        }
    }
}

@Composable
private fun RegisterRoute(
    onBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val viewModel: RegisterViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is RegisterEffect.RegistrationSucceeded -> onRegisterSuccess()
                is RegisterEffect.ShowMessage -> onShowMessage(effect.message)
            }
        }
    }

    AuthScreen(
        title = AppStrings.Auth.CREATE_ACCOUNT,
        subtitle = AppStrings.Auth.REGISTER_SUBTITLE
    ) {
        AuthTextField(
            value = uiState.username,
            onValueChange = { viewModel.onAction(RegisterAction.UsernameChanged(it)) },
            label = AppStrings.Auth.USERNAME,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        PasswordField(
            value = uiState.password,
            onValueChange = { viewModel.onAction(RegisterAction.PasswordChanged(it)) },
            label = AppStrings.Auth.PASSWORD,
            visible = passwordVisible,
            onToggleVisibility = { passwordVisible = !passwordVisible },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        PasswordField(
            value = uiState.confirmPassword,
            onValueChange = { viewModel.onAction(RegisterAction.ConfirmPasswordChanged(it)) },
            label = AppStrings.Auth.CONFIRM_PASSWORD,
            visible = passwordVisible,
            onToggleVisibility = { passwordVisible = !passwordVisible },
            keyboardActions = KeyboardActions(
                onDone = { viewModel.onAction(RegisterAction.Submit) }
            )
        )
        uiState.error?.let { ErrorText(it) }
        AuthSubmitButton(
            label = AppStrings.Auth.REGISTER,
            loading = uiState.isLoading,
            onClick = { viewModel.onAction(RegisterAction.Submit) }
        )
        TextButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.End),
            enabled = !uiState.isLoading
        ) {
            Text(AppStrings.Common.BACK)
        }
    }
}

@Composable
private fun ColumnScope.AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardOptions: KeyboardOptions
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = keyboardOptions
    )
}

@Composable
private fun ColumnScope.PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visible: Boolean,
    onToggleVisibility: () -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Password,
        imeAction = ImeAction.Done
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = keyboardOptions.copy(keyboardType = KeyboardType.Password),
        keyboardActions = keyboardActions,
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    imageVector = if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = if (visible) {
                        AppStrings.Accessibility.HIDE_PASSWORD
                    } else {
                        AppStrings.Accessibility.SHOW_PASSWORD
                    }
                )
            }
        }
    )
}

@Composable
private fun AuthSubmitButton(
    label: String,
    loading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
        enabled = !loading
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(label)
        }
    }
}
