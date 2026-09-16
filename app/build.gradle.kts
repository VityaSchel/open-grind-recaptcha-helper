import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

fun expandHome(path: String): String =
    if (path.startsWith("~")) System.getProperty("user.home") + path.substring(1) else path

val keystoreProperties = System.getenv("RECAPTCHA_HELPER_KEYSTORE_PROPERTIES")
    ?.takeIf { it.isNotBlank() }
    ?.let { path ->
        val f = file(expandHome(path))
        if (!f.exists()) {
            throw GradleException(
                "RECAPTCHA_HELPER_KEYSTORE_PROPERTIES points at '$path' but no file exists there. " +
                    "Create it (see contrib/keystore.properties.example) or unset the variable to build unsigned."
            )
        }
        Properties().apply { f.inputStream().use { load(it) } }
    }

android {
    namespace = "org.opengrind.recaptcha"
    compileSdk {
        version = release(36)
    }
    buildToolsVersion = "36.0.0"

    defaultConfig {
        applicationId = "org.opengrind.recaptcha"
        minSdk = 28
        targetSdk = 36
        versionCode = 1000000
        versionName = "1.0.0"
    }

    signingConfigs {
        keystoreProperties?.let { props ->
            create("release") {
                val store = file(
                    expandHome(
                        props.getProperty("storeFile")
                            ?: throw GradleException("keystore.properties is missing 'storeFile'")
                    )
                )
                if (!store.exists()) {
                    throw GradleException("storeFile '$store' from keystore.properties does not exist.")
                }
                storeFile = store
                storePassword = props.getProperty("password")
                keyAlias = props.getProperty("keyAlias")
                keyPassword = props.getProperty("password")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            keystoreProperties?.let { signingConfig = signingConfigs.getByName("release") }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.recaptcha)
    testImplementation(libs.junit)
}
