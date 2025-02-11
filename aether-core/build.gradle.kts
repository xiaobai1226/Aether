//val appVersion by extra("0.4.1")
//
//group = "com.xiaobai1226"
//version = appVersion

dependencies {
    implementation(platform("org.noear:solon-parent:${DependenciesVersion.solonVersion}"))
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
    implementation("cn.hutool:hutool-all:${DependenciesVersion.hutoolVersion}")
    implementation("org.projectlombok:lombok:${DependenciesVersion.lombokVersion}")
    annotationProcessor("org.projectlombok:lombok:${DependenciesVersion.lombokVersion}")
    implementation("com.mysql:mysql-connector-j:8.2.0")

    // 内部模块
    implementation(project(":aether-common"))
    implementation(project(":aether-admin"))
    implementation(project(":aether-domain"))
    implementation(project(":aether-dao"))

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
