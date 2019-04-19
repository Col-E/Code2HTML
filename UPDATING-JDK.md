## Updating for newer JDKs

Code2HTML is based off of Java 8. If you wish to run on a later version of Java the following changes will be neccesary to compile:


* [Specify JAXB as dependency](https://www.jesperdj.com/2018/09/30/jaxb-on-java-9-10-11-and-beyond/)
* Use `maven-compiler-plugin` version `3.8.0` and use `<release>{version}</release>` under `configuration`
* Use `controlsfx` version `9.0.0`
* Use `org.openjfx` dependencies _(can slap [`javafx-web`](https://mvnrepository.com/artifact/org.openjfx/javafx-web) into `dependencies`)_