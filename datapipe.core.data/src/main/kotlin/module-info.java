module datapipe.core.data {

    requires kotlin.stdlib;
    requires org.objectweb.asm;
    requires gson;
    requires org.apache.logging.log4j;

    exports datapipe.core.data.model;
    exports datapipe.core.data.model.metadata;
    exports datapipe.core.data.model.metadata.dsl;
    exports datapipe.core.data.model.metadata.parser;
    exports datapipe.core.data.model.metadata.parser.visitor;
    exports datapipe.core.data.model.metadata.transformer;
    exports datapipe.core.data.model.metadata.transformer.operation;
    exports datapipe.core.data.generator;

}