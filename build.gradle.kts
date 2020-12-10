
val remote = "192.168.80.111"
buildscript {
    val remote = "192.168.80.111"
    repositories {
        mavenLocal()
        maven {
            setUrl("http://$remote:9091/repository/maven-public/")
        }
    }
}

plugins {
    id("org.jetbrains.intellij") version "0.3.11"
    java
    idea
}
group = "tk.okou.vertx.plugin.idea"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenLocal()
    maven {
        setUrl("http://$remote:9091/repository/maven-public/")
    }
}

intellij {
    //https://www.jetbrains.com/intellij-repository/releases/
    version = "2018.3.1"
    pluginName = "xxxPlugin"
    setPlugins("coverage")
}
