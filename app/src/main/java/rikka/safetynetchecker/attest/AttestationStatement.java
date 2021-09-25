package rikka.safetynetchecker.attest;

import androidx.annotation.Nullable;

import com.google.api.client.json.webtoken.JsonWebSignature;
import com.google.api.client.util.Key;
import com.google.common.io.BaseEncoding;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * A statement returned by the Attestation API.
 */
public class AttestationStatement extends JsonWebSignature.Payload {
    /**
     * Embedded nonce sent as part of the request.
     */
    @Key
    private String nonce;

    /**
     * Timestamp of the request.
     */
    @Key
    private long timestampMs;

    /**
     * Package name of the APK that submitted this request.
     */
    @Key
    @Nullable
    private String apkPackageName;

    /**
     * Digest of certificate of the APK that submitted this request.
     */
    @Key
    private String[] apkCertificateDigestSha256;

    /**
     * Digest of the APK that submitted this request.
     */
    @Key
    @Nullable
    private String apkDigestSha256;

    /**
     * The device passed CTS and matches a known profile.
     */
    @Key
    private boolean ctsProfileMatch;

    /**
     * The device has passed a basic integrity test, but the CTS profile could not be verified.
     */
    @Key
    private boolean basicIntegrity;

    /**
     * Types of measurements that contributed to this response.
     */
    @Key
    private String evaluationType;

    @Key
    private String advice;

    public String getNonce() {
        return new String(BaseEncoding.base64().decode(nonce), StandardCharsets.UTF_8);
    }

    public long getTimestampMs() {
        return timestampMs;
    }

    public String getApkPackageName() {
        return String.valueOf(apkPackageName);
    }

    public String getApkDigestSha256() {
        return String.valueOf(apkDigestSha256);
    }

    public List<String> getApkCertificateDigestSha256() {
        return Arrays.asList(apkCertificateDigestSha256);
    }

    public boolean isCtsProfileMatch() {
        return ctsProfileMatch;
    }

    public boolean hasBasicIntegrity() {
        return basicIntegrity;
    }

    public boolean hasHardwareBackedEvaluationType() {
        return evaluationType.contains("HARDWARE_BACKED");
    }

    public String getAdvice() {
        return advice;
    }
}
