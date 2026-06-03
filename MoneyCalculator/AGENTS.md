# Project Guidance

This is a Java 26 Maven CLI application.

The application uses:

- `org.jline:jline` for terminal input and interactive command-line behavior.
- `org.jfree:jfreechart` for chart generation.

## Build

Use Maven for normal project tasks:

```sh
mvn test
mvn package
mvn install
```

Maven is installes in this PATH

```
/Applications/Apache NetBeans.app/Contents/Resources/netbeans/java/maven/bin/mvn
```

The Maven compiler plugin is configured with `<release>26</release>` and `-Xlint`.

## Entry Point

The packaged jar manifest points to:

```text
org.fede.calculator.report.ConsoleReports
```

## Notes for Future Changes

- Preserve CLI behavior implemented with JLine unless a task explicitly asks to change it.
- Keep chart-related work aligned with the existing JFreeChart classes under `src/main/java/org/fede/calculator/chart`.
- Prefer the existing package structure and Java style when adding or modifying code.
