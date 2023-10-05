plugins {
    id("io.github.heathensoft.jlib.conventions")
}

repositories { }

dependencies {
    implementation(project(":common"))
    implementation(project(":lwjgl"))
    implementation(project(":ai"))
    //runtimeOnly(files("../libs/lwjgl-3.3.1/natives/win-x64/lwjgl-natives-windows.jar"))
    //runtimeOnly(files("../libs/lwjgl-3.3.1/natives/win-x64/lwjgl-glfw-natives-windows.jar"))
    //runtimeOnly(files("../libs/lwjgl-3.3.1/natives/win-x64/lwjgl-opengl-natives-windows.jar"))
    //runtimeOnly(files("../libs/lwjgl-3.3.1/natives/win-x64/lwjgl-stb-natives-windows.jar"))
}
