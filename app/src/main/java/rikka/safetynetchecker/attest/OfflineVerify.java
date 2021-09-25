package rikka.safetynetchecker.attest;

import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.json.webtoken.JsonWebSignature;

import org.apache.http.conn.ssl.StrictHostnameVerifier;

import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLException;

public class OfflineVerify {
    public static AttestationStatement process(String signedAttestationStatement) throws AttestationException {
        JsonWebSignature jws;
        try {
            jws = JsonWebSignature
                    .parser(AndroidJsonFactory.getDefaultInstance())
                    .setPayloadClass(AttestationStatement.class)
                    .parse(signedAttestationStatement);
        } catch (Exception e) {
            throw new AttestationException("Not valid JWS format.", e);
        }

        X509Certificate cert;
        try {
            cert = jws.verifySignature();
            if (cert == null) {
                throw new AttestationException("Signature verification failed.");
            }
        } catch (GeneralSecurityException e) {
            throw new AttestationException("Error during cryptographic verification of the JWS signature.", e);
        }

        try {
            new StrictHostnameVerifier().verify("attest.android.com", cert);
        } catch (SSLException e) {
            throw new AttestationException("Certificate isn't issued for the hostname attest.android.com.", e);
        }

        return (AttestationStatement) jws.getPayload();
    }
}
