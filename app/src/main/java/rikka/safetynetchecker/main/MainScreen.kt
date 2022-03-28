package rikka.safetynetchecker.main

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.insets.ui.TopAppBar
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import rikka.safetynetchecker.BuildConfig
import rikka.safetynetchecker.R
import rikka.safetynetchecker.attest.AttestationStatement
import rikka.safetynetchecker.attest.OfflineVerify
import rikka.safetynetchecker.icon.OpenInNew
import rikka.safetynetchecker.icon.Visibility
import rikka.safetynetchecker.theme.YetAnotherSafetyNetCheckerTheme
import rikka.safetynetchecker.util.ResultOf
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun MainScreen(
    result: State<ResultOf<AttestationStatement>>,
    playServiceVersion: String?,
    onRefreshClick: () -> Unit = {},
    onLearnMoreClick: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = stringResource(id = R.string.app_name_short),
                            style = MaterialTheme.typography.subtitle2,
                            fontSize = 18.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Text(
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.caption,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                },
                backgroundColor = Color.Transparent,
                elevation = 0.dp,
                contentPadding = WindowInsets.statusBars
                    .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                    .asPaddingValues(),
                modifier = Modifier.fillMaxWidth()
            )
        },
        bottomBar = {
            Spacer(
                Modifier
                    .windowInsetsBottomHeight(WindowInsets.navigationBars)
                    .fillMaxWidth()
            )
        }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .widthIn(max = 600.dp)
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)
                    .navigationBarsPadding()
            ) {
                MainCard {
                    DeviceInfoContent(playServiceVersion)
                }
                Spacer(modifier = Modifier.height(8.dp))

                val showContent = result.value != ResultOf.Initial

                AnimatedVisibility(visible = showContent) {
                    Box(
                        modifier = Modifier.animateContentSize()
                    ) {
                        MainCard {
                            ResultContent(result)
                        }
                    }
                }

                if (showContent) {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                MainButton(
                    image = Icons.Outlined.Refresh,
                    text = stringResource(id = R.string.run_test_button),
                    enabled = result.value != ResultOf.Loading,
                    onClick = onRefreshClick
                )

                Spacer(modifier = Modifier.height(8.dp))

                MainButton(
                    image = Icons.Outlined.OpenInNew,
                    text = stringResource(id = R.string.learn_more_button),
                    enabled = true,
                    onClick = onLearnMoreClick
                )
            }
        }
    }
}

@Composable
fun DeviceInfoContent(playServiceVersion: String?) {
    MainCardColumn {
        MainCardTitle(text = stringResource(R.string.device))

        MainCardItem(
            text1 = stringResource(R.string.model),
            text2 = "${Build.MODEL} (${Build.DEVICE})"
        )
        MainCardItem(
            text1 = stringResource(R.string.android_version),
            text2 = "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            MainCardItem(
                text1 = stringResource(R.string.security_patch),
                text2 = Build.VERSION.SECURITY_PATCH
            )
        }

        if (playServiceVersion != null) {
            MainCardItem(
                text1 = stringResource(R.string.play_services_version),
                text2 = playServiceVersion
            )
        }
    }
}

@Composable
fun ShowRawJsonDialog(text: String, openDialog: MutableState<Boolean>) {
    AlertDialog(
        onDismissRequest = {
            openDialog.value = false
        },
        text = {
            SelectionContainer {
                Text(
                    text = text,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { openDialog.value = false }) {
                Text(stringResource(id = android.R.string.ok))
            }
        }
    )
}

@Composable
fun SuccessfulResultContent(statement: AttestationStatement) {
    MainCardTitle(text = stringResource(R.string.result))
    MainCardPassOrFailItem(
        text = stringResource(R.string.basic_integrity),
        pass = statement.hasBasicIntegrity()
    )
    MainCardPassOrFailItem(
        text = stringResource(R.string.cts_profile_match),
        pass = statement.isCtsProfileMatch
    )
    MainCardItem(
        text1 = stringResource(R.string.evaluation_type),
        text2 = if (statement.hasHardwareBackedEvaluationType()) {
            stringResource(R.string.hardware_backed)
        } else {
            stringResource(R.string.basic)
        }
    )
    val time = Instant.ofEpochMilli(statement.timestampMs).atZone(ZoneId.systemDefault())
    MainCardItem(
        text1 = stringResource(R.string.timestamp),
        text2 = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(time)
    )
    if (statement.isCtsProfileMatch) {
        MainCardItem(
            text1 = stringResource(R.string.apk_digest),
            text2 = statement.apkDigestSha256
        )
    }
    if (!statement.advice.isNullOrEmpty()) {
        MainCardItem(
            text1 = stringResource(R.string.advice),
            text2 = statement.advice
        )
    }

    val openDialog = remember { mutableStateOf(false) }

    Spacer(modifier = Modifier.height(12.dp))

    TextButton(
        onClick = { openDialog.value = true },
        shape = MaterialTheme.shapes.medium,
    ) {
        Icon(
            Icons.Outlined.Visibility,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(text = stringResource(R.string.view_json))
    }

    if (openDialog.value) {
        val rawJson = statement.toPrettyString()
        ShowRawJsonDialog(rawJson, openDialog = openDialog)
    }
}

@Preview
@Composable
fun LoadingContentPreview() {
    MainCard {
        MainCardColumn {
            LoadingContent()
        }
    }
}

@Composable
fun LoadingContent() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        Spacer(Modifier.size(24.dp))
        Text(text = stringResource(R.string.please_wait))
    }
}

@Preview
@Composable
fun FailureContentPreview() {
    MainCard {
        MainCardColumn {
            FailureContent(RuntimeException("test"))
        }
    }
}

@Composable
fun FailureContent(e: Throwable) {
    val resId = if (e is ApiException) R.string.api_error else R.string.something_went_wrong
    MainCardTitle(text = stringResource(resId))
    if (e is ApiException) {
        Text(text = "${e.statusCode}: ${CommonStatusCodes.getStatusCodeString(e.statusCode)}")
    } else {
        Text(text = e.message ?: e.stackTraceToString())
    }
}

@Composable
fun ResultContent(state: State<ResultOf<AttestationStatement>>) {
    MainCardColumn {
        when (val result = state.value) {
            is ResultOf.Success -> SuccessfulResultContent(result.value)
            is ResultOf.Failure -> FailureContent(result.error)
            ResultOf.Loading -> LoadingContent()
            ResultOf.Initial -> return@MainCardColumn
        }
    }
}

@Composable
fun MainPreview(attestationStatement: AttestationStatement) {
    YetAnotherSafetyNetCheckerTheme {
        val result = remember {
            mutableStateOf(
                ResultOf.Success(attestationStatement)
            )
        }
        MainScreen(result, "1 (1)")
    }
}

@Preview(showBackground = true)
@Composable
fun MainPreviewPhone() {
    MainPreview(OfflineVerify.process(BuildConfig.SAMPLE))
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun MainPreviewPhoneNight() {
    MainPreview(OfflineVerify.process(BuildConfig.SAMPLE))
}

@Preview(showBackground = true, device = Devices.PIXEL_C)
@Composable
fun MainPreviewTablet() {
    MainPreview(OfflineVerify.process(BuildConfig.SAMPLE))
}
