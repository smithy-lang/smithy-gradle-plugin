package software.amazon.smithy.producer2;

import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.AnnotationTrait;

public final class Producer2Trait extends AnnotationTrait {
    public static final ShapeId ID = ShapeId.from("smithy.producer#producer2");

    public Producer2Trait(ObjectNode objectNode) {
        super(ID, objectNode);
    }

    public Producer2Trait() {
        this(Node.objectNode());
    }

    public static final class Provider extends AnnotationTrait.Provider<Producer2Trait> {
        public Provider() {
            super(ID, Producer2Trait::new);
        }
    }
}
