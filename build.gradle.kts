plugins {
    java
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")

    group = "com.xiaobai1226"
    val appVersion by extra("0.8.0")
    version = appVersion
    description = "家庭网盘项目"
    java.sourceCompatibility = JavaVersion.VERSION_21

    dependencies {
        implementation(platform("org.noear:solon-parent:${DependenciesVersion.solonVersion}"))
        implementation(platform("com.baomidou:mybatis-plus-bom:${DependenciesVersion.mybatisPlusVersion}"))

        implementation("cn.hutool:hutool-all:${DependenciesVersion.hutoolVersion}")
        compileOnly("org.projectlombok:lombok:${DependenciesVersion.lombokVersion}")
        annotationProcessor("org.projectlombok:lombok:${DependenciesVersion.lombokVersion}")
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