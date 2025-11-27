dependencies {
    api("com.baomidou:mybatis-plus-solon-plugin")
    implementation("com.baomidou:mybatis-plus-jsqlparser")
    implementation("com.zaxxer:HikariCP:${DependenciesVersion.hikariCPVersion}")
    implementation("com.mysql:mysql-connector-j:${DependenciesVersion.mysqlConnectorJVersion}")

    // 内部模块
    implementation(project(":aether-domain"))
}