package software.amazon.smithy.producer1;

import software.amazon.smithy.model.SourceLocation;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.BooleanTrait;

public final class Producer1Trait extends BooleanTrait {
    public static final ShapeId ID = ShapeId.from("smithy.producer#producer1");

    public Producer1Trait(SourceLocation sourceLocation) {
        super(ID, sourceLocation);
    }

    public Producer1Trait() {
        this(SourceLocation.NONE);
    }

    public static final class Provider extends BooleanTrait.Provider<Producer1Trait> {
        public Provider() {
            super(ID, Producer1Trait::new);
        }
    }
}
