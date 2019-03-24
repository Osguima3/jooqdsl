import java.util.stream.Collectors

File generated = new File(basedir, "app/target/generated-sources/jooq/org/company/myproject/model/tables/Company.java")
assert generated.isFile()

BufferedReader reader = new BufferedReader(new FileReader(generated))
List<String> lines = reader.lines().collect(Collectors.toList())

assertLine(lines, "public final TableField<CompanyRecord, CompanyId> ID")
assertLine(lines, "public final TableField<CompanyRecord, CompanyName> NAME")
assertLine(lines, "public final TableField<CompanyRecord, CompanyCreationDate> CREATION_DATE")
assertLine(lines, "public final TableField<CompanyRecord, CompanyEmployees> EMPLOYEES")
assertLine(lines, "public final TableField<CompanyRecord, CompanyValuation> VALUATION")
assertLine(lines, "public final TableField<CompanyRecord, LegalEntityType> LEGAL_TYPE")
assertLine(lines, "public final TableField<CompanyRecord, CompanyIndustry> INDUSTRY")

reader.close()

private static void assertLine(List<String> lines, String line) {
    assert lines.stream().any { it.trim().startsWith(line) }
}
