package rikka.safetynetchecker.main

import android.os.Build
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.insets.*
import com.google.accompanist.insets.ui.TopAppBar
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import rikka.safetynetchecker.R
import rikka.safetynetchecker.attest.AttestationStatement
import rikka.safetynetchecker.attest.OfflineVerify
import rikka.safetynetchecker.icon.Visibility
import rikka.safetynetchecker.theme.YetAnotherSafetyNetCheckerTheme
import rikka.safetynetchecker.util.ResultOf

@Composable
fun MainScreen(
    result: State<ResultOf<AttestationStatement>>,
    onRefreshClick: () -> Unit = {}
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
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.subtitle2,
                            fontSize = 18.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Text(
                            text = stringResource(id = R.string.app_name_long),
                            style = MaterialTheme.typography.caption,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                },
                backgroundColor = Color.Transparent,
                elevation = 0.dp,
                contentPadding = rememberInsetsPaddingValues(
                    LocalWindowInsets.current.statusBars,
                    applyBottom = false,
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        bottomBar = {
            Spacer(
                Modifier
                    .navigationBarsHeight()
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
                    .widthIn(max = 480.dp)
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)
                    .navigationBarsPadding()
            ) {
                MainCard {
                    DeviceInfoContent()
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (result.value != ResultOf.Initial) {
                    MainCard {
                        ResultContent(result)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                RefreshButton(result.value != ResultOf.Loading, onRefreshClick)
            }
        }
    }
}

@Composable
fun RefreshButton(enabled: Boolean, onRefreshClick: () -> Unit) {
    OutlinedButton(
        onClick = onRefreshClick,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled
    ) {
        Icon(
            Icons.Outlined.Refresh,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(text = stringResource(id = R.string.run_test_button))
    }
}

@Composable
fun DeviceInfoContent() {
    MainCardColumn {
        MainCardTextRow(
            text1 = stringResource(R.string.device),
            text2 = "${Build.MODEL} (${Build.DEVICE})"
        )
        MainCardTextRow(
            text1 = stringResource(R.string.android_version),
            text2 = "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            MainCardTextRow(
                text1 = stringResource(R.string.security_patch),
                text2 = Build.VERSION.SECURITY_PATCH
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
            Button(
                onClick = { openDialog.value = false }) {
                Text(stringResource(id = android.R.string.ok))
            }
        }
    )
}

@Composable
fun SuccessfulResultContent(statement: AttestationStatement) {
    MainCardTitle(text = stringResource(if (statement.isCtsProfileMatch && statement.hasBasicIntegrity()) R.string.success else R.string.failed))
    MainCardTextRow(
        text1 = "Basic integrity",
        text2 = stringResource(if (statement.hasBasicIntegrity()) R.string.pass else R.string.fail)
    )
    MainCardTextRow(
        text1 = "CTS profile match",
        text2 = stringResource(if (statement.isCtsProfileMatch) R.string.pass else R.string.fail)
    )
    MainCardTextRow(
        text1 = "Evaluation type",
        text2 = if (statement.hasHardwareBackedEvaluationType()) "HARDWARE_BACKED" else "BASIC"
    )

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
    MainCardTitle(text = stringResource(if (e is ApiException) R.string.api_error else R.string.something_went_wrong))
    if (e is ApiException) {
        Text(text = "${e.statusCode}: ${CommonStatusCodes.getStatusCodeString(e.statusCode)}")
    } else {
        Text(text = e.toString())
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
fun MainPreview() {
    ProvideWindowInsets {
        YetAnotherSafetyNetCheckerTheme {
            val result = remember {
                mutableStateOf(
                    ResultOf.Success(
                        OfflineVerify.process(
                            "eyJhbGciOiJSUzI1NiIsIng1YyI6WyJNSUlGWHpDQ0JFZWdBd0lCQWdJUWZtOGlZWnp1cTBFSkFBQUFBSDd1UlRBTkJna3Foa2lHOXcwQkFRc0ZBREJHTVFzd0NRWURWUVFHRXdKVlV6RWlNQ0FHQTFVRUNoTVpSMjl2WjJ4bElGUnlkWE4wSUZObGNuWnBZMlZ6SUV4TVF6RVRNQkVHQTFVRUF4TUtSMVJUSUVOQklERkVOREFlRncweU1UQTNNVGt4TXpFek5ESmFGdzB5TVRFd01UY3hNekV6TkRGYU1CMHhHekFaQmdOVkJBTVRFbUYwZEdWemRDNWhibVJ5YjJsa0xtTnZiVENDQVNJd0RRWUpLb1pJaHZjTkFRRUJCUUFEZ2dFUEFEQ0NBUW9DZ2dFQkFLZk5TQll4M002SnJJaTBMUURGNFVaaHRzeTgyQ280TnZ3ci9GSW43LzlnK3hzV3pDWWdSN1FzR21yeUc5dlBGckd5UXJFZGpDUXVCU1FTd29vNGdwaUhpdzFYbnFGZnJOYzJ3TFJPL1BUdStha0ZESU02Z3UzZGRnd1FXR0daclZQektFak95TlNGTUJMMFdBS2l1dVlCdjE0UXZublcxRWtZYnFKZFRoNkxXZmV2Y1dSSytUdFZhOXpzR25Fbmc3a01QV1BCSzBOMGJQZ3hiNGpueGFIcWxMeHEvQ2pEbkxrREVkdWZlVDVVZ3JsVG53OVVtWm1NeGFQdGEvdnowY2g2ZmxDd3lpdmpHajJ4VEhLVll2bVlwNFRmTGcwY1VOUEUxZEtqTkliS1lDeFFJVnpueHV4ZXBUU1ZpWXVjUEY0VnduKzZEOVp4UUpKKy9lNktMSWtDQXdFQUFhT0NBbkF3Z2dKc01BNEdBMVVkRHdFQi93UUVBd0lGb0RBVEJnTlZIU1VFRERBS0JnZ3JCZ0VGQlFjREFUQU1CZ05WSFJNQkFmOEVBakFBTUIwR0ExVWREZ1FXQkJUTXNUSTVxZ0FPUmtBZDNNUEwwNWg0NmJvVlhEQWZCZ05WSFNNRUdEQVdnQlFsNGhnT3NsZVJsQ3JsMUYyR2tJUGVVN080a2pCdEJnZ3JCZ0VGQlFjQkFRUmhNRjh3S2dZSUt3WUJCUVVITUFHR0htaDBkSEE2THk5dlkzTndMbkJyYVM1bmIyOW5MMmQwY3pGa05HbHVkREF4QmdnckJnRUZCUWN3QW9ZbGFIUjBjRG92TDNCcmFTNW5iMjluTDNKbGNHOHZZMlZ5ZEhNdlozUnpNV1EwTG1SbGNqQWRCZ05WSFJFRUZqQVVnaEpoZEhSbGMzUXVZVzVrY205cFpDNWpiMjB3SVFZRFZSMGdCQm93R0RBSUJnWm5nUXdCQWdFd0RBWUtLd1lCQkFIV2VRSUZBekEvQmdOVkhSOEVPREEyTURTZ01xQXdoaTVvZEhSd09pOHZZM0pzY3k1d2Eya3VaMjl2Wnk5bmRITXhaRFJwYm5RdlgwWlFjWEZKU0dkWU5qZ3VZM0pzTUlJQkF3WUtLd1lCQkFIV2VRSUVBZ1NCOUFTQjhRRHZBSFVBWE54RGt2N21xMFZFc1Y2YTFGYm1FRGY3MWZwSDNLRnpsTEplNXZiSERzb0FBQUY2dngyTzFnQUFCQU1BUmpCRUFpQkp1V1BSbVJNdmpjVFVwSWJyTktoOHN4Ykd4TlBNZmxicnYxZHhUakp3Q2dJZ1M5d2dMVUplUXFMTVI4WGVuR05meVloYXFsclJ4eE04c1A4VklwUUdTUzBBZGdCOVB2TDRqLytJVldna3dzREtubEtKZVN2RkRuZ0pmeTVxbDJpWmZpTHcxd0FBQVhxL0hZK0tBQUFFQXdCSE1FVUNJRDJMMnJIQmxKaTlSRm9PZkVCM2R4SGVIV1RKd3NwNDZJZklqNm9LS3BYYkFpRUEyNVNZRk04ZzFUK0dJVXJVTTB4Y05Ud2kvbHJxaFlrUU1HK0ZzMmZtRmRJd0RRWUpLb1pJaHZjTkFRRUxCUUFEZ2dFQkFENjhmeEhMeE9DK1ZsTjFSTkN5S2RUcWZIYlJBQWROWVg3N0hXL21QQm5VQzFocmVUR3hHeFNOMURoak1xNHpoOFBDbTB6L3JCM3BEd2lnbWlNdmFYUEVEazZEbGlNU0V5ZDBjNnkwOWg1V05XTi9jeGpHL3VRMDJ6REMvRWkvZmRFZ3UyMUhneHM3Q0VUdTN0ZTZCbzFSeC94R1FtK2toNXYwcHYraVl6cnhVbE8vTWRvb2lkejlCQ1hXOHZyTUo2UnNRVlJQeTR5RlcvMzcyN2x1RFpZMEh0NW1FRklKQ3BWQ2lCTHNpeDBwbVRsa1padXREaC8vTWRNNUE0NzFWQUMxU0l4ekMzT2F0dFhWTFNtSXZnd1hXYlo5azJsekppekFsbFJLVWtNTFRkc09EcDUzM25Pa2RWU1o2ZitIcnFJc1RMTnM1UVNLYkU0cnhydlZOKzQ9IiwiTUlJRmpEQ0NBM1NnQXdJQkFnSU5BZ0NPc2dJek5tV0xaTTNibXpBTkJna3Foa2lHOXcwQkFRc0ZBREJITVFzd0NRWURWUVFHRXdKVlV6RWlNQ0FHQTFVRUNoTVpSMjl2WjJ4bElGUnlkWE4wSUZObGNuWnBZMlZ6SUV4TVF6RVVNQklHQTFVRUF4TUxSMVJUSUZKdmIzUWdVakV3SGhjTk1qQXdPREV6TURBd01EUXlXaGNOTWpjd09UTXdNREF3TURReVdqQkdNUXN3Q1FZRFZRUUdFd0pWVXpFaU1DQUdBMVVFQ2hNWlIyOXZaMnhsSUZSeWRYTjBJRk5sY25acFkyVnpJRXhNUXpFVE1CRUdBMVVFQXhNS1IxUlRJRU5CSURGRU5EQ0NBU0l3RFFZSktvWklodmNOQVFFQkJRQURnZ0VQQURDQ0FRb0NnZ0VCQUt2QXFxUENFMjdsMHc5ekM4ZFRQSUU4OWJBK3hUbURhRzd5N1ZmUTRjK21PV2hsVWViVVFwSzB5djJyNjc4UkpFeEswSFdEamVxK25MSUhOMUVtNWo2ckFSWml4bXlSU2poSVIwS09RUEdCTVVsZHNhenRJSUo3TzBnLzgycWovdkdEbC8vM3Q0dFRxeGlSaExRblRMWEpkZUIrMkRoa2RVNklJZ3g2d043RTVOY1VIM1Jjc2VqY3FqOHA1U2oxOXZCbTZpMUZocUxHeW1oTUZyb1dWVUdPM3h0SUg5MWRzZ3k0ZUZLY2ZLVkxXSzNvMjE5MFEwTG0vU2lLbUxiUko1QXU0eTFldUZKbTJKTTllQjg0RmtxYTNpdnJYV1VlVnR5ZTBDUWRLdnNZMkZrYXp2eHR4dnVzTEp6TFdZSGs1NXpjUkFhY0RBMlNlRXRCYlFmRDFxc0NBd0VBQWFPQ0FYWXdnZ0Z5TUE0R0ExVWREd0VCL3dRRUF3SUJoakFkQmdOVkhTVUVGakFVQmdnckJnRUZCUWNEQVFZSUt3WUJCUVVIQXdJd0VnWURWUjBUQVFIL0JBZ3dCZ0VCL3dJQkFEQWRCZ05WSFE0RUZnUVVKZUlZRHJKWGtaUXE1ZFJkaHBDRDNsT3p1Skl3SHdZRFZSMGpCQmd3Rm9BVTVLOHJKbkVhSzBnbmhTOVNaaXp2OElrVGNUNHdhQVlJS3dZQkJRVUhBUUVFWERCYU1DWUdDQ3NHQVFVRkJ6QUJoaHBvZEhSd09pOHZiMk56Y0M1d2Eya3VaMjl2Wnk5bmRITnlNVEF3QmdnckJnRUZCUWN3QW9Za2FIUjBjRG92TDNCcmFTNW5iMjluTDNKbGNHOHZZMlZ5ZEhNdlozUnpjakV1WkdWeU1EUUdBMVVkSHdRdE1Dc3dLYUFub0NXR0kyaDBkSEE2THk5amNtd3VjR3RwTG1kdmIyY3ZaM1J6Y2pFdlozUnpjakV1WTNKc01FMEdBMVVkSUFSR01FUXdDQVlHWjRFTUFRSUJNRGdHQ2lzR0FRUUIxbmtDQlFNd0tqQW9CZ2dyQmdFRkJRY0NBUlljYUhSMGNITTZMeTl3YTJrdVoyOXZaeTl5WlhCdmMybDBiM0o1THpBTkJna3Foa2lHOXcwQkFRc0ZBQU9DQWdFQUlWVG95MjRqd1hVcjByQVBjOTI0dnVTVmJLUXVZdzNuTGZsTGZMaDVBWVdFZVZsL0R1MThRQVdVTWRjSjZvL3FGWmJoWGtCSDBQTmN3OTd0aGFmMkJlb0RZWTlDay9iK1VHbHVoeDA2emQ0RUJmN0g5UDg0bm5yd3BSKzRHQkRaSytYaDNJMHRxSnkycmdPcU5EZmxyNUlNUThaVFdBM3lsdGFrelNCS1o2WHBGMFBwcXlDUnZwL05DR3YyS1gyVHVQQ0p2c2NwMS9tMnBWVHR5QmpZUFJRK1F1Q1FHQUpLanRON1I1REZyZlRxTVd2WWdWbHBDSkJrd2x1Nys3S1kzY1RJZnpFN2NtQUxza01LTkx1RHorUnpDY3NZVHNWYVU3VnAzeEw2ME9ZaHFGa3VBT094RFo2cEhPajkrT0ptWWdQbU9UNFgzKzdMNTFmWEp5Ukg5S2ZMUlA2blQzMUQ1bm1zR0FPZ1oyNi84VDloc0JXMXVvOWp1NWZaTFpYVlZTNUgwSHlJQk1FS3lHTUlQaEZXcmx0L2hGUzI4TjF6YUtJMFpCR0QzZ1lnRExiaURUOWZHWHN0cGsrRm1jNG9sVmxXUHpYZTgxdmRvRW5GYnI1TTI3MkhkZ0pXbytXaFQ5QllNMEppK3dkVm1uUmZmWGdsb0VvbHVUTmNXemM0MWRGcGdKdThmRjNMRzBnbDJpYlNZaUNpOWE2aHZVMFRwcGpKeUlXWGhrSlRjTUpsUHJXeDFWeXRFVUdyWDJsMEpEd1JqVy82NTZyMEtWQjAyeEhSS3ZtMlpLSTAzVGdsTElwbVZDSzNrQktrS05wQk5rRnQ4cmhhZmNDS09iOUp4Lzl0cE5GbFFUbDdCMzlySmxKV2tSMTdRblpxVnB0RmVQRk9Sb1ptRnpNPSIsIk1JSUZZakNDQkVxZ0F3SUJBZ0lRZDcwTmJOczIrUnJxSVEvRThGalREVEFOQmdrcWhraUc5dzBCQVFzRkFEQlhNUXN3Q1FZRFZRUUdFd0pDUlRFWk1CY0dBMVVFQ2hNUVIyeHZZbUZzVTJsbmJpQnVkaTF6WVRFUU1BNEdBMVVFQ3hNSFVtOXZkQ0JEUVRFYk1Ca0dBMVVFQXhNU1IyeHZZbUZzVTJsbmJpQlNiMjkwSUVOQk1CNFhEVEl3TURZeE9UQXdNREEwTWxvWERUSTRNREV5T0RBd01EQTBNbG93UnpFTE1Ba0dBMVVFQmhNQ1ZWTXhJakFnQmdOVkJBb1RHVWR2YjJkc1pTQlVjblZ6ZENCVFpYSjJhV05sY3lCTVRFTXhGREFTQmdOVkJBTVRDMGRVVXlCU2IyOTBJRkl4TUlJQ0lqQU5CZ2txaGtpRzl3MEJBUUVGQUFPQ0FnOEFNSUlDQ2dLQ0FnRUF0aEVDaXg3am9YZWJPOXkvbEQ2M2xhZEFQS0g5Z3ZsOU1nYUNjZmIyakgvNzZOdThhaTZYbDZPTVMva3I5ckg1em9RZHNmbkZsOTd2dWZLajZid1NpVjZucWxLcitDTW55NlN4bkdQYjE1bCs4QXBlNjJpbTlNWmFSdzFORURQalRyRVRvOGdZYkV2cy9BbVEzNTFrS1NVakI2RzAwajB1WU9EUDBnbUh1ODFJOEUzQ3ducUlpcnU2ejFrWjFxK1BzQWV3bmpIeGdzSEEzeTZtYld3WkRyWFlmaVlhUlFNOXNIbWtsQ2l0RDM4bTVhZ0kvcGJvUEdpVVUrNkRPb2dyRlpZSnN1QjZqQzUxMXB6cnAxWmtqNVpQYUs0OWw4S0VqOEM4UU1BTFhMMzJoN00xYkt3WVVIK0U0RXpOa3RNZzZUTzhVcG12TXJVcHN5VXF0RWo1Y3VIS1pQZm1naENONkozQ2lvajZPR2FLL0dQNUFmbDQvWHRjZC9wMmgvcnMzN0VPZVpWWHRMMG03OVlCMGVzV0NydU9DN1hGeFlwVnE5T3M2cEZMS2N3WnBESWxUaXJ4WlVUUUFzNnF6a20wNnA5OGc3QkFlK2REcTZkc280OTlpWUg2VEtYLzFZN0R6a3ZndGRpemprWFBkc0R0UUN2OVV3K3dwOVU3RGJHS29nUGVNYTNNZCtwdmV6N1czNUVpRXVhKyt0Z3kvQkJqRkZGeTNsM1dGcE85S1dnejd6cG03QWVLSnQ4VDExZGxlQ2ZlWGtrVUFLSUFmNXFvSWJhcHNaV3dwYmtORmhIYXgyeElQRURnZmcxYXpWWTgwWmNGdWN0TDdUbExuTVEvMGxVVGJpU3cxbkg2OU1HNnpPMGI5ZjZCUWRnQW1EMDZ5SzU2bURjWUJaVUNBd0VBQWFPQ0FUZ3dnZ0UwTUE0R0ExVWREd0VCL3dRRUF3SUJoakFQQmdOVkhSTUJBZjhFQlRBREFRSC9NQjBHQTFVZERnUVdCQlRrcnlzbWNSb3JTQ2VGTDFKbUxPL3dpUk54UGpBZkJnTlZIU01FR0RBV2dCUmdlMllhUlEyWHlvbFFMMzBFelRTby8vejlTekJnQmdnckJnRUZCUWNCQVFSVU1GSXdKUVlJS3dZQkJRVUhNQUdHR1doMGRIQTZMeTl2WTNOd0xuQnJhUzVuYjI5bkwyZHpjakV3S1FZSUt3WUJCUVVITUFLR0hXaDBkSEE2THk5d2Eya3VaMjl2Wnk5bmMzSXhMMmR6Y2pFdVkzSjBNRElHQTFVZEh3UXJNQ2t3SjZBbG9DT0dJV2gwZEhBNkx5OWpjbXd1Y0d0cExtZHZiMmN2WjNOeU1TOW5jM0l4TG1OeWJEQTdCZ05WSFNBRU5EQXlNQWdHQm1lQkRBRUNBVEFJQmdabmdRd0JBZ0l3RFFZTEt3WUJCQUhXZVFJRkF3SXdEUVlMS3dZQkJBSFdlUUlGQXdNd0RRWUpLb1pJaHZjTkFRRUxCUUFEZ2dFQkFEU2tIckVvbzlDMGRoZW1NWG9oNmRGU1BzamJkQlpCaUxnOU5SM3Q1UCtUNFZ4ZnE3dnFmTS9iNUEzUmkxZnlKbTlidmhkR2FKUTNiMnQ2eU1BWU4vb2xVYXpzYUwreXlFbjlXcHJLQVNPc2hJQXJBb3labCt0SmFveDExOGZlc3NtWG4xaElWdzQxb2VRYTF2MXZnNEZ2NzR6UGw2L0FoU3J3OVU1cENaRXQ0V2k0d1N0ejZkVFovQ0xBTng4TFpoMUo3UUpWajJmaE10ZlRKcjl3NHozMFoyMDlmT1UwaU9NeStxZHVCbXB2dll1UjdoWkw2RHVwc3pmbncwU2tmdGhzMThkRzlaS2I1OVVodm1hU0daUlZiTlFwc2czQlpsdmlkMGxJS08yZDF4b3pjbE96Z2pYUFlvdkpKSXVsdHprTXUzNHFRYjlTei95aWxyYkNnajg9Il19.eyJub25jZSI6ImJtOXVZMlU9IiwidGltZXN0YW1wTXMiOjE2MzIyNDc5NDE4MjMsImN0c1Byb2ZpbGVNYXRjaCI6ZmFsc2UsImFwa0NlcnRpZmljYXRlRGlnZXN0U2hhMjU2IjpbXSwiYmFzaWNJbnRlZ3JpdHkiOmZhbHNlLCJhZHZpY2UiOiJSRVNUT1JFX1RPX0ZBQ1RPUllfUk9NLExPQ0tfQk9PVExPQURFUiIsImV2YWx1YXRpb25UeXBlIjoiQkFTSUMsSEFSRFdBUkVfQkFDS0VEIn0.BIqN61Dy_Fn6J6QFeHZeRDa9XqHIuzg2siDiadp4i3K9ko31IpOeuzf-k4BTiMyZ0mFiCpLZenTnABlrJ9WPwIJZFImI6WvihXt1vwTcVoQKi0W1rUgZK6ZM1_bTlo_WUPgDKE9Bs5MONNyiiKcAAsUKH5lztPZ4xX-wiDrWx5vVTCR6chafYgN_1qZnosRzDfWas3AoFfe5U4CX7hEFFq8fPE4nbLG3R3XDh9WDS77IcEha9IU6K06IVqpxDNlK9AgVWrVgP9JE_asEAA-13gsCTpoKXRo0MWhlWefpuVXsv7d1xFfxSUW7awqQr-1xqARxj2UbMLv6UaVT_BocvA"
                        )
                    )
                )
            }
            MainScreen(result)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainPreviewPhone() {
    MainPreview()
}

@Preview(showBackground = true, widthDp = 720, heightDp = 540)
@Composable
fun MainPreviewTablet() {
    MainPreview()
}
