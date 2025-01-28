dependencies {
    implementation(platform("org.noear:solon-parent:${DependenciesVersion.solonVersion}"))
    implementation("org.noear:mybatis-plus-extension-solon-plugin")

    // 内部模块
    implementation(project(":aether-domain"))
}

tasks.test {
    useJUnitPlatform()
}