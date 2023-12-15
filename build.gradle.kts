plugins {
    application
    kotlin("jvm") version "1.9.20"
    id("org.graalvm.buildtools.native") version "0.9.19"
    kotlin("plugin.serialization") version "1.9.21"
}

group = "cn.llonvne"
version = "0.0.1"
application {
    mainClass.set("MainKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
}

graalvmNative {
    binaries {

        named("main") {
            fallback.set(false)
            verbose.set(true)
            buildArgs.add("-H:+InstallExitHandlers")
            buildArgs.add("-H:+ReportUnsupportedElementsAtRuntime")
            buildArgs.add("-H:+ReportExceptionStackTraces")
        }

        named("test") {
            fallback.set(false)
            verbose.set(true)
            buildArgs.add("-H:+InstallExitHandlers")
            buildArgs.add("-H:+ReportUnsupportedElementsAtRuntime")
            buildArgs.add("-H:+ReportExceptionStackTraces")
        }
    }
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}