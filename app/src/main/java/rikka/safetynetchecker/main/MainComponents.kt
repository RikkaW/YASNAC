package rikka.safetynetchecker.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import rikka.safetynetchecker.R
import rikka.safetynetchecker.icon.Cancel

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
    val dp = LocalConfiguration.current.smallestScreenWidthDp
    when {
        dp > 600 -> {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(text = text1, modifier = Modifier.fillMaxWidth(0.4f))
                Text(text = text2)
            }
        }
        else -> {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = text1, style = MaterialTheme.typography.overline)
            Spacer(modifier = Modifier.height(1.dp))
            Text(text = text2)
        }
    }
}

@Composable
fun MainCardPassOrFailItem(text: String, pass: Boolean) {
    val dp = LocalConfiguration.current.smallestScreenWidthDp
    when {
        dp > 600 -> {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(text = text, modifier = Modifier.fillMaxWidth(0.4f))
                PassOrFailText(pass = pass)
            }
        }
        else -> {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = text, style = MaterialTheme.typography.overline)
            Spacer(modifier = Modifier.height(1.dp))
            PassOrFailText(pass = pass)
        }
    }
}

@Composable
fun MainCardTitle(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        modifier = Modifier.padding(bottom = 2.dp)
    )
}

@Composable
fun MainButton(image: ImageVector, text: String, enabled: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled
    ) {
        Icon(
            image,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(text = text)
    }
}

@Composable
fun PassOrFailText(pass: Boolean) {
    val color = if (pass) MaterialTheme.colors.primary else MaterialTheme.colors.error
    val text = stringResource(if (pass) R.string.pass else R.string.fail)
    val imageVector = if (pass) Icons.Outlined.CheckCircle else Icons.Outlined.Cancel
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = color
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Icon(
            imageVector = imageVector,
            tint = color,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Preview
@Composable
fun PassText() {
    Box {
        PassOrFailText(true)
    }
}

@Preview
@Composable
fun FailText() {
    Box {
        PassOrFailText(false)
    }
}
