# jOOQ DSL Maven Plugin

This plugin is an extension to [jOOQ's code generation plugin](https://www.jooq.org/doc/3.11/manual/code-generation/codegen-configuration/) that lets you define the user model ([Forced types](https://www.jooq.org/doc/3.11/manual/code-generation/codegen-advanced/codegen-config-database/codegen-database-forced-types/)) from a type-safe `.kts` ([Kotlin script](https://kotlinlang.org/)) file.

## Configuration

To use this plugin, just replace [jOOQ's](https://www.jooq.org/doc/3.11/manual/code-generation/codegen-configuration/) groupId, artifactId and version. You can leave all other configuration untouched.

Here is a basic example:

```xml
<plugin>
  <groupId>io.github.osguima3.jooqdsl</groupId>
  <artifactId>jooqdsl-maven-plugin</artifactId>
  <version>${jooqdsl.version}</version>
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

Additionally, you will need to add a dependency to the definition model, like so:

```xml
<dependency>
    <groupId>io.github.osguima3.jooqdsl</groupId>
    <artifactId>jooqdsl-model</artifactId>
    <version>${jooqdsl.version}</version>
</dependency>
```

The main advantage over jOOQ's plugin is the way `ForcedTypes` can be defined:

```kotlin
ModelDefinition {
    tables {
        table("customer") {
            field("id", CustomerId::class) // customer.id will be converted to CustomerId
            field("string", CustomerName::class)
            field("registration", CustomerRegistrationTime::class)
            field("wallet_amount", CustomerWalletAmount::class)

            // Has CustomerRegistrationStatus in java/kotlin, String type in the database 
            field("registration_status") { enum(CustomerRegistrationStatus::class, databaseType = "String") }
            
            // Custom field conversion
            field("address") { custom(converter = CustomerAddressConverter::class) }
        }
    }
}
```

Thanks to Kotlin's type safety, it will verify that the classes you use exist and that custom converters implement the necessary interfaces.

The plugin also provides default converter implementations for types like `java.time.Instant` or value objects (POJOs with a single field), popularly used in Domain-Driven Design.

It also provides a simplified interface to define custom converters, using type reification to infer the `fromType` and `toType` fields required in jOOQ's [`Converter`](http://www.jooq.org/javadoc/3.11.10/org/jooq/Converter.html) so you don't need to add them.

## Using jOOQ with test containers

This plugin also integrates with [Test containers](https://www.testcontainers.org/), which starts a small docker container with the needed database implementation, which can run [migration scripts](https://flywaydb.org/documentation/migrations) before executing jOOQ's generator.

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
