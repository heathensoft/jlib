plugins {
    id("io.github.heathensoft.jlib.conventions")
}

repositories {
    //mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation(project(":lwjgl"))
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