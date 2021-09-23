package rikka.safetynetchecker.main

import android.app.Activity
import android.os.Build
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.android.gms.safetynet.SafetyNet
import rikka.safetynetchecker.BuildConfig
import rikka.safetynetchecker.attest.AttestationStatement
import rikka.safetynetchecker.attest.OfflineVerify
import rikka.safetynetchecker.util.ResultOf
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.security.SecureRandom
import java.util.*


class MainViewModel : ViewModel() {

    val result: MutableState<ResultOf<AttestationStatement>> = mutableStateOf(ResultOf.Initial)

    private val random: Random = SecureRandom()

    private fun getNonce(): ByteArray {
        val byteStream = ByteArrayOutputStream()
        val bytes = ByteArray(24)
        random.nextBytes(bytes)
        try {
            byteStream.write(bytes)
            byteStream.write(System.currentTimeMillis().toString().toByteArray())
            byteStream.write(Build.MODEL.toString().toByteArray())
            byteStream.write(Build.VERSION.SDK_INT.toString().toByteArray())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                byteStream.write(Build.VERSION.SECURITY_PATCH.toString().toByteArray())
            }
        } catch (e: IOException) {
            throw IllegalStateException("Unable to generate nonce")
        }

        return byteStream.toByteArray()
    }

    fun checkSafetyNet(activity: Activity) {
        result.value = ResultOf.Loading

        SafetyNet.getClient(activity).attest(getNonce(), BuildConfig.API_KEY)
            .addOnSuccessListener(activity) {
                try {
                    result.value = (ResultOf.Success(OfflineVerify.process(it.jwsResult)))
                } catch (e: Throwable) {
                    result.value = (ResultOf.Failure(e))
                }
            }
            .addOnFailureListener(activity) { e ->
                result.value = (ResultOf.Failure(e))
            }
    }
}
