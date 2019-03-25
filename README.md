# jOOQ DSL Maven Plugin

[![Build status](https://travis-ci.com/Osguima3/jooq-dsl-maven-plugin.svg?token=qjGCjX1xvY58EecSGDj1&branch=master)](https://travis-ci.com/Osguima3/jooq-dsl-maven-plugin)
[![Coverage Status](https://coveralls.io/repos/github/Osguima3/jooq-dsl-maven-plugin/badge.svg?t=b785cw)](https://coveralls.io/github/Osguima3/jooq-dsl-maven-plugin)

This plugin is an extension to [jOOQ's code generation plugin](https://www.jooq.org/doc/3.11/manual/code-generation/codegen-configuration/) that enables defining the user model ([Forced types](https://www.jooq.org/doc/3.11/manual/code-generation/codegen-advanced/codegen-config-database/codegen-database-forced-types/)) with a Type-safe .kts ([Kotlin script](https://kotlinlang.org/)) file.

## Configuration

This plugin's configuration is the same as [jOOQ's plugin](https://www.jooq.org/doc/3.11/manual/code-generation/codegen-configuration/), with the exception of the artifact id.

Here is a basic example:

```xml
<plugin>
  <groupId>io.osguima3.jooq</groupId>
  <artifactId>jooq-dsl-maven-plugin</artifactId>
  <version>1.0-SNAPSHOT</version>
  <executions>
    <execution>
      <id>generate-jooq</id>
      <goals>
        <goal>generate-jooq</goal>
      </goals>
    </execution>
  </executions>
  <configuration>
    <jdbc>
      <driver>org.postgresql.Driver</driver>
      <user>my_user</user>
      <password>my_password</password>
    </jdbc>
    <generator>
      <generate>
        <routines>false</routines>
        <pojos>false</pojos>
        <fluentSetters>true</fluentSetters>
        <javaTimeTypes>true</javaTimeTypes>
      </generate>
      <database>
        <name>org.jooq.meta.postgres.PostgresDatabase</name>
        <inputSchema>public</inputSchema>
        <includes>public.*</includes>
        <recordVersionFields>version</recordVersionFields>
      </database>
      <target>
        <packageName>org.example.project.model</packageName>
      </target>
    </generator>
  </configuration>
</plugin>
```

The main advantage over jOOQ's plugin is the way `ForcedTypes` can be defined:

```kotlin
ModelDefinition {
    tables {
        table("customer") {
            // Default field configuration
            field("id", CustomerId::class)
            field("string", CustomerName::class)
            field("registration", CustomerRegistrationTime::class)
            field("wallet_amount", CustomerWalletAmount::class)
            
            // Custom field configuration
            field("registration_status") { enum(CustomerRegistrationStatus::class, databaseType = "String") }
            field("address") { custom(converter = CustomerAddressConverter::class) }
        }
    }
}
```

As a Kotlin script, this definition is type-safe. The plugin also provides default implementations for types like `java.time.Instant` or tiny types (POJOs with a single field), popularly used in Domain-driven design.

It also simplifies the definition of custom converters using type reification to infer the `fromType` and `toType` fields required in jOOQ's [`Converter`](http://www.jooq.org/javadoc/3.11.10/org/jooq/Converter.html).

## Using jOOQ against test containers

This plugin is also integrated with [Test containers](https://www.testcontainers.org/), which starts a small docker container with the needed database implementation, which can run [migration scripts](https://flywaydb.org/documentation/migrations) before executing jOOQ's generator.

This is a sample setup for postgres:

```xml
<plugin>
  <!-- ... -->
  <dependencies>
    <dependency>
      <groupId>org.testcontainers</groupId>
      <artifactId>postgresql</artifactId>
      <version>1.10.6</version>
    </dependency>
  </dependencies>
  <configuration>
    <container>
      <provider>org.testcontainers.containers.PostgreSQLContainerProvider</provider>
      <version>10.3</version>
      <migrationPath>src/main/resources/db/migration</migrationPath>
    </container>
    <!-- ... -->
  </configuration>
</plugin>
```

## License

This project is licensed under the [Apache License](https://www.apache.org/licenses/LICENSE-2.0)
