package rikka.safetynetchecker.attest;

import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.json.webtoken.JsonWebSignature;

import java.security.GeneralSecurityException;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;

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

        var sslSession = new FakeSSLSession() {
            @Override
            public Certificate[] getPeerCertificates() {
                return new Certificate[]{cert};
            }
        };
        var verifier = HttpsURLConnection.getDefaultHostnameVerifier();
        if (!verifier.verify("attest.android.com", sslSession)) {
            throw new AttestationException("Certificate isn't issued for the hostname attest.android.com.");
        }

        return (AttestationStatement) jws.getPayload();
    }

    private static class FakeSSLSession implements SSLSession {
        public int getApplicationBufferSize() {
            throw new UnsupportedOperationException();
        }

        public String getCipherSuite() {
            throw new UnsupportedOperationException();
        }

        public long getCreationTime() {
            throw new UnsupportedOperationException();
        }

        public byte[] getId() {
            throw new UnsupportedOperationException();
        }

        public long getLastAccessedTime() {
            throw new UnsupportedOperationException();
        }

        public Certificate[] getLocalCertificates() {
            throw new UnsupportedOperationException();
        }

        public Principal getLocalPrincipal() {
            throw new UnsupportedOperationException();
        }

        public int getPacketBufferSize() {
            throw new UnsupportedOperationException();
        }

        public javax.security.cert.X509Certificate[] getPeerCertificateChain() {
            throw new UnsupportedOperationException();
        }

        public Certificate[] getPeerCertificates() {
            throw new UnsupportedOperationException();
        }

        public String getPeerHost() {
            throw new UnsupportedOperationException();
        }

        public int getPeerPort() {
            throw new UnsupportedOperationException();
        }

        public Principal getPeerPrincipal() {
            throw new UnsupportedOperationException();
        }

        public String getProtocol() {
            throw new UnsupportedOperationException();
        }

        public SSLSessionContext getSessionContext() {
            throw new UnsupportedOperationException();
        }

        public Object getValue(String name) {
            throw new UnsupportedOperationException();
        }

        public String[] getValueNames() {
            throw new UnsupportedOperationException();
        }

        public void invalidate() {
            throw new UnsupportedOperationException();
        }

        public boolean isValid() {
            throw new UnsupportedOperationException();
        }

        public void putValue(String name, Object value) {
            throw new UnsupportedOperationException();
        }

        public void removeValue(String name) {
            throw new UnsupportedOperationException();
        }
    }
}
