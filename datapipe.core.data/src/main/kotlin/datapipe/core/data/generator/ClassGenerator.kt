package datapipe.core.data.generator

import datapipe.core.data.model.metadata.*
import com.google.gson.annotations.SerializedName
import datapipe.core.data.model.metadata.parser.serialize
import org.apache.logging.log4j.LogManager
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import java.io.File
import java.io.FileOutputStream

/**
 * @author: andrei shlykov
 * @since: 21.01.2018
 */
object ClassGenerator {

    private val logger = LogManager.getLogger(ClassGenerator::class.java)

    private val GENERATED_CLASS_FULL_NAME = GeneratedClass::class.java.canonicalName.replace(".", "/")
    private val loader = AutoGeneratedClassLoader()

    private var autoNameCounter = 0

    fun generateClassAndSave(metadata: MetadataClass, file: String): Class<GeneratedClass> {
        val className = genClassName()
        val byteCode = generateByteCode(className, metadata)

        val fileWriter = FileOutputStream(File(file))
        fileWriter.write(byteCode)
        fileWriter.close()

        return loadClass(className, byteCode)
    }

    fun generateClass(metadata: MetadataClass): Class<GeneratedClass> {
        val className = "datapipe/core/data/generated/${genClassName()}"
        val byteCode = generateByteCode(className, metadata)
        return loadClass(className, byteCode)
    }

    fun generateByteCode(className: String, metadata: MetadataClass): ByteArray {
        logger.info("generate byte code for class {}", className)
        logger.debug("generate byte code by metadata {}", metadata)

        val classWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES)

        classWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC,
                className, null, GENERATED_CLASS_FULL_NAME, null)

        for (property in metadata.properties) {
            genProperty(property, classWriter)
        }

        genStaticMetadataClass(metadata, classWriter, className)
        genDefaultConstructor(metadata, classWriter, className)

        classWriter.visitEnd()
        return classWriter.toByteArray()
    }

    @Suppress("UNCHECKED_CAST")
    private fun loadClass(className: String, byteCode: ByteArray): Class<GeneratedClass> =
            loader.loadNewClass(className.replace("/", "."), byteCode) as Class<GeneratedClass>

    private fun genStaticMetadataClass(metadata: MetadataClass, classWriter: ClassWriter, className: String) {
        classWriter.visitField(
                Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC + Opcodes.ACC_FINAL,
                "_metadata",
                "Ldatapipe/core/data/model/metadata/Metadata;",
                null,
                null
        ).visitEnd()


        val staticConstructor = classWriter.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null)
        staticConstructor.visitCode()
        staticConstructor.visitLdcInsn(serialize(metadata))
        staticConstructor.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "datapipe/core/data/model/metadata/parser/TokenizeKt",
                "tokenize",
                "(Ljava/lang/String;)Ljava/util/List;",
                false)
        staticConstructor.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "datapipe/core/data/model/metadata/parser/MetadataTokenKt",
                "buildMetadataAstTree",
                "(Ljava/lang/Iterable;)Ldatapipe/core/data/model/metadata/parser/MetadataAstNode;",
                false
        )
        staticConstructor.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "datapipe/core/data/model/metadata/parser/MetadataAstNodeKt",
                "buildMetadata",
                "(Ldatapipe/core/data/model/metadata/parser/MetadataAstNode;)Ldatapipe/core/data/model/metadata/Metadata;",
                false
        )
        staticConstructor.visitFieldInsn(
                Opcodes.PUTSTATIC,
                className,
                "_metadata",
                "Ldatapipe/core/data/model/metadata/Metadata;"
        )
        staticConstructor.visitInsn(Opcodes.RETURN)
        staticConstructor.visitMaxs(2, 2)
        staticConstructor.visitEnd()
    }

    private fun genDefaultConstructor(metadataClass: MetadataClass, classWriter: ClassWriter, className: String) {
        val defaultConstructor = classWriter.visitMethod(Opcodes.ACC_PUBLIC,
                "<init>", "()V", null, null)
        defaultConstructor.visitCode()
        defaultConstructor.visitVarInsn(Opcodes.ALOAD, 0)
        defaultConstructor.visitMethodInsn(Opcodes.INVOKESPECIAL,
                GENERATED_CLASS_FULL_NAME, "<init>", "()V", false)

        for (property in metadataClass.properties.filterIsInstance<MetadataPropertyDefaultValue>()) {
            defaultConstructor.visitVarInsn(Opcodes.ALOAD, 0)
            defaultConstructor.visitLdcInsn(property.defaultValue)
            defaultConstructor.visitFieldInsn(Opcodes.PUTFIELD, className, property.name,
                    getRealType(property.type, maybePrimitive = true))
        }

        for (property in metadataClass.properties.filter { it.type is MetadataClass }) {
            if (hasNestedDefaultProps(property.type as MetadataClass)) {
                val clazz = property.type.generatedClass

                defaultConstructor.visitVarInsn(Opcodes.ALOAD, 0)
                defaultConstructor.visitLdcInsn(Type.getObjectType(clazz.name.replace(".", "/")))

                defaultConstructor.visitInsn(Opcodes.ICONST_0)
                defaultConstructor.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Class")
                defaultConstructor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class",
                        "getDeclaredConstructor", "([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;", false)

                defaultConstructor.visitInsn(Opcodes.ICONST_0)
                defaultConstructor.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object")
                defaultConstructor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/reflect/Constructor",
                        "newInstance", "([Ljava/lang/Object;)Ljava/lang/Object;", false)

                defaultConstructor.visitTypeInsn(Opcodes.CHECKCAST, clazz.name.replace(".", "/"))
                defaultConstructor.visitFieldInsn(Opcodes.PUTFIELD, className, property.name, Type.getDescriptor(clazz))
            }
        }

        defaultConstructor.visitInsn(Opcodes.RETURN)
        defaultConstructor.visitMaxs(1, 1)
        defaultConstructor.visitEnd()
    }

    private fun genProperty(metadataProperty: MetadataProperty, classWriter: ClassWriter) {
        logger.debug("generate property by metadata {}", metadataProperty)
        val field = when (metadataProperty.type) {
            is MetadataPrimitive -> {
                val defaultValue = (metadataProperty as? MetadataPropertyDefaultValue)?.defaultValue
                classWriter.visitField(Opcodes.ACC_PUBLIC,
                        metadataProperty.name, getRealType(metadataProperty.type, maybePrimitive = true), null, defaultValue)
            }
            is MetadataList -> classWriter.visitField(Opcodes.ACC_PUBLIC,
                    metadataProperty.name, Type.getDescriptor(List::class.java), getRealType(metadataProperty.type), null)
            is MetadataClass -> {
                val clazz = metadataProperty.type.generatedClass
                classWriter.visitField(Opcodes.ACC_PUBLIC, metadataProperty.name,
                        Type.getDescriptor(clazz), null, null)
            }
            else -> {
                println("WARNING: cant create property not primitive type: ${metadataProperty.type}")
                return
            }
        }
        // TODO тест на генерацию SerializedName
        if (metadataProperty.aliasNames.isNotEmpty()) {
            val annotation = field.visitAnnotation(Type.getDescriptor(SerializedName::class.java), true)
            annotation.visit("value", metadataProperty.aliasNames.first())
            if (metadataProperty.aliasNames.size > 1) {
                annotation.visit("alternate", metadataProperty.aliasNames.drop(1).toTypedArray())
            }
            annotation.visitEnd()
        }
        field.visitEnd()
    }

    private fun getRealType(metadata: Metadata, maybePrimitive: Boolean = false): String = when (metadata) {
        PrimitiveString  -> Type.getDescriptor(String::class.java)
        PrimitiveDouble  -> if (maybePrimitive) Type.DOUBLE_TYPE.descriptor  else "Ljava/lang/Double;"
        PrimitiveBoolean -> if (maybePrimitive) Type.BOOLEAN_TYPE.descriptor else "Ljava/lang/Boolean;"
        PrimitiveLong    -> if (maybePrimitive) Type.LONG_TYPE.descriptor    else "Ljava/lang/Long;"
        is MetadataClass -> Type.getDescriptor(metadata.generatedClass)
        is MetadataList  -> "Ljava/util/List<${getRealType(metadata.containsType)}>;"
        else -> throw RuntimeException("Don't know type: $metadata")
    }

    private fun genClassName() = "AutoGeneratedClass${autoNameCounter++}"

    private fun hasNestedDefaultProps(metadataClass: MetadataClass): Boolean {
        if (metadataClass.properties.any { it is MetadataPropertyDefaultValue }) {
            return true
        } else {
            val nestedClasses = metadataClass.properties.map(MetadataProperty::type).filterIsInstance<MetadataClass>()
            if (nestedClasses.isEmpty()) {
                return false
            } else {
                return nestedClasses.any { hasNestedDefaultProps(it) }
            }
        }
    }

}