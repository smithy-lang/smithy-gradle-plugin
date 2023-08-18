$version: "2.0"

namespace example

use io.smithy.example#jsonName

structure MyStructure {
    @jsonName("TESTING")
    myMember: String
}
