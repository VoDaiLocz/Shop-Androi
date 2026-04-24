import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CongratsScreen(onGoToHome: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🎉", fontSize = 80.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Đặt hàng thành công!", style = MaterialTheme.typography.headlineMedium)
        Text("Cảm ơn bạn đã mua sắm tại Shop")
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onGoToHome) {
            Text("Tiếp tục mua sắm")
        }
    }
}