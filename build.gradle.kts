plugins {
    java
}

subprojects {
    apply(plugin = "java")

    group = "com.xiaobai1226"
    val appVersion by extra("0.7.0")
    version = appVersion
    description = "家庭网盘项目"
    java.sourceCompatibility = JavaVersion.VERSION_21

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}

allprojects {
    repositories {
        mavenCentral()
    }
}