dependencies {
    implementation("cn.hutool:hutool-all:${DependenciesVersion.hutoolVersion}")

    implementation("org.projectlombok:lombok:${DependenciesVersion.lombokVersion}")
    annotationProcessor("org.projectlombok:lombok:${DependenciesVersion.lombokVersion}")
}

tasks.test {
    useJUnitPlatform()
}