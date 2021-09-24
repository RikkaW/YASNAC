package rikka.safetynetchecker.main

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.android.gms.safetynet.SafetyNet
import rikka.safetynetchecker.BuildConfig
import rikka.safetynetchecker.attest.AttestationException
import rikka.safetynetchecker.attest.AttestationStatement
import rikka.safetynetchecker.attest.OfflineVerify
import rikka.safetynetchecker.util.ResultOf
import java.time.OffsetDateTime
import java.util.*


class MainViewModel : ViewModel() {

    val result: MutableState<ResultOf<AttestationStatement>> = mutableStateOf(ResultOf.Initial)

    private val fingerprint = "${Build.BRAND}/${Build.PRODUCT}/${Build.DEVICE}:" +
            "${Build.VERSION.RELEASE}/${Build.ID}/${Build.VERSION.INCREMENTAL}:" +
            "${Build.TYPE}/${Build.TAGS}"

    private fun getNonce(): String {
        var s = "${UUID.randomUUID()}\n" +
                "${OffsetDateTime.now()}\n" +
                "${fingerprint}\n" +
                "${Build.VERSION.SDK_INT}\n";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            s += "${Build.VERSION.SECURITY_PATCH}\n"
        }
        return s
    }

    fun checkSafetyNet(context: Context) {
        result.value = ResultOf.Loading
        val nonce = getNonce()
        SafetyNet.getClient(context.applicationContext).attest(nonce.toByteArray(), BuildConfig.API_KEY)
            .addOnSuccessListener {
                try {
                    val statement = OfflineVerify.process(it.jwsResult)
                    statement.originalNonce = nonce
                    result.value = (ResultOf.Success(statement))
                } catch (e: AttestationException) {
                    Log.w(TAG, "OfflineVerify: ", e)
                    result.value = (ResultOf.Failure(e))
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "checkSafetyNet: ", e)
                result.value = (ResultOf.Failure(e))
            }
    }

    companion object {
        private const val TAG = "YASNAC"
    }
}
