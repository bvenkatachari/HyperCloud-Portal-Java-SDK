# DCHQ-SDK
Java SDK simplifies using DCHQ REST API's. 

This is a community driven project, feel free to submit pull requests.

You can interact with live REST API here:
```
https://dchq.readme.io/
```

# build SDK (requires JDK7+, Gradle)
gradle build

# build without test
gradle build -x test


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
