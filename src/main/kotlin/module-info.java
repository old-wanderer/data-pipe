module datapipe.examples {

    requires datapipe.core.data;
    requires datapipe.core.pipeline;

    // TODO эти модули включены в datapipe.core.data. Разобраться
    requires kotlin.stdlib;
    requires org.objectweb.asm;
    requires gson;

    // Требует gson. TODO Тоже странная история, вроде gson использует auto-module
    requires java.sql;

}