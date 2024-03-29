plugins {
    `java-library`
    `maven-publish`
    id("io.izzel.taboolib") version "1.55"
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
    install("module-lang")

    install("platform-bukkit")

    install("expansion-command-helper")
    install("expansion-javascript")
    install("expansion-ioc")
    install("expansion-lang-tools")

    options("skip-minimize")
    classifier = null
    version = "6.0.10-118"
}

repositories {
    maven { url = uri("https://repo.dmulloy2.net/repository/public/") }
    maven { url = uri("https://nexus.phoenixdevt.fr/repository/maven-public/") }
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://mvn.lumine.io/repository/maven-public/") }
    maven { url = uri("https://mvn.lumine.io/repository/maven/") }
    mavenCentral()
}

dependencies {
    compileOnly("ink.ptms.core:v11200:11200")
    compileOnly("net.Indyuce:MMOItems:6.7.3")
    compileOnly("io.lumine:MythicLib-dist:1.4")
    compileOnly("io.lumine:MythicLib:1.4")
    compileOnly("com.google.code.gson:gson:2.9.0")
    compileOnly("com.github.LoneDev6:API-ItemsAdder:3.2.5")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.7.0")
    taboo("ink.ptms:um:1.0.0-beta-25")
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