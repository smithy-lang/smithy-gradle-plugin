$version: "2.0"

namespace smithy.example

use smithy.example.internal#InternalStructure

structure Baz {
    foo: String
    bar: InternalStructure
}

@aws.auth#unsignedPayload
operation Foo {}
