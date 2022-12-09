# Code2HTML

A JavaFX app for converting user specified languages to HTML.

**Requirements**

- Versions `4.X.X` and beyond require Java 17 to run _(JavaFX bundled)_
- Versions `3.X.X` and below require Java 8 to run _(JavaFX not bundled)_

## Screenshots

* ![Main View](ss-html.png)
* ![Config View](ss-config.png)

## Download

See the [releases](https://github.com/Col-E/Code2HTML/releases) page for the latest build. Or compile with maven via `mvn package`

## Libraries used:

* [Apache Commons Text](https://commons.apache.org/proper/commons-text/) - HTML escaping
* [Florian Ingerl's Regex](https://github.com/florianingerl/com.florianingerl.util.regex) - Drop in `java.util.regex` replacement that allows for `(?N)` recursion _(Also GPLv2)_
* [JavaFX](https://openjfx.io/) - UI
* [picocli](https://picocli.info/) - CLI