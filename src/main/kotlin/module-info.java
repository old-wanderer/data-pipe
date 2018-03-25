module abm.rnd {

    requires datapipe.core.data;

    // TODO эти модули включены в datapipe.core.data. Разобраться
    requires kotlin.stdlib;
    requires org.objectweb.asm;
    requires gson;

    // Требует gson. TODO Тоже странная история, вроде gson использует auto-module
    requires java.sql;

    exports datapipe.core.pipeline;
}