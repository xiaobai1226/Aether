plugins {
    java
}

val appVersion by extra("0.4.0")

group = "com.xiaobai1226"
version = appVersion

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.noear:solon-parent:3.0.3"))
    implementation("org.noear:solon-web")
    implementation("org.noear:solon-boot-jetty")
    implementation("org.noear:solon-view-thymeleaf")
    implementation("org.noear:solon.web.webdav:2.9.1")
    implementation("org.noear:solon.logging.logback")
    implementation("org.noear:solon.validation")
    implementation("org.noear:mybatis-plus-extension-solon-plugin")
    implementation("org.noear:sa-token-solon-plugin")
    implementation("org.noear:redisson-solon-plugin")

    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("cn.hutool:hutool-all:5.8.25")
    implementation("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    implementation("com.mysql:mysql-connector-j:8.2.0")
    implementation("com.github.kokorin.jaffree:jaffree:2023.09.10")

    testImplementation("org.noear:solon-test")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

/**
 * 打jar包配置
 */
tasks.withType<Jar> {
    // 设置Jar包的基本名称
    archiveBaseName.set("aether")
    // 禁用自动添加版本号到Jar包名称的行为
    archiveVersion.set("")

    manifest {
        attributes.apply {
            set("Main-Class", "com.xiaobai1226.aether.core.AetherCoreApp")
        }
    }

    dependsOn(configurations.runtimeClasspath)

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map {
        if (it.isDirectory) it else zipTree(it)
    })

    from(sourceSets.main.get().output)
}

/**
 * 配置支持app.xml中读取ext的数据
 */
tasks.withType<ProcessResources> {
    filesMatching("app.yml") {
        val tokens = mutableMapOf<String, Any?>()
        project.properties.forEach { key, value ->
            if (value != null) {
                tokens[key.toString()] = value.toString()
            }
        }
        filter<org.apache.tools.ant.filters.ReplaceTokens>("tokens" to tokens)
    }
}

/**
 * 打印版本号
 */
tasks.register("printVersion") {
    doLast {
        println("$version")
    }
}
