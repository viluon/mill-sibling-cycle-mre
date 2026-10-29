# Cyclic reference between sibling `.mill` files

> [!WARNING]
> MRE done by Copilot, Claude Opus 5.5.

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

Mill wraps each helper file in `object <file>_`, wildcard-imports every sibling's object, and re-exports its own object at the top level. `scala/` has that shape without Mill and fails the same way:

```sh
scala-cli compile --scala 3.9.0 scala
```

It compiles once either `foo.scala` or `oof.scala` drops its `export`.

## Incremental variant

Whether a clean compile fails depends on file names. With `trait Oof` in `oof.mill`, `trait Bar extends Oof` in `bar.mill` and `trait Foo extends Bar` in `foo.mill`, the clean compile succeeds, but adding a member to `Bar` and `Foo` breaks the next incremental compile:

```
[error] bar.mill:3:19
trait Bar extends Oof {
                  ^^^
illegal trait inheritance: super<none> does not derive from trait Oof's superclass Object
```

In larger builds the same situation shows up as `Cyclic reference involving type …` or as parent types resolving to `Any`, until `./mill --meta-level 1 clean`.
