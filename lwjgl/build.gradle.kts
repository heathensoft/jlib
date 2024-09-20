plugins {
    id("io.github.heathensoft.jlib.conventions")
}

repositories { }

dependencies {
    implementation(project(":common"))
    api(files("../libs/lwjgl-3.3.4/lwjgl.jar"))
    api(files("../libs/lwjgl-3.3.4/lwjgl-glfw.jar"))
    api(files("../libs/lwjgl-3.3.4/lwjgl-opengl.jar"))
    api(files("../libs/lwjgl-3.3.4/lwjgl-stb.jar"))
}
