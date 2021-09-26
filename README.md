# YASNAC

YASNAC (short for **Y**et **A**nother **S**afety**N**et **A**ttestation **C**hecker) is an Android app that demonstrates SafetyNet Attestation API.

YASNAC is written with [Jetpack Compose](https://developer.android.com/jetpack/compose).

## Introduction

SafetyNet is developed by Google, it provides a set of services and APIs.
SafetyNet Attestation API provides a cryptographically-signed attestation, assessing the device's integrity. The app developer can use SafetyNet Attestation API to check if the device is an emulator, bootloader unlocked, system integrity compromised (root for example), etc. Read [the document from Google](https://developer.android.com/training/safetynet/attestation) for more.

This app uses SafetyNet Attestation API and displays the result.

In the production environment, the response of the SafetyNet Attestation API should be transfer to a remote server for verification. As a simple demonstration project, it is impractical to provide a server, so the verification step runs locally.

## Download

[GitHub release](https://github.com/RikkaW/YASNAC/releases/latest)

## Build

1. Obtain an API key by [following the guide from Google](https://developer.android.com/training/safetynet/attestation#obtain-api-key)
2. Write the key to `local.properties` in the form of `apiKey=YOUR_API_KEY`
3. Build with Android Studio or command line `gradlew :app:aR`

## Something else

Android's [Key Attestation API](https://developer.android.com/training/articles/security-key-attestation) is used by SafetyNet to check if the device is unlocked.

[Key Attestation (vvb2060/KeyAttestation)](https://github.com/vvb2060/KeyAttestation) is another demonstrate app for the Key Attestation API.

