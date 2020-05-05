namespace smithy.example

structure Baz {
  foo: String
}

@aws.auth#unsignedPayload
operation Foo {}
