$version: "2"
namespace example.weather
use aws.protocols#restJson1
use example.weather.operations#OperationWithMixin

@restJson1
service Weather {
    version: "2006-03-01"
    operations: [OperationWithMixin]
}