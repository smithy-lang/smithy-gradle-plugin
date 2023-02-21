$version: "2"

namespace example.weather.errors

@error("server")
structure ThrottlingException {

}

@error("client")
structure ValidationException{

}
