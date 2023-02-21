$version: "2"

namespace example.weather.mixins

use example.weather.errors#ThrottlingException
use example.weather.errors#ValidationException

@mixin
operation OperationMixin {
    errors: [
        ThrottlingException
        ValidationException
    ]
}