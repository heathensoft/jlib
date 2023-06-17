plugins {
    id("io.github.heathensoft.jlib.conventions")
}

repositories {
    //mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation(project(":ai"))
    implementation(project(":tiles"))
    implementation(project(":lwjgl"))
    implementation(files("../libs/json/json-simple-1.1.1.jar"))
    implementation(files("../libs/lwjgl-3.3.1/lwjgl.jar"))
    implementation(files("../libs/lwjgl-3.3.1/lwjgl-glfw.jar"))
    implementation(files("../libs/lwjgl-3.3.1/lwjgl-opengl.jar"))
    implementation(files("../libs/lwjgl-3.3.1/lwjgl-stb.jar"))
    implementation(files("../libs/tinylog-2.5.0/tinylog-api-2.5.0.jar"))
    implementation(files("../libs/tinylog-2.5.0/tinylog-impl-2.5.0.jar"))
    implementation(files("../libs/joml/joml-1.10.4/joml-1.10.4.jar"))
    implementation(files("../libs/joml/joml-primitives-1.10.0/joml-primitives-1.10.0.jar"))
    runtimeOnly(files("../libs/lwjgl-3.3.1/natives/win-x64/lwjgl-natives-windows.jar"))
    runtimeOnly(files("../libs/lwjgl-3.3.1/natives/win-x64/lwjgl-glfw-natives-windows.jar"))
    runtimeOnly(files("../libs/lwjgl-3.3.1/natives/win-x64/lwjgl-opengl-natives-windows.jar"))
    runtimeOnly(files("../libs/lwjgl-3.3.1/natives/win-x64/lwjgl-stb-natives-windows.jar"))
}

tasks.jar {
    //manifest.attributes["Main-Class"] = "com.example.MyMainClass"
    val dependencies = configurations
        .runtimeClasspath
        .get()
        .map(::zipTree) // OR .map { zipTree(it) }
    from(dependencies)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}