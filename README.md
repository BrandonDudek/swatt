# Software Automated Testing Tools (SWATT)
SWAT (pronounced \\swät\\) is a library of Tools to help Software Testers quickly and efficiently Automate their Test Suites.

## Library Table of Contents (Packages)
* Asserts
  * Contains custom Assertions that can be commonly used.
* Exceptions
  * Contains custom Runtime Exceptions used by the SWATT library.
  * All of the Exception have a common ancestor, for easy catching. 
* Selenium
  * These tools will help with testing Web Apps.
  * It is configured to work with the latest version of Chrome and Firefox version 59.0.1.
  * Has been tested on windows 7 and MacOS 10.13, but should work on similar systems.
* String
  * A Helper class to preform common and/or complex String manipulations.
  * Is **very** helpful for Whitespace manipulation and Character Sets.
* XML
  * Contains Helper classes to preform common and/or complex XML parsing, writing, querying, and modifying.

## Javadoc API
[SWATT.xyz](https://swatt.xyz/)

## Class Types
This library contains 3 basic types of classes to assist you in your Automated Testing Efforts.
* Helper Classes
  * Helper classes are meant to help you work with an existing Class, Library, or File Type
  * However, they do **not** contain all of the functionality needed. You may still need to use other classes for 
  some functionality.
* Utility (Util) Classes
  * Utility Classes will help you perform certain Functions
  * Each class will contain a related group of functions 
* Wrapper Classes
  * Wrapper Classes are also meant to help you work with an existing Class or File Type
  * It should be used as a replacement for the Class that it "wraps"
  * You will interact with these Wrapper Classes directly and not with the Classes that they "wrap"
  
## Assumptions / Technologies Used
This library is built with the assumptions that you will be using the following technologies:
* [Maven](https://maven.apache.org/)
  * For dependency resolution. 
* [Jackson (FasterXML version)](https://github.com/FasterXML/jackson)
  * For JSON and XML mapping.
* [Log4J (version 2)](https://logging.apache.org/log4j/2.x/)
  * For Logging.
* [Saxon-HE](http://saxon.sourceforge.net/)
  * For XPath querying.
* [TestNG](http://testng.org/)
  * For Unit Tests.

Usage of conflicting technologies may result in unexpected and undesired behavior.

## Logging
Every class in this library logs with the Log4J library.
And all of them follow this standard:
* Public Methods
  * Has an INFO level "[START]" log
  * Has a DEBUG level "[END]" log
  * Any other logs in the method are at the DEBUG or TRACE level
  * _Note:_ WARNING logs may also be present, but **Exceptions** will be thrown instead of logging ERRORs
* Protected / Package-Private / Private Methods
  * Has a DEBUG level "[START]" log
  * Has a TRACE level "[END]" log
  * Any other logs in the method are at the TRACE level
  * _Note:_ WARNING logs may also be present, but **Exceptions** will be thrown instead of logging ERRORs 
