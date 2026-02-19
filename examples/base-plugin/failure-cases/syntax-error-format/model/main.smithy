namespace smithy.example

// Intentional syntax error: bare keyword with no identifier following it.
// The smithyFormat task must fail with a clear "Cannot format invalid models" message,
// not a secondary NoClassDefFoundError (regression for SMITHY-3541).
structure

