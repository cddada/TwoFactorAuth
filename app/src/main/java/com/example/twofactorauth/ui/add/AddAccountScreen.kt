package com.example.twofactorauth.ui.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.twofactorauth.R
import com.example.twofactorauth.data.model.AccountType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountScreen(
    onNavigateBack: () -> Unit,
    onScanQr: () -> Unit,
    viewModel: AddAccountViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.add_account)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Scan QR Code button
            OutlinedButton(
                onClick = onScanQr,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("扫描二维码")
            }

            HorizontalDivider()

            // Account Type selector
            Text(
                text = "验证类型",
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(AccountType.TOTP, AccountType.HOTP).forEach { type ->
                    FilterChip(
                        selected = uiState.type == type,
                        onClick = { viewModel.updateType(type) },
                        label = { Text(type.name) }
                    )
                }
            }

            // Issuer
            OutlinedTextField(
                value = uiState.issuer,
                onValueChange = { viewModel.updateIssuer(it) },
                label = { Text(stringResource(R.string.issuer_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Account Name
            OutlinedTextField(
                value = uiState.accountName,
                onValueChange = { viewModel.updateAccountName(it) },
                label = { Text(stringResource(R.string.account_name_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Secret
            OutlinedTextField(
                value = uiState.secret,
                onValueChange = { viewModel.updateSecret(it) },
                label = { Text(stringResource(R.string.secret_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = { Text("Base32编码的密钥，至少16个字符") }
            )

            // Algorithm dropdown
            AlgorithmDropdown(
                selected = uiState.algorithm,
                onSelect = { viewModel.updateAlgorithm(it) }
            )

            // Digits
            Text(
                text = "${stringResource(R.string.digits_label)}: ${uiState.digits}",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(6, 8).forEach { d ->
                    FilterChip(
                        selected = uiState.digits == d,
                        onClick = { viewModel.updateDigits(d) },
                        label = { Text("$d 位") }
                    )
                }
            }

            // Period (only for TOTP)
            if (uiState.type == AccountType.TOTP) {
                Text(
                    text = "${stringResource(R.string.period_label)}: ${uiState.period}秒",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(15, 30, 60).forEach { p ->
                        FilterChip(
                            selected = uiState.period == p,
                            onClick = { viewModel.updatePeriod(p) },
                            label = { Text("${p}s") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save button
            Button(
                onClick = { viewModel.save() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(20.dp),
                        strokeWidth = 2.dp
                    )
                }
                Text(stringResource(R.string.save))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlgorithmDropdown(
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { androidx.compose.runtime.mutableStateOf(false) }
    val algorithms = listOf("SHA1", "SHA256", "SHA512")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.algorithm_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            algorithms.forEach { algo ->
                DropdownMenuItem(
                    text = { Text(algo) },
                    onClick = {
                        onSelect(algo)
                        expanded = false
                    }
                )
            }
        }
    }
}
