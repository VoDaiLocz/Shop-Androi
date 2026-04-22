package com.example.shop.admin.ui.category

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.shop.admin.viewmodel.AdminCategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateCategoryScreen(
    categoryId: Int,
    onNavigateBack: () -> Unit,
    viewModel: AdminCategoryViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    // Lấy dữ liệu danh mục hiện tại từ DB
    val currentCategory by viewModel.getCategoryById(categoryId).collectAsState(initial = null)

    // Khi dữ liệu được tải về, điền vào các ô nhập liệu
    LaunchedEffect(currentCategory) {
        currentCategory?.let {
            name = it.name
            imageUrl = it.imageUrl ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cập nhật danh mục") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (currentCategory == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier
                .padding(padding)
                .padding(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên danh mục") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Link ảnh danh mục") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.updateCategory(categoryId, name, imageUrl)
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cập nhật thay đổi")
                }
            }
        }
    }
}