package dev.elide.maven.plugin

import org.apache.maven.plugin.compiler.AbstractCompilerMojo
import org.apache.maven.plugin.compiler.CompilerMojo

/** @author Lauri Heino <datafox> */
open class ElideCompileMojoImpl : CompilerMojo() {
    init {
        AbstractCompilerMojo::class.java.getDeclaredField("compilerId").apply {
            isAccessible = true
            set(this@ElideCompileMojoImpl, "elide")
        }
    }
}
