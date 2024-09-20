plugins {
    id("io.github.heathensoft.jlib.conventions")
}

repositories {
    //mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation(project(":lwjgl"))
}


tasks.sourcesJar {
    archiveBaseName.set("heathensoft-jlib")
    from(project(":common").sourceSets.main.get().allSource)
    from(project(":lwjgl").sourceSets.main.get().allSource)
    from(zipTree("../libs/tinylog-2.5.0/tinylog-api-2.5.0-sources.jar"))
    from(zipTree("../libs/tinylog-2.5.0/tinylog-impl-2.5.0-sources.jar"))
    from(zipTree("../libs/lwjgl-3.3.4/sources/lwjgl-glfw-sources.jar"))
    from(zipTree("../libs/lwjgl-3.3.4/sources/lwjgl-opengl-sources.jar"))
    from(zipTree("../libs/lwjgl-3.3.4/sources/lwjgl-sources.jar"))
    from(zipTree("../libs/lwjgl-3.3.4/sources/lwjgl-stb-sources.jar"))
    from(zipTree("../libs/joml/joml-1.10.4/joml-1.10.4-sources.jar"))
    from(zipTree("../libs/joml/joml-primitives-1.10.0/joml-primitives-1.10.0-sources.jar"))
    from(zipTree("../libs/json/json-simple-1.1.1-sources.jar"))
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.jar {
    archiveBaseName.set("heathensoft-jlib")
    dependsOn(project(":common").tasks.build)
    dependsOn(project(":lwjgl").tasks.build)
    val dependencies = configurations
        .runtimeClasspath
        .get()
        .map(::zipTree)
    from(dependencies)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    //manifest.attributes["Main-Class"] = "com.example.MyMainClass"
}