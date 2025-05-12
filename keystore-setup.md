# Keystore Setup for LoayApp

This guide explains how to set up the keystore for signing the release build of LoayApp.

## Generate a Keystore

If you don't have a keystore yet, you can generate one using the following command:

```bash
keytool -genkey -v -keystore loayapp.keystore -alias loayapp -keyalg RSA -keysize 2048 -validity 10000
```

Follow the prompts to enter your information and create a strong password.

## Configure local.properties

Add the following lines to your `local.properties` file (create it in the project root if it doesn't exist):

```properties
# Keystore configuration
storeFile=path/to/your/loayapp.keystore
storePassword=your_keystore_password
keyAlias=loayapp
keyPassword=your_key_password
```

Replace the placeholder values with your actual keystore information:
- `storeFile`: The absolute path to your keystore file
- `storePassword`: The password to your keystore
- `keyAlias`: The alias used when creating the key
- `keyPassword`: The password for the specific key

## Security Note

**IMPORTANT**: Never commit your `local.properties` file or your keystore to version control. Make sure they are included in your `.gitignore` file.

## CI/CD Configuration

For CI/CD environments, consider using environment variables instead of hardcoding values in files:

```groovy
signingConfigs {
    release {
        storeFile file(System.getenv("KEYSTORE_FILE") ?: "")
        storePassword System.getenv("KEYSTORE_PASSWORD") ?: ""
        keyAlias System.getenv("KEY_ALIAS") ?: ""
        keyPassword System.getenv("KEY_PASSWORD") ?: ""
    }
}
```

## Building the Release Version

Once your keystore is set up, you can build the release version using:

```bash
./gradlew assembleRelease
```

The signed APK will be located at `app/build/outputs/apk/release/app-release.apk` 