$version: "2"

namespace example.weather.operations

use example.weather.mixins#OperationMixin

@http(method: "POST", uri: "/my/resource/uri/{myInputField}")
operation OperationWithMixin with [ OperationMixin ] {
    input := {
        @required
        @httpLabel
        myInputField: String,
        other: String
    }
}