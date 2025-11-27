dependencies {
    implementation("org.noear:solon-web")
    implementation("org.noear:sa-token-solon-plugin")

    // 内部模块
    implementation(project(":aether-common"))
    implementation(project(":aether-domain"))
    implementation(project(":aether-dao"))
}