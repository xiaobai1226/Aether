dependencies {
    implementation("org.projectlombok:lombok:${DependenciesVersion.lombokVersion}")
    annotationProcessor("org.projectlombok:lombok:${DependenciesVersion.lombokVersion}")
}

tasks.test {
    useJUnitPlatform()
}