plugins {
    java
}

subprojects {
    apply(plugin = "java")

    group = "com.xiaobai1226"
    val appVersion by extra("0.6.0")
    version = appVersion
    description = "家庭网盘项目"
    java.sourceCompatibility = JavaVersion.VERSION_21
}

allprojects {
    repositories {
        mavenCentral()
    }
}