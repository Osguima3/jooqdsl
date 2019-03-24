import java.util.stream.Collectors

File generated = new File(basedir, "app/target/generated-sources/jooq/io/osguima3/jooqdsl/multimodule/app/model/tables/Test.java")
assert generated.isFile()

BufferedReader reader = new BufferedReader(new FileReader(generated))
List<String> lines = reader.lines().collect(Collectors.toList())

assertLine(lines, "public final TableField<TestRecord, TinyId> UUID")
assertLine(lines, "public final TableField<TestRecord, TinyString> STRING")
assertLine(lines, "public final TableField<TestRecord, TinyInstant> INSTANT")
assertLine(lines, "public final TableField<TestRecord, TinyInt> INT")
assertLine(lines, "public final TableField<TestRecord, TinyBigDecimal> BIG_DECIMAL")
assertLine(lines, "public final TableField<TestRecord, CustomEnum> CUSTOM_ENUM")
assertLine(lines, "public final TableField<TestRecord, StringEnum> STRING_ENUM")

reader.close()

private static void assertLine(List<String> lines, String line) {
    assert lines.stream().any { it.trim().startsWith(line) }
}
