plugins {
    id("io.github.heathensoft.jlib.conventions")
}

repositories { }

dependencies {
    implementation(project(":common"))
    implementation(project(":ai"))
    implementation(project(":tiles"))
    implementation(project(":gui"))
    implementation(project(":ui"))
    implementation(project(":lwjgl"))
    runtimeOnly(files("../libs/lwjgl-3.3.4/natives/win-x64/lwjgl-natives-windows.jar"))
    runtimeOnly(files("../libs/lwjgl-3.3.4/natives/win-x64/lwjgl-glfw-natives-windows.jar"))
    runtimeOnly(files("../libs/lwjgl-3.3.4/natives/win-x64/lwjgl-opengl-natives-windows.jar"))
    runtimeOnly(files("../libs/lwjgl-3.3.4/natives/win-x64/lwjgl-stb-natives-windows.jar"))
}
