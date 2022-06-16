package rikka.safetynetchecker.main

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.safetynet.SafetyNet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rikka.safetynetchecker.BuildConfig
import rikka.safetynetchecker.attest.AttestationException
import rikka.safetynetchecker.attest.AttestationStatement
import rikka.safetynetchecker.attest.OfflineVerify
import rikka.safetynetchecker.util.ResultOf
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.util.*


class MainViewModel : ViewModel() {

    val result: MutableState<ResultOf<AttestationStatement>> = mutableStateOf(ResultOf.Initial)

    private val keys: Array<String> = BuildConfig.API_KEY.apply { shuffle() }
    private var count = 0

    private val fingerprint = "${Build.BRAND}/${Build.PRODUCT}/${Build.DEVICE}:" +
            "${Build.VERSION.RELEASE}/${Build.ID}/${Build.VERSION.INCREMENTAL}:" +
            "${Build.TYPE}/${Build.TAGS}"

    private fun getNonce(): String {
        var s = "${UUID.randomUUID()}\n" +
                "${OffsetDateTime.now()}\n" +
                "${fingerprint}\n" +
                "${Build.VERSION.SDK_INT}\n"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            s += "${Build.VERSION.SECURITY_PATCH}\n"
        }
        return s
    }

    fun checkSafetyNet(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        result.value = ResultOf.Loading

        val availability = GoogleApiAvailability.getInstance()
        availability.isGooglePlayServicesAvailable(context, 13000000).let {
            if (it == ConnectionResult.SUCCESS) {
                return@let
            }

            val reason = when (it) {
                ConnectionResult.SERVICE_MISSING -> "Google Play services is missing on this device."
                ConnectionResult.SERVICE_UPDATING -> "Google Play service is currently being updated on this device."
                ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> "The installed version of Google Play services is out of date."
                ConnectionResult.SERVICE_DISABLED -> "The installed version of Google Play services has been disabled on this device."
                ConnectionResult.SERVICE_INVALID -> "The version of the Google Play services installed on this device is not authentic."
                else -> "Unknown result ${availability.getErrorString(it)}."
            }
            result.value = ResultOf.Failure(AttestationException(reason))
            return@launch
        }

        val nonce = getNonce()
        val requestTime = Instant.now()

        val key: String
        if (count < keys.size) {
            key = keys[count]
            count++
        } else {
            count = 0
            key = keys[count]
            count++
        }

        SafetyNet.getClient(context.applicationContext)
            .attest(nonce.toByteArray(), key)
            .addOnSuccessListener {
                try {
                    val statement = OfflineVerify.process(it.jwsResult)
                    if (nonce != statement.nonce) {
                        throw AttestationException("Nonce does not match")
                    }
                    val timeout = requestTime.plus(Duration.ofSeconds(10))
                    val statementTime = Instant.ofEpochMilli(statement.timestampMs)
                    if (statementTime.isAfter(timeout)) {
                        throw AttestationException("Attestation response timeout")
                    }
                    if (statement.isCtsProfileMatch) {
                        if (BuildConfig.APPLICATION_ID != statement.apkPackageName) {
                            throw AttestationException("Application id does not match")
                        }
                        if (!statement.apkCertificateDigestSha256.contains(BuildConfig.certificateDigest)) {
                            throw AttestationException("Apk certificate does not match")
                        }
                    }

                    result.value = ResultOf.Success(statement)
                } catch (e: AttestationException) {
                    Log.w(TAG, "OfflineVerify: ", e)
                    result.value = ResultOf.Failure(e)
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "checkSafetyNet: ", e)
                result.value = ResultOf.Failure(e)
            }
    }

    companion object {
        private const val TAG = "YASNAC"
    }
}
