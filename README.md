# Java2HTML

JavaFX app for converting java to HTML. Paste source code into the top-left panel and the HTML will show in the top-right, with a preview at the bottom.
The CSS and _optional JS_ are available in the other tabs in the top-right.

### Top-Left tabs:

* **HTML**: The HTML output.
* **CSS**: The CSS code that styles the HTML span tags.
* **JS**: Optional JS for manual inclusion of collapse-sections.
* **Configuration**: List of Regex groups. If you want to add your own expressions, the option is there. Doing so will automatically generate a CSS entry for your expression's identifying name.

While the JS is not necessary it allows you to make portions of the code collapseable. For examples see [here](https://col-e.github.io/Recaf/plugins-ex-mwscan.html). 

## Download

See the [releases](https://github.com/Col-E/Java2HTML/releases) page for the latest build. Or compile with maven via `mvn package`

## Screenshots

* ![Main View](ss-html.png)
* ![Config View](ss-config.png)

## Libraries used:

* [Apache Commons IO](https://commons.apache.org/proper/commons-io/)
* [Apache Commons Text](https://commons.apache.org/proper/commons-text/)
* [ControlsFX](https://github.com/controlsfx/controlsfx)
* [JRegex](http://jregex.sourceforge.net/)