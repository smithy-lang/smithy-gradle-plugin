package software.amazon.smithy.producer2;

import software.amazon.smithy.model.SourceLocation;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.BooleanTrait;

public final class Producer2Trait extends BooleanTrait {
    public static final ShapeId ID = ShapeId.from("smithy.producer#producer2");

    public Producer2Trait(SourceLocation sourceLocation) {
        super(ID, sourceLocation);
    }

    public Producer2Trait() {
        this(SourceLocation.NONE);
    }

    public static final class Provider extends BooleanTrait.Provider<Producer2Trait> {
        public Provider() {
            super(ID, Producer2Trait::new);
        }
    }
}
