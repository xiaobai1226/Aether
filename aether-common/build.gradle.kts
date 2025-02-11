dependencies {
    implementation(platform("org.noear:solon-parent:${DependenciesVersion.solonVersion}"))
    implementation("org.noear:solon.logging.logback")
    implementation("cn.hutool:hutool-all:${DependenciesVersion.hutoolVersion}")
    implementation("com.github.kokorin.jaffree:jaffree:${DependenciesVersion.jaffreeVersion}")

    implementation("org.projectlombok:lombok:${DependenciesVersion.lombokVersion}")
    annotationProcessor("org.projectlombok:lombok:${DependenciesVersion.lombokVersion}")
}

tasks.test {
    useJUnitPlatform()
}