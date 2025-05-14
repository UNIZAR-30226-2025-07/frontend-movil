import com.google.protobuf.gradle.*
import org.gradle.kotlin.dsl.create

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlinx-serialization")
    id("com.google.protobuf")
}



android {
    namespace = "eina.unizar.frontend_movil"
    compileSdk = 34

    defaultConfig {
        applicationId = "eina.unizar.frontend_movil"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    flavorDimensions.add("server")
    productFlavors {
        create("emu") {
            dimension = "server"
            buildConfigField("String", "SERVER_URL", "\"ws://10.0.2.2:8080/ws\"")
        }
        create("device") {
            dimension = "server"
            buildConfigField("String", "SERVER_URL", "\"ws://192.168.100.63:8080/ws\"")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.1"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.56.1"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                create("grpc")
            }
            it.builtins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}

// Ensure proto generation runs before Kotlin compilation for each variant
/*afterEvaluate {
    android.applicationVariants.all { variant ->
        def variantName = variant.name.capitalize()
        def compileTask = tasks.named("compile${variantName}Kotlin")
        def protoTask   = tasks.named("generate${variantName}Proto")

        compileTask.configure {
            dependsOn(protoTask)
        }
    }
}*/

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.protolite.well.known.types)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("androidx.navigation:navigation-compose:2.5.3")
    implementation("androidx.compose.ui:ui:1.5.0") // Jetpack Compose
    implementation("androidx.compose.material:material:1.5.0") // Material Design
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.0") // Vista previa en Compose
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")  //Corrutinas
    implementation("androidx.navigation:navigation-compose:2.7.4")
    implementation("io.ktor:ktor-client-core:2.3.5")
    implementation("io.ktor:ktor-client-android:2.3.5")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.5")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.5")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp
    implementation ("com.squareup.okhttp3:okhttp:4.11.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.11.0")

    //WebSocket
    implementation("org.java-websocket:Java-WebSocket:1.5.3")

    implementation ("androidx.core:core-ktx:1.10.1")
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("com.google.android.material:material:1.9.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")

    // WebSocket para la comunicación con el servidor
    implementation ("org.java-websocket:Java-WebSocket:1.5.3")

    // Gson para el manejo de JSON
    implementation ("com.google.code.gson:gson:2.10.1")

    // Socket.IO for WebSocket communication
    implementation("io.socket:socket.io-client:2.1.0") {
        exclude(group = "org.json", module = "json")
    }
    implementation ("org.json:json:20220924")

    implementation ("com.squareup.okhttp3:okhttp:4.11.0")

    // Coroutines para operaciones asíncronas
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    // Protobuf y gRPC
    implementation ("com.google.protobuf:protobuf-javalite:3.25.1")
    implementation ("com.google.protobuf:protobuf-kotlin-lite:3.25.1")
    implementation ("io.grpc:grpc-protobuf-lite:1.56.1")
    implementation ("io.grpc:grpc-stub:1.56.1")
    implementation ("io.grpc:grpc-okhttp:1.56.1") // Para Android


    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.1.5")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")

}