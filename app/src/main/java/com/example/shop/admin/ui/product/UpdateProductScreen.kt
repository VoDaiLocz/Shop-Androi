package com.example.shop.admin.ui.product

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.shop.admin.viewmodel.AdminProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateProductScreen(
    productId: Int,
    onNavigateBack: () -> Unit,
    viewModel: AdminProductViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    //State cho Category
    val categories by viewModel.allCategories.collectAsState(initial = emptyList())
    var expanded by remember { mutableStateOf(false) }
    var selectedCategoryName by remember { mutableStateOf("Chọn danh mục") }
    var selectedCategoryId by remember { mutableIntStateOf(-1) }


    val currentProduct by viewModel.getProductById(productId).collectAsState(initial = null)

    //Đổ dữ liệu cũ vào các ô nhập liệu khi lấy được product
    LaunchedEffect(currentProduct) {
        currentProduct?.let { product ->
            name = product.name
            price = product.price.toString()
            description = product.description
            quantity = product.quantity.toString()
            selectedCategoryId = product.categoryId

            // Tìm tên danh mục tương ứng
            selectedCategoryName = categories.find { it.id == product.categoryId }?.name ?: "Danh mục cũ"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cập nhật sản phẩm") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (currentProduct == null) {
            // Hiển thị loading trong khi đợi lấy dữ liệu
            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên sản phẩm") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Giá sản phẩm") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Số lượng") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Dropdown chọn Category
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCategoryName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Danh mục") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategoryId = category.id
                                    selectedCategoryName = category.name
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Mô tả") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                OutlinedButton(
                    onClick = { imagePicker.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (imageUri == null) "Chọn ảnh mới từ máy" else "Đã chọn ảnh mới")
                }

                errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val productPrice = price.toDoubleOrNull()
                        val productQuantity = quantity.toIntOrNull()
                        when {
                            name.isBlank() -> errorMessage = "Vui lòng nhập tên sản phẩm."
                            productPrice == null -> errorMessage = "Giá sản phẩm không hợp lệ."
                            productQuantity == null -> errorMessage = "Số lượng không hợp lệ."
                            selectedCategoryId == -1 -> errorMessage = "Vui lòng chọn danh mục."
                            else -> {
                                errorMessage = null
                                viewModel.updateProduct(
                                    id = productId,
                                    name = name,
                                    price = productPrice,
                                    description = description,
                                    imageUrl = currentProduct?.imageUrl.orEmpty(),
                                    quantity = productQuantity,
                                    categoryId = selectedCategoryId,
                                    imageUri = imageUri
                                ) { success ->
                                    if (success) {
                                        onNavigateBack()
                                    } else {
                                        errorMessage = "Không cập nhật được sản phẩm. Vui lòng kiểm tra dữ liệu hoặc quyền admin."
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Cập nhật thay đổi", modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}
