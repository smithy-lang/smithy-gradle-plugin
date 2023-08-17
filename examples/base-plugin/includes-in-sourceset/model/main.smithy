$version: "2.0"

namespace smithy.example

use com.example.included#MyIncludedStructure

structure Example {
    foo: String
    bar: MyIncludedStructure
}
