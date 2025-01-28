dependencies {
    implementation(platform("org.noear:solon-parent:${DependenciesVersion.solonVersion}"))
    implementation("org.noear:solon-web")
    implementation("org.noear:sa-token-solon-plugin")
    implementation("org.noear:mybatis-plus-extension-solon-plugin")

    implementation("cn.hutool:hutool-all:${DependenciesVersion.hutoolVersion}")
    implementation("org.projectlombok:lombok:${DependenciesVersion.lombokVersion}")
    annotationProcessor("org.projectlombok:lombok:${DependenciesVersion.lombokVersion}")

    // 内部模块
    implementation(project(":aether-common"))
    implementation(project(":aether-domain"))
    implementation(project(":aether-dao"))

    testImplementation("org.noear:solon-test")
}

tasks.test {
    useJUnitPlatform()
}