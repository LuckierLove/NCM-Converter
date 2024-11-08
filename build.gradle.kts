plugins {
    kotlin("jvm") version "2.0.20"
}

group = "cn.luckierlove"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.jthink:jaudiotagger:3.0.1")
    implementation("com.alibaba.fastjson2:fastjson2:2.0.53")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}