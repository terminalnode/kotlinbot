import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.50"
}

group = "xyz.kazuthecat.kotlinbot"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    compile("com.jagrosh:jda-utilities:3.0.2")
    compile("net.dv8tion:JDA:4.0.0_46")
    runtime("ch.qos.logback:logback-classic:1.2.3")
    runtime("mysql:mysql-connector-java:5.1.37")
    implementation("com.google.code.gson:gson:2.8.5")
    implementation(kotlin("stdlib-jdk8"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}