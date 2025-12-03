import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    jvm()
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation("io.github.compose-fluent:fluent:v0.1.0")
            implementation("io.github.compose-fluent:fluent-icons-extended:v0.1.0")
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation("com.mayakapps.compose:window-styler:0.3.2")
            implementation("net.java.dev.jna:jna:5.13.0")
            implementation("net.java.dev.jna:jna-platform:5.13.0")
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}

compose.desktop {
    application {
        mainClass = "org.source.pack.MainKt"

        buildTypes.release.proguard {
            configurationFiles.from(
                project.file("proguard-rules.desktop.pro"),
                project.file("proguard-rules.common.pro")
            )
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.source.pack"
            packageVersion = "1.0.0"

            windows {
                iconFile.set(project.file("icons/icon.ico"))
                shortcut = true
            }
        }
    }
}
