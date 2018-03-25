module datapipe.core.data {

    requires kotlin.stdlib;
    requires org.objectweb.asm;
    requires gson;

    exports datapipe.core.data.model;
    exports datapipe.core.data.model.metadata;
    exports datapipe.core.data.model.metadata.parser;
    exports datapipe.core.data.generator;

}