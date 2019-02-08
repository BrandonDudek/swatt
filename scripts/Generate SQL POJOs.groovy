import com.intellij.database.model.DasTable
import com.intellij.database.util.Case
import com.intellij.database.util.DasUtil

/*
 * Available context bindings:
 *   SELECTION   Iterable<DasObject>
 *   PROJECT     project
 *   FILES       files helper
 */

packageName = "com.sample;"
typeMapping = [
  (~/(?i)int|number\([0-9]+\)/)             : "Long",
  (~/(?i)float|double|decimal|real|number/) : "BigDecimal",
  (~/(?i)datetime|timestamp/)               : "Timestamp",
  (~/(?i)date/)                             : "LocalDate",
  (~/(?i)time/)                             : "LocalTime",
  (~/(?i)/)                                 : "String"
]

FILES.chooseDirectoryAndSave("Choose directory", "Choose where to store generated files") { dir ->
  SELECTION.filter { it instanceof DasTable }.each { generate(it, dir) }
}

def generate(table, dir) {
  def className = javaName(table.getName(), true)
  def fields = calcFields(table)

  packageName = dir.getPath()
  if(packageName.contains("\\src\\main\\java\\")) {
    packageName = packageName.substring(packageName.indexOf("\\src\\main\\java\\") + 15)
  }
  else if(packageName.contains("\\src\\test\\java\\")) {
    packageName = packageName.substring(packageName.indexOf("\\src\\test\\java\\") + 15)
  }
  else if(packageName.contains(":")) {
    packageName = packageName.substring(packageName.indexOf(":") + 1)
  }
  packageName = packageName.replace('\\', '.') + ";"

  new File(dir, className + ".java").withPrintWriter { out -> generate(out, table.getName(), className, fields) }
}

def generate(out, tableName, className, fields) {

  def lowerClassName = javaName(className, false)

  out.println "package $packageName"
  out.println ""
  out.println "import xyz.swatt.pojo.SqlPojo;"
  out.println ""
  out.println "import java.math.BigDecimal;"
  out.println "import java.sql.ResultSet;"
  out.println "import java.sql.SQLException;"
  out.println "import java.sql.Timestamp;"
  out.println "import java.time.LocalDate;"
  out.println "import java.time.LocalTime;"
  out.println "import java.util.*;"
  out.println "import java.util.stream.Collectors;"
  out.println ""
  out.println "@SuppressWarnings(\"Duplicates\")"
  out.println "public class $className implements SqlPojo<$className> {"
  out.println ""
  out.println "\t//========================= Enums ========================================="
  out.println "\tpublic static enum Column {"
  fields.eachWithIndex() { it, index ->
    out.println "\t\t${it.colname}($index, \"${it.name}\"),"
  }
  out.println "\t\t;"
  out.println "\t\t/** 0-based */"
  out.println "\t\tpublic final int INDEX;"
  out.println "\t\tpublic final String NAME;"
  out.println "\t\tColumn(int _index, String _name) {"
  out.println "\t\t\tINDEX = _index;"
  out.println "\t\t\tNAME = _name;"
  out.println "\t\t}"
  out.println "\t}"
  out.println ""
  out.println "\t//========================= STATIC CONSTANTS ==============================="
  out.println "\tpublic static final String TABLE_NAME = \"$tableName\";"
  out.println ""
  out.println "\t//========================= Variables ======================================"
  fields.each() {
    if (it.annos != "") out.println "\t${it.annos}"
    out.println "\tpublic ${it.type} ${it.name}; // ${it.spec}"
  }
  out.println ""
  out.println "\t//========================= Row Mapper ====================================="
  out.println "\t@Override"
  out.println "\tpublic $className mapRow(ResultSet _rs, int _rowNum) throws SQLException {"
  out.println ""
  out.println "\t\t//------------------------ Variables -----------------------------------"
  out.println "\t\t$className $lowerClassName = new $className();"
  out.println ""
  out.println "\t\t//------------------------ Code ----------------------------------------"
  fields.eachWithIndex() { elem, index ->
    out.println "\t\t$lowerClassName.${elem.name} = _rs.getObject(${index + 1}, ${elem.type}.class);"
  }
  out.println ""
  out.println "\t\treturn $lowerClassName;"
  out.println "\t}"
  out.println ""
  out.println "\t//========================= Getters & Setters =============================="
  out.println "\t@Override"
  out.println "\tpublic Object getColumn(int _columnIndex) {"
  out.println "\t\tswitch(_columnIndex) {"
  fields.eachWithIndex() { elem, index ->
    out.println "\t\t\tcase ${index}:"
    out.println "\t\t\t\treturn ${elem.name};"
  }
  out.println "\t\t\tdefault:"
  out.println "\t\t\t\tthrow new RuntimeException(\"Unknown Column Index: \" + _columnIndex + \"!\");"
  out.println "\t\t}"
  out.println "\t}"
  fields.each() {
    out.println ""
    out.println "\tpublic ${it.type} get${it.name.capitalize()}() {"
    out.println "\t\treturn ${it.name};"
    out.println "\t}"
    out.println "\tpublic void set${it.name.capitalize()}(${it.type} _${it.name}) {"
    out.println "\t\tthis.${it.name} = _${it.name};"
    out.println "\t}"
    if(it.type == "BigDecimal") {
      out.println "\tpublic void set${it.name.capitalize()}(double _${it.name}) { // RowMapper cannot map to BigDecimal directly."
      out.println "\t\tthis.${it.name} = BigDecimal.valueOf(_${it.name});"
      out.println "\t}"
    }
  }
  out.println ""
  out.println "\t//========================= Comparators ===================================="
  out.println "\t@Override"
  out.println "\tpublic Set<String> determineDifferences(SqlPojo _otherObject, int... _ignoreColumnIndexes) {"
  out.println "\t\t"
  out.println "\t\t//------------------------ Variables -----------------------------------"
  out.println "\t\tSet<Integer> ignoreColumns = new HashSet();"
  out.println "\t\tHashSet<String> diffs = new HashSet<>();"
  out.println ""
  out.println "\t\t//------------------------ Code ----------------------------------------"
  out.println "\t\tif(_otherObject.getClass() != getClass()) {"
  out.println "\t\t\tdiffs.add(\"Given SqlPojo object is of different type! [\" + getClass() + \" != \" + _otherObject.getClass() + \"]\");"
  out.println "\t\t}"
  out.println "\t\t$className other$className = ($className) _otherObject;"
  out.println ""
  out.println "\t\tif(_ignoreColumnIndexes != null) {"
  out.println "\t\t\tignoreColumns.addAll(Arrays.stream(_ignoreColumnIndexes).boxed().collect(Collectors.toList()));"
  out.println "\t\t}"
  out.println "\t\tignoreColumns.addAll(this.differencesSkipColumns);"
  out.println "\t\tignoreColumns.addAll(other${className}.differencesSkipColumns);"
  out.println ""
  fields.eachWithIndex() { it, index ->
    if(it.type == "BigDecimal") {
      out.println "\t\tif(!ignoreColumns.contains(Column.${it.colname}.INDEX) && !(${it.name} == null || other$className.${it.name} == null ? ${it.name} == other$className.${it.name} : ${it.name}.compareTo(other$className.${it.name}) == 0)) { // BigDecimal's .equals(Object) method takes precision into account."
    }
    else {
      out.println "\t\tif(!ignoreColumns.contains(Column.${it.colname}.INDEX) && !Objects.equals(this.${it.name}, other$className.${it.name})) {"
    }
    out.println "\t\t\tdiffs.add(\"Different ${it.name}: \" + this.${it.name} + \" != \" + other$className.${it.name});"
    out.println "\t\t}"
    out.println ""
  }
  out.println "\t\treturn diffs;"
  out.println "\t}"
  out.println ""
  out.println "\t@Override"
  out.println "\tpublic boolean equals(Object _otherObject) {"
  out.println "\t\tif(this == _otherObject) { return true; }"
  out.println "\t\tif(_otherObject == null || getClass() != _otherObject.getClass()) { return false; }"
  out.println "\t\t$className other = ($className) _otherObject;"
  out.println "\t\treturn "
  fields.eachWithIndex() { it, index ->
    if(it.type == "BigDecimal") {
      out.print "\t\t\t\t(${it.name} == null || other.${it.name} == null ? ${it.name} == other.${it.name} : ${it.name}.compareTo(other.${it.name}) == 0) /* BigDecimal's .equals(Object) method takes precision into account. */"
    }
    else {
      out.print "\t\t\t\tObjects.equals(${it.name}, other.${it.name})"
    }
    if(index < fields.size() - 1) {
      out.println " &&"
    }
    else {
      out.println ";"
    }
  }
  out.println "\t}"
  out.println ""
  out.println "\t@Override"
  out.println "\tpublic String toString() {"
  out.println "\t\treturn \"${className}{\" +"
  fields.each() {
    out.println "\t\t\t\"${it.name}='\" + ${it.name} + \"', \" +"
  }
  out.println "\t\t'}';"
  out.println "\t}"
  out.println ""
  out.println "}"
}

def calcFields(table) {
  DasUtil.getColumns(table).reduce([]) { fields, col ->
    def spec = Case.LOWER.apply(col.getDataType().getSpecification())
    def typeStr = typeMapping.find { p, t -> p.matcher(spec).find() }.value
    fields += [[
                 colname : col.getName(),
                 name : javaName(col.getName(), false),
                 type : typeStr,
                 spec : spec,
                 annos: ""]]
  }
}

def javaName(str, capitalize) {
  def s = com.intellij.psi.codeStyle.NameUtil.splitNameIntoWords(str)
    .collect { Case.LOWER.apply(it).capitalize() }
    .join("")
    .replaceAll(/[^\p{javaJavaIdentifierPart}[_]]/, "_")
  capitalize || s.length() == 1? s : Case.LOWER.apply(s[0]) + s[1..-1]
}
