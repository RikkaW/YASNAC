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
import com.google.android.gms.security.ProviderInstaller
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rikka.safetynetchecker.BuildConfig
import rikka.safetynetchecker.R
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

            val id = when (it) {
                ConnectionResult.SERVICE_MISSING -> R.string.error_gms_missing
                ConnectionResult.SERVICE_UPDATING -> R.string.error_gms_updating
                ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> R.string.error_gms_version_update_required
                ConnectionResult.SERVICE_DISABLED -> R.string.error_gms_disabled
                ConnectionResult.SERVICE_INVALID -> R.string.error_gms_invalid
                else -> R.string.error_gms_unknown
            }
            val reason = context.getString(id, availability.getErrorString(it))
            result.value = ResultOf.Failure(AttestationException(reason))
            return@launch
        }

        try {
            ProviderInstaller.installIfNeeded(context)
        } catch (e: Exception) {
            Log.w(TAG, "GmsProviderInstaller: ", e)
            val wrap = AttestationException(context.getString(R.string.error_gms_provider), e)
            result.value = ResultOf.Failure(wrap)
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
                    val statement = OfflineVerify.process(context, it.jwsResult)
                    if (nonce != statement.nonce) {
                        throw AttestationException(context.getString(R.string.error_statement_nonce))
                    }
                    val timeout = requestTime.plus(Duration.ofSeconds(10))
                    val statementTime = Instant.ofEpochMilli(statement.timestampMs)
                    if (statementTime.isAfter(timeout)) {
                        throw AttestationException(context.getString(R.string.error_statement_timeout))
                    }
                    if (statement.isCtsProfileMatch) {
                        if (BuildConfig.APPLICATION_ID != statement.apkPackageName) {
                            throw AttestationException(context.getString(R.string.error_statement_package_name))
                        }
                        if (!statement.apkCertificateDigestSha256.contains(BuildConfig.certificateDigest)) {
                            throw AttestationException(context.getString(R.string.error_statement_certificate))
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
