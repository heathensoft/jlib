plugins {
    id("io.github.heathensoft.jlib.conventions")
}


repositories {
    //mavenCentral()
}

dependencies {
    implementation(project(":common"))
    api(files("../libs/lwjgl-3.3.1/lwjgl.jar"))
    api(files("../libs/lwjgl-3.3.1/lwjgl-glfw.jar"))
    api(files("../libs/lwjgl-3.3.1/lwjgl-opengl.jar"))
    api(files("../libs/lwjgl-3.3.1/lwjgl-stb.jar"))
}
