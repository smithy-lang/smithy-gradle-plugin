package software.amazon.smithy.producer1;

import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.AnnotationTrait;

public final class Producer1Trait extends AnnotationTrait {
    public static final ShapeId ID = ShapeId.from("smithy.producer#producer1");

    public Producer1Trait(ObjectNode objectNode) {
        super(ID, objectNode);
    }

    public Producer1Trait() {
        this(Node.objectNode());
    }

    public static final class Provider extends AnnotationTrait.Provider<Producer1Trait> {
        public Provider() {
            super(ID, Producer1Trait::new);
        }
    }
}
