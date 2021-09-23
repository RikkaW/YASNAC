package rikka.safetynetchecker.attest;

import androidx.annotation.Nullable;

import com.google.api.client.json.webtoken.JsonWebSignature;
import com.google.api.client.util.Key;
import com.google.common.io.BaseEncoding;

import java.util.ArrayList;
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

    public byte[] getNonce() {
        return BaseEncoding.base64().decode(nonce);
    }

    public long getTimestampMs() {
        return timestampMs;
    }

    public String getApkPackageName() {
        return String.valueOf(apkPackageName);
    }

    public byte[] getApkDigestSha256() {
        if (apkDigestSha256 == null) return null;
        return BaseEncoding.base64().decode(apkDigestSha256);
    }

    public List<byte[]> getApkCertificateDigestSha256() {
        List<byte[]> certs = new ArrayList<>(apkCertificateDigestSha256.length);
        for (String s : apkCertificateDigestSha256) {
            certs.add(BaseEncoding.base64().decode(s));
        }
        return certs;
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
}
