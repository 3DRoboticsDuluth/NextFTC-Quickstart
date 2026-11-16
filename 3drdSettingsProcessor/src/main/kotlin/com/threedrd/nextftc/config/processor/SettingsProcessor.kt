package com.threedrd.nextftc.config.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*

class SettingsProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) = SettingsProcessor(environment.codeGenerator)
}

class SettingsProcessor(val codeGenerator: CodeGenerator) : SymbolProcessor {
    val generated = mutableSetOf<String>()

    @OptIn(KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val properties = resolver.getSymbolsWithAnnotation("com.threedrd.nextftc.config.Setting")
            .filterIsInstance<KSPropertyDeclaration>().toList()
        properties.groupBy { it.parentDeclaration as KSClassDeclaration }.forEach { (owner, settings) ->
            val name = owner.qualifiedName!!.asString()
            if (generated.add(name)) {
                val packageName = owner.packageName.asString()
                val className = name.removePrefix("$packageName.").replace('.', '$') + "$" + "SettingOrder"
                val names = settings.map { it.simpleName.asString() }.toSet()
                val ordered = resolver.getDeclarationsInSourceOrder(owner)
                    .filterIsInstance<KSPropertyDeclaration>()
                    .map { it.simpleName.asString() }.filter { it in names }.toList()
                codeGenerator.createNewFile(Dependencies(false, owner.containingFile!!), packageName, className, "java")
                    .bufferedWriter().use { writer ->
                        writer.write("""
                            package $packageName;

                            public final class $className {
                                public static final String[] names = {${ordered.joinToString { "\"$it\"" }}};
                            }
                        """.trimIndent())
                    }
            }
        }
        return emptyList()
    }
}
