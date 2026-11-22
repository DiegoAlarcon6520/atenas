import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    // Kover: mide la cobertura de las pruebas unitarias y genera el reporte HTML.
    alias(libs.plugins.kover)
}

android {
    namespace = "cl.duoc.atenas"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "cl.duoc.atenas"
        minSdk = 24
        targetSdk = 36
        // Cada APK publicado debe subir versionCode (entero, lo usa Android para
        // decidir si es una actualización). versionName es el texto que ve el usuario.
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Firma del APK release con una clave EXCLUSIVAMENTE DE DEMOSTRACIÓN.
    // Las claves no están en el repositorio: se leen de keystore.properties
    // (ignorado por Git; ver keystore.properties.example). Si ese archivo no
    // existe, el release se genera sin firmar y el resto del proyecto compila igual.
    val archivoFirma = rootProject.file("keystore.properties")
    val firma = Properties().apply {
        if (archivoFirma.exists()) archivoFirma.inputStream().use { load(it) }
    }

    signingConfigs {
        if (archivoFirma.exists()) {
            create("demo") {
                storeFile = rootProject.file(firma.getProperty("storeFile"))
                storePassword = firma.getProperty("storePassword")
                keyAlias = firma.getProperty("keyAlias")
                keyPassword = firma.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
            if (archivoFirma.exists()) {
                signingConfig = signingConfigs.getByName("demo")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

// Exporta el esquema de la base de datos a app/schemas (se versiona en Git).
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)

    // Carga y cacheo de imágenes (vista previa de las evidencias)
    implementation(libs.coil.compose)

    // Comunicación con el microservicio (atenas-backend): HTTP + JSON
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // Persistencia local: copia de los datos del servidor para trabajar sin conexión
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    // Permite probar ViewModels que usan corrutinas sin abrir el emulador.
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.room.testing)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

/*
 * COBERTURA DE PRUEBAS (Kover)
 *
 * Reporte:      gradlew.bat :app:koverHtmlReportDebug
 *               -> app/build/reports/kover/htmlDebug/index.html
 * Verificación: gradlew.bat :app:koverVerifyDebug (falla bajo 80 % de ramas)
 *
 * Se mide SOLO la lógica de negocio, con una lista de inclusión: todo lo que
 * no aparece acá queda fuera del reporte (MainActivity, Application, temas,
 * pantallas Compose, navegación, entidades, DAO generados por Room, datos
 * semilla). El detalle de qué entra y por qué está en el README.
 */
kover {
    reports {
        filters {
            includes {
                classes(
                    // Reglas de negocio, validaciones y enums con comportamiento
                    "cl.duoc.atenas.domain.*",
                    // Cálculos de fechas y control de la sesión
                    "cl.duoc.atenas.core.Fechas",
                    "cl.duoc.atenas.core.SesionManager",
                    // Repositorios: traducen filtros y coordinan escrituras
                    "cl.duoc.atenas.data.repository.*",
                    // ViewModels: estado de cada pantalla y sus decisiones
                    "cl.duoc.atenas.ui.*ViewModel",
                    "cl.duoc.atenas.ui.*ViewModel\$*"
                )
            }
            excludes {
                classes(
                    // Contenedor de datos sin lógica (solo campos).
                    "cl.duoc.atenas.data.repository.CriteriosBusqueda",
                    // Sin lógica de negocio: cambia el tema y cierra sesión.
                    "cl.duoc.atenas.ui.perfil.PerfilViewModel*",
                    // Sin lógica de negocio: guarda y borra archivos de fotos.
                    "cl.duoc.atenas.ui.evidencias.EvidenciasViewModel*"
                )
            }
        }
        verify {
            rule("Cobertura mínima de ramas en la lógica de negocio") {
                minBound(80, kotlinx.kover.gradle.plugin.dsl.CoverageUnit.BRANCH)
            }
        }
    }
}
