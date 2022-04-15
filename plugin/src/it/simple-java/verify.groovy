import static java.util.stream.Collectors.toList

File generated = new File(
    basedir,
    "target/generated-sources/jooq/io/osguima3/jooqdsl/it/simplejava/model/tables/Test.java"
)
assert generated.isFile()

static def assertField(List<String> lines, String type, String name) {
    assert lines.stream().any { it.startsWith("public final TableField<TestRecord, $type> $name") }
}

BufferedReader reader = new BufferedReader(new FileReader(generated))
List<String> lines = reader.lines()
        .map { it.trim() }
        .filter { it.startsWith("public final TableField") }
        .collect(toList())

assertField(lines, "java.util.UUID", "UUID")
assertField(lines, "String", "STRING")
assertField(lines, "Instant", "INSTANT")
assertField(lines, "Integer", "INT")
assertField(lines, "BigDecimal", "BIG_DECIMAL")
assertField(lines, "CustomEnum", "CUSTOM_ENUM")
assertField(lines, "StringEnum", "STRING_ENUM")
assertField(lines, "Date", "CUSTOM")

reader.close()
