/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rikka.safetynetchecker.attest;

import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.json.webtoken.JsonWebSignature;

import org.apache.http.conn.ssl.StrictHostnameVerifier;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLException;

/**
 * Sample code to verify the device attestation statement offline.
 */
public class OfflineVerify {

    private static final StrictHostnameVerifier HOSTNAME_VERIFIER = new StrictHostnameVerifier();

    private static AttestationStatement parseAndVerify(String signedAttestationStatement) throws AttestationException {
        // Parse JSON Web Signature format.
        JsonWebSignature jws;
        try {
            jws = JsonWebSignature.parser(AndroidJsonFactory.getDefaultInstance())
                    .setPayloadClass(AttestationStatement.class).parse(signedAttestationStatement);
        } catch (IOException e) {
            throw new AttestationException("Not valid JWS " + "format.", e);
        }

        // Verify the signature of the JWS and retrieve the signature certificate.
        X509Certificate cert;
        try {
            cert = jws.verifySignature();
            if (cert == null) {
                throw new AttestationException("Signature verification failed.");
            }
        } catch (GeneralSecurityException e) {
            throw new AttestationException("Error during cryptographic verification of the JWS signature.", e);
        }

        // Verify the hostname of the certificate.
        if (!verifyHostname(cert)) {
            throw new AttestationException("Certificate isn't issued for the hostname attest.android.com.");
        }

        // Extract and use the payload data.
        return (AttestationStatement) jws.getPayload();
    }

    /**
     * Verifies that the certificate matches the specified hostname.
     * Uses the {@link StrictHostnameVerifier} from the Apache HttpClient library
     * to confirm that the hostname matches the certificate.
     */
    private static boolean verifyHostname(X509Certificate leafCert) {
        try {
            // Check that the hostname matches the certificate. This method throws an exception if
            // the cert could not be verified.
            HOSTNAME_VERIFIER.verify("attest.android.com", leafCert);
            return true;
        } catch (SSLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static AttestationStatement process(String signedAttestationStatement) throws AttestationException {
        return parseAndVerify(signedAttestationStatement);
    }

}
