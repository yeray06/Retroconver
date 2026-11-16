import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

// Datos del servidor Supabase (E4-03.1, #114). Salen de local.properties, que Git no sube,
// o de variables de entorno con el mismo nombre. Sin ellos la app compila igual, pero no
// conecta con el servidor. Cómo rellenarlos: supabase/README.md, «Conectar la app».
val propiedadesLocales = Properties().apply {
    val fichero = rootProject.file("local.properties")
    if (fichero.isFile) fichero.inputStream().use { load(it) }
}

fun datoDelServidor(nombre: String): String =
    (propiedadesLocales.getProperty(nombre) ?: System.getenv(nombre) ?: "").trim()

// Firma del APK de la Release (EF-01.1, #83). La clave nunca se sube al repositorio: en la CI
// llega por los secretos de GitHub (.github/workflows/release.yml) y en un ordenador, por
// local.properties. Sin ella, el APK de release sale sin firmar. Cómo crearla: README, «Publicar una versión».
val ficheroDeFirma = datoDelServidor("FIRMA_FICHERO").takeIf { it.isNotEmpty() }?.let { rootProject.file(it) }

android {
    namespace = "com.retroconver"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.retroconver"
        // Android 8.0: java.time incluido para manejar fechas sin librerías extra (ver docs/arquitectura.md)
        minSdk = 26
        targetSdk = 36
        // Se sube en cada Release: versionCode en 1 y versionName con el número de la etiqueta (v1.0 → "1.0")
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SUPABASE_URL", "\"${datoDelServidor("SUPABASE_URL")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${datoDelServidor("SUPABASE_ANON_KEY")}\"")
    }

    signingConfigs {
        if (ficheroDeFirma?.isFile == true) {
            create("release") {
                storeFile = ficheroDeFirma
                storePassword = datoDelServidor("FIRMA_CLAVE_ALMACEN")
                keyAlias = datoDelServidor("FIRMA_ALIAS")
                keyPassword = datoDelServidor("FIRMA_CLAVE")
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Android lint en la CI (proyecto limpio para la entrega final). Las versiones de las librerías
    // se eligen a propósito (docs/arquitectura.md), así que no avisa de que hay otras más nuevas
    lint {
        abortOnError = true
        checkDependencies = false
        disable += setOf("GradleDependency", "NewerVersionAvailable", "AndroidGradlePluginVersion", "OldTargetApi")
        xmlReport = true
        textReport = true
    }

    buildFeatures {
        compose = true
        // Para leer SUPABASE_URL y SUPABASE_ANON_KEY desde el código
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

// Room guarda aquí el esquema de cada versión de la base de datos, para poder revisar los cambios
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // Base de Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // ViewModel y ciclo de vida
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Navegación
    implementation(libs.androidx.navigation.compose)

    // Persistencia
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)

    // Red: llamadas a Supabase
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)

    // Tareas en segundo plano: subida de fotos que sigue aunque se salga del álbum
    implementation(libs.androidx.work.runtime.ktx)

    // Imágenes
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Pruebas
    testImplementation(libs.junit)
    testImplementation(libs.okhttp.mockwebserver)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4.accessibility)
    androidTestImplementation(libs.androidx.room.testing)

    // Solo en depuración: vistas previas y pruebas de Compose
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
