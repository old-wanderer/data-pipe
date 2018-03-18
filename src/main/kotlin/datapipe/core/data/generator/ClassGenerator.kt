package datapipe.core.data.generator

import datapipe.core.data.model.metadata.*
import com.google.gson.annotations.SerializedName
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

/**
 * @author: andrei shlykov
 * @since: 21.01.2018
 */
object ClassGenerator {

    val cache = mutableMapOf<String, Class<*>>()

    private val GENERATED_CLASS_FULL_NAME = GeneratedClass::class.java.canonicalName.replace(".", "/")
    private val loader = AutoGeneratedClassLoader()

    private var autoNameCounter = 0

    fun generateClass(metadata: MetadataClass): Class<*> {

        val classWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES)
        val className = genClassName()

        classWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC,
                "abm/core/data/generated/$className", null,
                GENERATED_CLASS_FULL_NAME, null)

        val defaultConstructor = classWriter.visitMethod(Opcodes.ACC_PUBLIC,
                "<init>", "()V", null, null)
        defaultConstructor.visitCode()
        defaultConstructor.visitVarInsn(Opcodes.ALOAD, 0)
        defaultConstructor.visitMethodInsn(Opcodes.INVOKESPECIAL,
                GENERATED_CLASS_FULL_NAME, "<init>", "()V", false)
        defaultConstructor.visitInsn(Opcodes.RETURN)
        defaultConstructor.visitMaxs(1, 1)
        defaultConstructor.visitEnd()

        for (property in metadata.properties) {
            genProperty(property, classWriter)
        }

        classWriter.visitEnd()

        val clazz = loader.loadNewClass("abm.core.data.generated.$className", classWriter.toByteArray())
        cache["abm/core/data/generated/$className"] = clazz
        return clazz
    }

    private fun genProperty(propertyMetadata: PropertyMetadata, classWriter: ClassWriter) {
        val field = when (propertyMetadata.type) {
            is MetadataPrimitive -> classWriter.visitField(Opcodes.ACC_PUBLIC,
                    propertyMetadata.name, getRealType(propertyMetadata.type, maybePrimitive = true), null, null)
            is MetadataList -> classWriter.visitField(Opcodes.ACC_PUBLIC,
                    propertyMetadata.name, Type.getDescriptor(List::class.java), getRealType(propertyMetadata.type), null)
            is MetadataClass -> {
                val clazz = generateClass(propertyMetadata.type)
                classWriter.visitField(Opcodes.ACC_PUBLIC, propertyMetadata.name,
                        Type.getDescriptor(clazz), null, null)
            }
            else -> {
                println("WARNING: cant create property not primitive type: ${propertyMetadata.type}")
                return
            }
        }
        if (propertyMetadata.aliasNames.isNotEmpty()) {
            val annotation = field.visitAnnotation(Type.getDescriptor(SerializedName::class.java), true)
            annotation.visit("value", propertyMetadata.aliasNames.first())
            if (propertyMetadata.aliasNames.size > 1) {
                annotation.visit("alternate", propertyMetadata.aliasNames.drop(1).toTypedArray())
            }
            annotation.visitEnd()
        }
        field.visitEnd()
    }

    private fun getRealType(metadata: Metadata, maybePrimitive: Boolean = false): String = when (metadata) {
        PrimitiveString -> Type.getDescriptor(String::class.java)
        PrimitiveDouble -> if (maybePrimitive) Type.DOUBLE_TYPE.descriptor  else "Ljava/lang/Double;"
        PrimitiveBoolean -> if (maybePrimitive) Type.BOOLEAN_TYPE.descriptor else "Ljava/lang/Boolean;"
        PrimitiveLong -> if (maybePrimitive) Type.LONG_TYPE.descriptor    else "Ljava/lang/Long;"
        is MetadataClass -> Type.getDescriptor(generateClass(metadata))
        is MetadataList -> "Ljava/util/List<${getRealType(metadata.containsType)}>;"
        else -> throw RuntimeException("Don't know type: $metadata")
    }

    private fun genClassName() = "AutoGeneratedClass${autoNameCounter++}"

}