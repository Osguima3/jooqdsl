import static java.util.stream.Collectors.toList

File generated = new File(
    basedir,
    "app/target/generated-sources/jooq/io/github/osguima3/jooqdsl/it/multimodule/app/model/tables/Test.java"
)
assert generated.isFile()

static def assertField(List<String> lines, String type, String name) {
    assert lines.any { it.startsWith("public final TableField<TestRecord, $type> $name") }
}

BufferedReader reader = new BufferedReader(new FileReader(generated))
List<String> lines = reader.lines()
        .map { it.trim() }
        .filter { it.startsWith("public final TableField") }
        .collect(toList())

assertField(lines, "IdValueObject", "UUID")
assertField(lines, "StringValueObject", "STRING")
assertField(lines, "InstantValueObject", "INSTANT")
assertField(lines, "IntValueObject", "INT")
assertField(lines, "BigDecimalValueObject", "BIG_DECIMAL")
assertField(lines, "CustomEnum", "CUSTOM_ENUM")
assertField(lines, "StringEnum", "STRING_ENUM")
assertField(lines, "DateValueObject", "VALUE_OBJECT")
assertField(lines, "Date", "CONVERTER")
assertField(lines, "String", "CUSTOM")

reader.close()
