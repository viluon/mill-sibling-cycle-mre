> [!WARNING]
> MRE done by Copilot, Claude Opus 5.5.

# Spurious cyclic reference with top-level `export` across files

```sh
scala-cli compile --scala 3.10.1 .
```

```
-- [E046] Cyclic Error: a.scala:7:32 -----------------------------------
7 |object foo_ { trait Foo extends Bar }
  |                                ^
  |                               Cyclic reference involving val <import>
```

Fails on Scala 3.3.7, 3.9.0 and 3.10.1. Compiles if any of these change:

- the files are merged into one, or their names swapped,
- either `export` is dropped,
- `Oof` stops extending `Foo`,
- `import p.bar_.*` becomes `import p.bar_.Bar` or moves into `object foo_`.

Related: scala/scala3#17201, scala/scala3#26340. Found via Mill, which wraps each `.mill` file in `object <file>_` and re-exports it the same way.
