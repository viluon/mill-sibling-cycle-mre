> [!WARNING]
> MRE done by Copilot, Claude Opus 5.5.

# Cyclic reference between sibling `.mill` files

`bar.mill`, `foo.mill` and `oof.mill` declare `trait Bar`, `trait Foo extends Bar` and `trait Oof extends Foo`.

## Reproduce

```sh
./mill resolve _
```

```
[error] foo.mill:3:19
trait Foo extends Bar
                  ^
Cyclic reference involving val <import>
```

Fails on Mill 1.3.1 and 2.0.0-M2.

## Cause

Mill wraps each helper file in `object <file>_`, wildcard-imports every sibling's object and re-exports its own at top level. `scala/` mirrors that without Mill and fails the same way:

```sh
scala-cli compile --scala 3.9.0 scala
```

It compiles once either `foo.scala` or `oof.scala` drops its `export`.

## Incremental variant

With `Oof` as base (`Bar extends Oof`, `Foo extends Bar`), clean compile succeeds, but adding a member to `Bar` and `Foo` breaks the next incremental compile:

```
[error] bar.mill:3:19
trait Bar extends Oof {
                  ^^^
illegal trait inheritance: super<none> does not derive from trait Oof's superclass Object
```

Larger builds show `Cyclic reference involving type …` or parents resolving to `Any` until `./mill --meta-level 1 clean`.
