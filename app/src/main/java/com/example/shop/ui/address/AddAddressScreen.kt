package com.example.shop.ui.address

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.shop.viewmodel.AddressViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAddressScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddressViewModel = hiltViewModel()
) {
    // Trạng thái cho các ô nhập liệu
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thêm địa chỉ mới") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tên gợi nhớ (Ví dụ: Nhà riêng, Công ty)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Home, null) }
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Số điện thoại nhận hàng") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Phone, null) }
            )

            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("Tỉnh / Thành phố") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.LocationCity, null) }
            )

            OutlinedTextField(
                value = detail,
                onValueChange = { detail = it },
                label = { Text("Địa chỉ chi tiết (Số nhà, tên đường...)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                leadingIcon = { Icon(Icons.Default.Person, null) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank() && detail.isNotBlank()) {
                        viewModel.addAddress(
                            name = name,
                            phone = phone,
                            detail = detail,
                            city = city
                        )
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = name.isNotBlank() && phone.isNotBlank() && detail.isNotBlank()
            ) {
                Text("Lưu địa chỉ")
            }
        }
    }
}