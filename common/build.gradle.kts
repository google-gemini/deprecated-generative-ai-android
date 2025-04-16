/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    id("com.android.library")
    id("maven-publish")
    id("com.ncorti.ktfmt.gradle")
    id("changelog-plugin")
    id("release-plugin")
    kotlin("android")
    kotlin("plugin.serialization")
}

ktfmt {
    googleStyle()
}

android {
    namespace = "com.google.ai.client.generativeai.common"
    compileSdk = 34

    buildFeatures.buildConfig = true

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        buildConfigField("String", "VERSION_NAME", "\"${project.version.toString()}\"")
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    val ktorVersion = "2.3.2"

    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("org.slf4j:slf4j-nop:2.0.9")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.7.3")
    implementation("org.reactivestreams:reactive-streams:1.0.3")

    implementation("com.google.guava:listenablefuture:1.0")
    implementation("androidx.concurrent:concurrent-futures:1.2.0-alpha02")
    implementation("androidx.concurrent:concurrent-futures-ktx:1.2.0-alpha02")

    testImplementation("junit:junit:4.13.2")
    testImplementation("io.kotest:kotest-assertions-core:4.0.7")
    testImplementation("io.kotest:kotest-assertions-jvm:4.0.7")
    testImplementation("io.kotest:kotest-assertions-json:4.0.7")
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.google.ai.client.generativeai"
            artifactId = "common"
            version = project.version.toString()
            pom {
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
            }
            afterEvaluate {
                from(components["release"])
            }
        }
    }
    repositories {
        maven {
            url = uri("${projectDir}/m2")
        }
    }
}
