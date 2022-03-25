package rikka.safetynetchecker.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.common.GoogleApiAvailability
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.safetynetchecker.theme.YetAnotherSafetyNetCheckerTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val composableScope = rememberCoroutineScope()

            YetAnotherSafetyNetCheckerTheme {
                val systemUiController = rememberSystemUiController()
                val useDarkIcons = MaterialTheme.colors.isLight

                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = useDarkIcons
                    )
                }

                val onRefreshClick: () -> Unit = {
                    composableScope.launch {
                        withContext(Dispatchers.IO) {
                            viewModel.checkSafetyNet(this@MainActivity)
                        }
                    }
                }
                val onLearnMoreClick: () -> Unit = {
                    try {
                        startActivity(
                            Intent(Intent.ACTION_VIEW)
                                .setData(Uri.parse("https://developer.android.com/training/safetynet/attestation"))
                        )
                    } catch (_: Throwable) {
                    }
                }
                val playServiceVersion = try {
                    val pi = packageManager.getPackageInfo(
                        GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE,
                        0
                    )
                    pi.versionName
                } catch (e: Throwable) {
                    null
                }
                MainScreen(
                    viewModel.result,
                    playServiceVersion,
                    onRefreshClick,
                    onLearnMoreClick
                )
            }
        }
    }
}
