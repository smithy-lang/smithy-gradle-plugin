namespace smithy.example

structure Foo {
  foo: String
}

@aws.auth#unsignedPayload
operation Bar {
}
