dependencies {
    implementation(libs.solon.web)
    implementation(libs.solon.satoken.plugin)

    // 内部模块
    implementation(project(":aether-common"))
    implementation(project(":aether-domain"))
    implementation(project(":aether-dao"))
}