File generated = new File(basedir, "app/target/generated-sources/jooq/org/company/myproject/model/tables/Company.java")
assert generated.isFile()

BufferedReader reader = new BufferedReader(new FileReader(generated))
works = reader.lines().any { it.trim().startsWith("public final TableField<CompanyRecord, CompanyName> NAME") }
reader.close()
assert works
