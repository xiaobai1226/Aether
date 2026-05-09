dependencies {
    api(libs.mybatis.plus.solon.plugin)
    implementation(libs.mybatis.plus.jsqlparser)
    implementation(libs.hikaricp)
    implementation(libs.mysql.connector)

    // 内部模块
    implementation(project(":aether-domain"))
}