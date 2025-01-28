dependencies {
    implementation(platform("org.noear:solon-parent:${DependenciesVersion.solonVersion}"))
    implementation("org.noear:solon.validation")
    implementation("org.noear:mybatis-plus-extension-solon-plugin")
    implementation("org.projectlombok:lombok:${DependenciesVersion.lombokVersion}")
    annotationProcessor("org.projectlombok:lombok:${DependenciesVersion.lombokVersion}")

    // 内部模块
    implementation(project(":aether-common"))
}

tasks.test {
    useJUnitPlatform()
}