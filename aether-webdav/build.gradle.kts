dependencies {
    api(platform(rootProject.libs.solon.parent))
    api("org.noear:solon")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
