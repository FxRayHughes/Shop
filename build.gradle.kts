plugins {
    `java-library`
    `maven-publish`
    id("io.izzel.taboolib") version "1.40"
    id("org.jetbrains.kotlin.jvm") version "1.5.10"
}

taboolib {
    description {
        contributors {
            name("枫溪")
        }
    }
    install("common")
    install("common-5")
    install("module-configuration")
    install("module-chat")
    install("module-nms")
    install("module-nms-util")
    install("module-kether")
    install("module-ui")
    install("platform-bukkit")
    install("expansion-command-helper")
    install("expansion-javascript")
    classifier = null
    version = "6.0.9-68"
}

repositories {
    maven { url = uri("https://repo.dmulloy2.net/repository/public/") }
    maven { url = uri("https://jitpack.io") }
    mavenCentral()
}

dependencies {
    compileOnly("ink.ptms.core:v11200:11200")
    compileOnly("com.google.code.gson:gson:2.9.0")
    compileOnly("com.github.LoneDev6:API-ItemsAdder:3.2.3c")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.7.0")
    taboo("ink.ptms:um:1.0.0-beta-18")
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

publishing {
    repositories {
        maven {
            url = uri("https://repo.tabooproject.org/repository/releases")
            credentials {
                username = project.findProperty("taboolibUsername").toString()
                password = project.findProperty("taboolibPassword").toString()
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("library") {
            from(components["java"])
            groupId = project.group.toString()
        }
    }
}