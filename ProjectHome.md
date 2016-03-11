# SLF4Fx #
is the opensource framework that allows to integrate Flex logging API on client side with many java logging frameworks on server side.

The idea behind is simple. On client side we have LoggingTarget that sends all log records to slf4fx server written on java. The server utilizes [Simple Logging Facade for Java (SLF4J)](http://www.slf4j.org) for routing all incoming records to supported java logging framework. The list of supported frameworks includes all well-known logging frameworks. They provide many ways for further log manipulations - filtering, saving to file, writing to console, sending to email ant etc. There are also many UI and text based tools for log analysis that works with those logging frameworks.

SLF4Fx uses very simple but efficient binary protocol to transfer log records between flex client and java server.
The framework also provides ability to collect log records from particular client application instance that could be useful on remote support.

In development you can use provided simple sl4fx server or you can integrate the slf4fx server bean into your own java application with just few lines of code.

See [Integration](Integration.md) page for futher details or you can start using SLF4Fx with FourSimpleStepsToStart.

# Status #
Current stable version is 1.12 (no fixes, no improvements, maven repository were added)

# Maven #
You can use following repository in your pom. See [Maven](Maven.md) for details.
```
<repositories>
    ...
    <repository>
        <id>slf4fx-maven-repo</id>
        <name>slf4fx maven repository</name>
        <url>http://slf4fx.googlecode.com/svn/repo</url>
    </repository>
    ...
</repositories>
```

# Thanks #
I used the marvelous _Apache Mina framework_ for design server. Thank you guys!

_SLF4J_ is very simple in use and provides freedom to choose any (known by me :) ) java logging framework.

_flex-mojos_ is great maven plugin that shift RIA development based on Flex3 to enterprise level.