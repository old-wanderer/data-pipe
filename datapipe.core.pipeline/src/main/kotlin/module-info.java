module datapipe.core.pipeline {

    requires datapipe.core.data;

    // TODO эти модули включены в datapipe.core.data. Разобраться
    requires kotlin.stdlib;
    requires org.objectweb.asm;
    requires gson;

    exports datapipe.core.pipeline;

}