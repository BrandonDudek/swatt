# SoftWare Automated Testing Tools (SWATT)
SWATT _(pronounced \\swät\\)_ is a library of Tools to help Software Testers quickly and efficiently Automate their Test Suites.

## Homepage
* [swatt.xyz](https://swatt.xyz)

## Javadoc
* [github.io/swatt/docs](https://brandondudek.github.io/swatt/docs/)

## Maven Repository
* [Maven Central](https://mvnrepository.com/artifact/xyz.swatt)

## Library Table of Contents / Packages
* Asserts
  * Contains custom Assertions that can be commonly used.
* Exceptions
  * Contains custom Runtime Exceptions used by the SWATT library.
  * All of the Exception have a common ancestor, for easy catching.
* Log
  * Contains the `@LogMethods` annotation for automatic logging.
  * _Note:_ You must enable the AspectJ compiler on your project
    * see: [Applying already compiled aspect JARs](https://www.mojohaus.org/aspectj-maven-plugin/examples/libraryJars.html)
* POJO
  * If your POJO Classes implement the `SqlPojo` interface, then it will make running compares on these classes much easier
  * To get IntelliJ to generate these POJOs, use the [Generate SQL POJOs](https://github.com/BrandonDudek/swatt/blob/master/scripts/Generate%20SQL%20POJOs.groovy) groovy script
* Selenium
  * These tools will help with testing Web Apps.
  * It is configured to work with the latest version of Chrome and Firefox version 59.0.1.
  * Has been tested on windows 7 and MacOS 10.13, but should work on similar systems.
  * **Sample Project:** [swatt-selenium-sample-project](https://github.com/BrandonDudek/swatt-selenium-sample-project)
* SOAP
  * A helper class to make sending SOAP messages easy.
  * **Sample Code:** see [SOAP Client Unit Tests](https://github.com/BrandonDudek/swatt/blob/master/src/test/java/xyz/swatt/tests/soap/SoapClientTests.java)
* String
  * A helper class to preform common and/or complex String manipulations.
  * Is **very** helpful for Whitespace manipulation and Character Sets.
* TestGN and Reporters
  * Contains a Matrix style report that you can copy to an Excel sheet, for reporting purposes
  * Also allows you to report know bugs that you have put workarounds in place for.
* XML
  * Contains Helper classes to preform common and/or complex XML parsing, writing, querying, and modifying.

## Selenium/Browser Version Comparability
|SWATT|Selenium|Windows|MacOS|Ubuntu|
|:---:|:---:|:---:|:---:|:---:|
|1.11.0|3.141.59|10|10.13 (High Sierra)|18.04.2|

|Chrome Driver|Chrome|
|:---:|:---:|
|83.0.4103.39|83.0.4103.97|

|Gecko Driver|Firefox|
|:---:|:---:|
|0.24.0|60-66|

|IE Driver|IE|
|:---:|:---:|
|3.141.5|11|

* For more details and known bugs, see: [swatt.xyz/selenium-driver-browser-version-compatibility](https://swatt.xyz/selenium-driver-browser-version-compatibility/)

## Class Types
This library contains 3 basic types of classes to assist you in your Automated Testing Efforts.
* **Helper Classes**
  * Helper classes are meant to help you work with an existing Class, Library, or File Type
  * However, they do **not** contain all of the functionality needed. You may still need to use other classes for 
  some functionality.
* **Utility (Util) Classes**
  * Utility Classes will help you perform certain Functions
  * Each class will contain a related group of functions 
* **Wrapper Classes**
  * Wrapper Classes are also meant to help you work with an existing Class or File Type
  * It should be used as a replacement for the Class that it "wraps"
  * You will interact with these Wrapper Classes directly and not with the Classes that they "wrap"
  
## External Libraries Used
This library is built with the assumptions that you will be using the following technologies:
* [AspectJ](https://www.eclipse.org/aspectj/)
  * For Aspect Oriented Programming (AOP) support
* [Jackson (FasterXML version)](https://github.com/FasterXML/jackson)
  * For JSON and XML mapping.
* [Log4J (version 2)](https://logging.apache.org/log4j/2.x/)
  * For Logging.
* [Saxon-HE](http://saxon.sourceforge.net/)
  * For XPath querying.
* [Spring Framework](https://spring.io/)
  * For lots of different functionality (including JDBC)
* [TestNG](http://testng.org/)
  * For Unit Tests.
  
###### (Usage of conflicting technologies may result in unexpected and undesired behavior.)

## Logging
Every class in this library logs with the Log4J library.
And all of them follow this standard:
* **Public Methods**
  * Has an INFO level "[START]" log
  * Has a DEBUG level "[END]" log
  * Any other logs in the method are at the DEBUG or TRACE level
  * _Note:_ WARNING logs may also be present, but ERRORs will only be logged if an **Exception** is being thrown or cannot be caught 
* **Protected** / **Package-Private** / **Private Methods**
  * Has a DEBUG level "[START]" log
  * Has a TRACE level "[END]" log
  * Any other logs in the method are at the TRACE level
  * _Note:_ WARNING logs may also be present, but ERRORs will only be logged if an **Exception** is being thrown or cannot be caught

## Contributing
* Feel free to report any bugs you may find or any improvement that you may want
  * (The whole point of this Library is collaboration)
* If you would like to write code to fix bugs or add functionality, we also encourage that! 😊
  * We only ask that you adhere to the following Rules.

## Coding Contribution Rules
* Set your git project user to your GitHub Username and Email address
  * At your root project directory, in Command Prompt / Terminal enter:
    * `git config user.name "YOUR_GITHUB_USER_NAME"`
    * `git config user.email "YOUR_GITHUB_EMAIL_ADDRESS"`
  * To validate you changes, enter: `git config --list`
* Do all of your changes in a new branch
* Use Merge Requests to push your changes to the **master** branch
* Follow the [Logging](#logging) standards.
* Add **full** Javadocs to all Public Classes, Methods, Enums, etc.
* Add your `@author` tag to any code section that you edited.
  * In the format: `@author Name Here (<a href="github.com/GITHUB_USERNAME">GITHUB_USERNAME</a>)`
    * i.e. `@author Brandon Dudek (<a href="github.com/BrandonDudek">BrandonDudek</a>)`
