# LoayApp Release Checklist

Use this checklist to prepare your app for release to ensure everything is properly configured.

## Pre-Release Checks

- [ ] Update app version in `app/build.gradle.kts`
  - [ ] Increment `versionCode` (integer)
  - [ ] Update `versionName` (string) using semantic versioning (e.g., 1.0.0)

- [ ] Check all resources
  - [ ] Ensure all strings are properly localized
  - [ ] Verify images and icons have appropriate resolutions for different screen densities
  - [ ] Remove any debug or placeholder resources

- [ ] Code cleanup
  - [ ] Remove all debugging code and logs (e.g., `Log.d`, `println`)
  - [ ] Ensure no hardcoded sensitive information is present in the code

- [ ] Finalize ProGuard/R8 configuration
  - [ ] Verify `proguard-rules.pro` has appropriate rules for all libraries
  - [ ] Test the app with minification enabled to catch any issues

## Firebase Configuration

- [ ] Update Firebase configuration
  - [ ] Make sure the production Firebase project is configured
  - [ ] Ensure Firebase Analytics is properly implemented
  - [ ] Test offline capabilities with Firebase Realtime Database

## Testing

- [ ] Perform manual testing
  - [ ] Test app on multiple device sizes
  - [ ] Verify all features work as expected
  - [ ] Check app startup time and overall performance

- [ ] User-specific testing
  - [ ] Test app with real sensor data
  - [ ] Verify alerts and notifications work correctly
  - [ ] Test offline functionality

## Build Configuration

- [ ] Configure keystore for app signing
  - [ ] Generate a production keystore if not already done
  - [ ] Add keystore details to `local.properties` (DO NOT commit to version control)
  - [ ] Ensure `signingConfigs` in build.gradle is correctly set up

- [ ] Enable minification and resource shrinking
  - [ ] Set `isMinifyEnabled = true` in release build type
  - [ ] Set `isShrinkResources = true` in release build type

## Build and Verify Release

- [ ] Build a release version
  ```bash
  ./gradlew clean assembleRelease
  ```

- [ ] Test the signed APK
  - [ ] Install the release APK on a test device
  - [ ] Verify all features work with the signed release build
  - [ ] Check app performance and stability

## Google Play Store Preparation

- [ ] Prepare store listing assets
  - [ ] App screenshots (different device sizes)
  - [ ] App icon
  - [ ] Feature graphic (1024 x 500 px)
  - [ ] Promo graphic (180 x 120 px)
  - [ ] App description
  - [ ] Privacy policy URL

- [ ] Create a release on Google Play Console
  - [ ] Upload signed APK or App Bundle
  - [ ] Fill in release notes
  - [ ] Set up phased rollout if desired

## Final Verification

- [ ] Test Google Play Store installation
  - [ ] Perform an internal test track release
  - [ ] Verify installation from Play Store works
  - [ ] Check auto-updates work correctly

- [ ] Monitor initial release
  - [ ] Set up crash reporting alerts
  - [ ] Monitor Firebase Analytics for user engagement
  - [ ] Be ready to fix critical issues quickly 