$version: "2.0"

namespace io.smithy.gradle.example

@trait(
    selector: "resource"
    breakingChanges: [
        {
            change: "presence"
        }
    ]
)
structure resourceMetadata {
    /// Provides a custom name for your resource.
    @required
    description: String

    /// A type for the resource
    @required
    type: ResourceType

    /// A list of associated structures
    associated: Associated
}

@private
@idRef(failWhenMissing: true, selector: "structure")
string Associated

@private
enum ResourceType {
    NORMAL
    SPECIAL
    OTHER
    NONE
}

@tags(["no-generate"])
@trait(selector: "member")
string jsonName
