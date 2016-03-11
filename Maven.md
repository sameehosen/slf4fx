# Add slf4fx-maven-repo to your project pom.xml #
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

# Add slf4fx dependency to you client project pom #
```
<dependencies>
    ...
    <dependency>
        <groupId>org.room13.slf4fx</groupId>
        <artifactId>slf4fx_server</artifactId>
        <version>1.12</version>
    </dependency>
    ...
<dependencies>
```

# Add slf4fx dependency to you server project pom #
If you want to integrate slf4fx server into your server application then add following to your project pom.xml
```
<dependencies>
    ...
    <dependency>
        <groupId>org.room13.slf4fx</groupId>
        <artifactId>slf4fx_server</artifactId>
        <version>1.12</version>
    </dependency>
    ...
<dependencies>
```