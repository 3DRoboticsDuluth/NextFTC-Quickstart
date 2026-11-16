package com.threedrd.nextftc.config.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import java.io.ByteArrayOutputStream
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mockito.*

@OptIn(KspExperimental::class)
class SettingsProcessorTests {
    @Test
    fun emitsOnlyAnnotatedPropertiesInSourceOrderAndOnlyOnceAcrossRounds() {
        val resolver = mock(Resolver::class.java)
        val output = ByteArrayOutputStream()
        val requests = mutableListOf<List<Any?>>()
        val generator = mock(CodeGenerator::class.java) { invocation ->
            if (invocation.method.name == "createNewFile") {
                requests += invocation.arguments.toList()
                output
            } else RETURNS_DEFAULTS.answer(invocation)
        }
        val owner = mock(KSClassDeclaration::class.java)
        val file = mock(KSFile::class.java)
        val qualified = name("example.Outer.Config")
        `when`(owner.qualifiedName).thenReturn(qualified)
        val packageName = name("example")
        `when`(owner.packageName).thenReturn(packageName)
        `when`(owner.containingFile).thenReturn(file)
        fun property(value: String) = mock(KSPropertyDeclaration::class.java).apply {
            val propertyName = name(value)
            `when`(simpleName).thenReturn(propertyName)
            `when`(parentDeclaration).thenReturn(owner)
        }
        val zebra = property("zebra")
        val alpha = property("alpha")
        val ignored = property("ignored")
        `when`(resolver.getSymbolsWithAnnotation("com.threedrd.nextftc.config.Setting"))
            .thenAnswer { sequenceOf(alpha, zebra) }
        `when`(resolver.getDeclarationsInSourceOrder(owner))
            .thenAnswer { sequenceOf(zebra, mock(KSFunctionDeclaration::class.java), ignored, alpha) }
        val environment = mock(SymbolProcessorEnvironment::class.java)
        `when`(environment.codeGenerator).thenReturn(generator)
        val processor = SettingsProcessorProvider().create(environment)
        assertTrue(processor.process(resolver).isEmpty())
        assertTrue(processor.process(resolver).isEmpty())
        assertEquals(1, requests.size)
        val request = requests.single()
        val dependencies = request.first() as Dependencies
        assertFalse(dependencies.aggregating)
        assertEquals(listOf(file), dependencies.originatingFiles)
        assertEquals(listOf("example", "Outer\$Config\$SettingOrder", "java"), request.drop(1))
        assertEquals("""
            package example;

            public final class Outer${'$'}Config${'$'}SettingOrder {
                public static final String[] names = {"zebra", "alpha"};
            }
        """.trimIndent(), output.toString("UTF-8"))
    }

    @Test
    fun noSettingsProduceNoFiles() {
        val resolver = mock(Resolver::class.java)
        val generator = mock(CodeGenerator::class.java)
        `when`(resolver.getSymbolsWithAnnotation("com.threedrd.nextftc.config.Setting")).thenReturn(emptySequence())
        assertTrue(SettingsProcessor(generator).process(resolver).isEmpty())
        verifyNoInteractions(generator)
    }

    fun name(value: String) = mock(KSName::class.java).apply { `when`(asString()).thenReturn(value) }
}
