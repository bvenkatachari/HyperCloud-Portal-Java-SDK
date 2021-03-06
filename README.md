<figure>
<img src="http://www.hypergrid.com/wp-content/themes/hypergrid/img/logo.png" alt="" />
</figure>



# HyperCloud Portal Java SDK
This Java SDK simplifies using HyperCloud Portal REST API's. Providers high-level java abstraction for REST API's.

# OpenSource and Community-Led
The SDK is open-source and community driven effort. If you want to contribute please reach out to us aafanah@hypergrid.com

You can interact with live REST API here:
```
https://api.hypergrid.com/
```

# build SDK (requires JDK7+, Gradle 2.14.1)
gradle build

# build without test
gradle build -x test

# build without test
gradle build --refresh-dependencies -x test

# test
gradle test

# sdk maven endpoint
## maven dependency
```
<dependency>
    <groupId>io.dchq</groupId>
    <artifactId>DCHQ-SDK</artifactId>
    <version>3.0-SNAPSHOT</version>
</dependency>
```

## gradle
# You'll add sonatype snapshot repos to pull sdk.
```
repositories {
    maven { url "http://repo.spring.io/libs-release" }
    maven { url "https://oss.sonatype.org/" }
    maven { url "https://oss.sonatype.org/content/groups/public" }
    maven { url "https://oss.sonatype.org/content/repositories/snapshots/" }
    mavenLocal()
    mavenCentral()
}
```
# Gradle dependency
```
compile("io.dchq:DCHQ-SDK:3.0-SNAPSHOT")
```
# Example Code
```
BlueprintService blueprintService = ServiceFactory.buildBlueprintService(rootUrl, username, password);
ResponseEntity<List<Blueprint>> responseEntity = blueprintService.get();
for (Blueprint bl : responseEntity.getResults()) {
    logger.info("Blueprint type [{}] name [{}] author [{}]", bl.getBlueprintType(), bl.getName(), bl.getCreatedBy());
}
```

Code examples and [JUnit and IT tests](https://github.com/intesar/DCHQ-SDK/tree/master/src/test/java/io/dchq/sdk/core)
