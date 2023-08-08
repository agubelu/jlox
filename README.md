# jlox

Java interpreter for the lox programming language, from [Crafting Interpreters](https://craftinginterpreters.com)

My implementation has a few minor syntax changes to make myself happy:
- `nil` -> `null`
- `fun` -> `fn`
- `var` -> `let`

And some practical additions, such as:
- `+=`, `-=`, `*=`, and `/=` for operation and assignment
- `%` for modulo
- `break` support inside `for` and `while` loops
- `print` as a built-in function instead of a statement