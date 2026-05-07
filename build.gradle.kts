plugins {
    java
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")

    group = "com.xiaobai1226"
    val appVersion by extra("0.12.0")
    version = appVersion
    description = "家庭网盘项目"
    java.sourceCompatibility = JavaVersion.VERSION_21

    dependencies {
        // 使用 Version Catalog 管理依赖版本
        implementation(platform(rootProject.libs.solon.parent))
        implementation(platform(rootProject.libs.mybatis.plus.bom))

        implementation(rootProject.libs.hutool.all)
        compileOnly(rootProject.libs.lombok)
        annotationProcessor(rootProject.libs.lombok)
    }

    // tasks.withType<JavaCompile> {
    //     options.encoding = "UTF-8"
    // }
}

allprojects {
    repositories {
        mavenCentral()
    }
}