group = "app.chiggi.sonyliv"

patches {
    about {
        name = "Chiggi Patches"
        description = "Patches for SonyLIV (Android TV), Nutrilio, Threads, Arrow Puzzle and CrazyGames"
        source = "git@github.com:durgesh0505/chiggi_morphe_patches.git"
        author = "durgesh0505"
        contact = "na"
        website = "na"
        license = "GNU General Public License v3.0, with additional GPL section 7 requirements"
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

// Separate configuration so gson is available at runtime for the
// generatePatchesList task but never bundled into the APK.
val patchListGeneratorClasspath: Configuration by configurations.creating

dependencies {
    compileOnly(libs.gson)
    patchListGeneratorClasspath(libs.gson)
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"

        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
        mainClass.set("util.PatchListGeneratorKt")
    }

    // Used by gradle-semantic-release-plugin.
    publish {
        dependsOn("generatePatchesList")
    }
}