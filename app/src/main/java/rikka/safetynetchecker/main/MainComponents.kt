package rikka.safetynetchecker.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainCard(content: @Composable () -> Unit) {
    Card(
        elevation = 0.dp,
        border = ButtonDefaults.outlinedBorder,
        modifier = Modifier.fillMaxWidth(),
        content = content
    )
}

@Composable
fun MainCardColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        content = content
    )
}

@Composable
fun MainCardItem(text1: String, text2: String) {
    Spacer(modifier = Modifier.height(8.dp))
    Text(text = text1, style = MaterialTheme.typography.overline)
    Text(text = text2)
}

@Composable
fun MainCardTitle(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}
