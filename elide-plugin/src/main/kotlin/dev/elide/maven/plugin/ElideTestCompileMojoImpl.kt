package dev.elide.maven.plugin

import org.apache.maven.plugin.compiler.AbstractCompilerMojo
import org.apache.maven.plugin.compiler.CompilerMojo
import org.apache.maven.plugin.compiler.TestCompilerMojo

/** @author Lauri Heino <datafox> */
open class ElideTestCompileMojoImpl : TestCompilerMojo() {
    init {
        AbstractCompilerMojo::class.java.getDeclaredField("compilerId").apply {
            isAccessible = true
            set(this@ElideTestCompileMojoImpl, "elide")
        }
    }
}
