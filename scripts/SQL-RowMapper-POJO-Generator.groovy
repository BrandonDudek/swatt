import com.intellij.database.model.DasTable
import com.intellij.database.util.Case
import com.intellij.database.util.DasUtil

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

/*
 * Available context bindings:
 *   SELECTION   Iterable<DasObject>
 *   PROJECT     project
 *   FILES       files helper
 */

packageName = "com.sample;"
typeMapping = [
        ///// Numbers /////
        // If both precision and scale are not given, in a NUMBER deffinition, oracle defaults to max for both.
        (~/(?i)int|number\s*\(\s*(([0-9]+\s*(,\s*0\s*)?)|,\s*0\s*)\)/): "Long", // NUMBER(1+), NUMBER(,0), NUMBER(1+,0)
        // TODO: Handle Whole Number with more than 18 digits. (Use BigInteger.)
        (~/(?i)float|double|decimal|real|number/)                     : "BigDecimal", // NUMBER, NUMBER(*), NUMBER(,1+), NUMBER(*,1+), NUMBER(0+,1+)

        ///// Date/Time /////
        (~/(?i)date|datetime/)                                        : "LocalDateTime", // SQL Date also contains Time in Oracle Databases.
        (~/(?i)time/)                                                 : "LocalTime",
        (~/(?i)timestamp/)                                            : "Timestamp",

        ///// Objects /////
        // https://mvnrepository.com/artifact/com.oracle.spatial/com.springsource.oracle.spatial.geometry
        (~/(?i)mdsys.sdo_geometry/)                                   : "JGeometry",

        ///// Fallback /////
        (~/(?i)/)                                                     : "String"
]

FILES.chooseDirectoryAndSave("Choose directory", "Choose where to store generated files") { dir ->
  SELECTION.filter { it instanceof DasTable }.each { generateFile(it, dir) }
}

//---------- Generate Methods ----------
def generateFile(table, dir) {
  def className = javaName(table.getName(), true)
  def fields = calcFields(table)

  packageName = dir.getPath()
  if (packageName.contains("\\src\\main\\java\\")) {
    packageName = packageName.substring(packageName.indexOf("\\src\\main\\java\\") + 15)
  } else if (packageName.contains("\\src\\test\\java\\")) {
    packageName = packageName.substring(packageName.indexOf("\\src\\test\\java\\") + 15)
  } else if (packageName.contains(":")) {
    packageName = packageName.substring(packageName.indexOf(":") + 1)
  }
  packageName = packageName.replace('\\', '.') + ";"

  new File(dir, className + ".java").withPrintWriter { out -> generateClass(out, table.getDbParent().getName(), table.getName(), className, fields) }
}

def generateClass(out, schemaName, tableName, className, fields) {

  def lowerClassName = javaName(className, false)

  out.println "package $packageName"
  out.println ""
  generateImports(out)
  out.println ""
  out.println "@Generated(value = \"xyz.swatt.SQL-RowMapper-POJO-Generator.groovy\", date = \"" + ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS) +
          "\", comments = \"Generator Version 2\")"
  out.println "@LogMethods"
  out.println "@SuppressWarnings(\"Duplicates\")"
  out.println "public class $className implements SqlPojo<$className>, Cloneable {"
  out.println ""
  out.println "\t//========================= Enums ========================================="
  out.println "\tpublic static enum Column implements SqlPojo.RowMapperColumnEnum {"
  fields.eachWithIndex() { it, index ->
    out.println "\n\t\t/**${it.type} - ${it.spec}*/"
    out.println "\t\t${it.colname}($index, \"${it.colname}\"),"
  }
  out.println "\t\t;"
  out.println "\t\t/** 0-based */"
  out.println "\t\tpublic final int COLUMN_INDEX;"
  out.println "\t\tpublic final String COLUMN_NAME;"
  out.println "\t\t"
  out.println "\t\tpublic static Column fromString(String _value) {\n" +
          "\t\t\tfor(Column enumEntry : Column.values()) {\n" +
          "\t\t\t\tif(enumEntry.COLUMN_NAME.equalsIgnoreCase(_value)) { // SQL Column Names are Case Insensitive.\n" +
          "\t\t\t\t\treturn enumEntry;\n" +
          "\t\t\t\t}\n" +
          "\t\t\t}\n" +
          "\t\t\treturn null;\n" +
          "\t\t}"
  out.println "\t\t"
  out.println "\t\tColumn(int _index, String _name) {"
  out.println "\t\t\tCOLUMN_INDEX = _index;"
  out.println "\t\t\tCOLUMN_NAME = _name;"
  out.println "\t\t}"
  out.println "\t\t"
  out.println "\t\t@Override"
  out.println "\t\tpublic String getColumnName() {"
  out.println "\t\t\treturn COLUMN_NAME;"
  out.println "\t\t}"
  out.println "\t\t"
  out.println "\t\t@Override"
  out.println "\t\tpublic int getColumnIndex() {"
  out.println "\t\t\treturn COLUMN_INDEX;"
  out.println "\t\t}"
  out.println "\t\t"
  out.println "\t\t/**"
  out.println "\t\t * @return The Column Name."
  out.println "\t\t */"
  out.println "\t\t@Override"
  out.println "\t\tpublic String toString() {"
  out.println "\t\t\treturn COLUMN_NAME;"
  out.println "\t\t}"
  out.println "\t}"
  out.println ""
  out.println "\t//========================= STATIC CONSTANTS ==============================="
  out.println "\t@SuppressWarnings(\"unused\")"
  out.println "\tprivate static final Logger LOGGER = LogManager.getLogger(${className}.class);"
  out.println "\tpublic static final String TABLE_NAME = \"$tableName\";"
  out.println "\tpublic static final String SCHEMA_NAME = \"$schemaName\";"
  out.println "\tpublic static final String FULL_TABLE_NAME = SCHEMA_NAME + \".\" + TABLE_NAME;"
  out.println ""
  out.println "\t//========================= Static Methods ==============================="
  generateStaticMethods(out, className)
  out.println ""
  out.println "\t//========================= Variables ======================================"
  fields.each() {
    if (it.annos != "") out.println "\t${it.annos}"
    out.println "\t/**\n" +
            "\t * ${it.spec}\n" +
            "\t */\n" +
            "\tpublic ${it.type} ${it.name};\n"
  }
  out.println "\t//========================= Row Mapper ====================================="
  out.println "\t@Override"
  out.println "\tpublic $className mapRow(ResultSet _rs, int _rowNum) throws SQLException {"
  out.println ""
  out.println "\t\t//------------------------ Variables -----------------------------------"
  out.println "\t\tResultSetMetaData metaData = _rs.getMetaData();"
  out.println "\t\t$className $lowerClassName = new $className();"
  out.println ""
  out.println "\t\t//------------------------ Code ----------------------------------------"
  out.println "\t\tfor(int i = 1; i <= metaData.getColumnCount(); i++) {\n" +
          "\n" +
          "\t\t\tString columnName = metaData.getColumnName(i);\n" +
          "\n" +
          "\t\t\tColumn column = Column.fromString(columnName);\n" +
          "\t\t\tif(column==null) {\n" +
          "\t\t\t\tthrow new EnumConstantNotPresentException(Column.class, columnName);\n" +
          "\t\t\t}\n" +
          "\t\t\t\n" +
          "\t\t\tswitch(column) {"
  fields.eachWithIndex() { it, index ->
    out.println "\t\t\t\tcase ${it.colname}:"
    if (it.type == "JGeometry") {
      out.println "\t\t\t\t\t$lowerClassName.${it.name} = (${it.type}) _rs.getObject(i);"
    } else {
      out.println "\t\t\t\t\t$lowerClassName.${it.name} = _rs.getObject(i, ${it.type}.class);"
    }
    out.println "\t\t\t\t\tbreak;"
  }
  out.println "\t\t\t\tdefault:\n" +
          "\t\t\t\t\tthrow new EnumConstantNotPresentException(Column.class, columnName);\n" +
          "\t\t\t}\n" +
          "\t\t}"
  out.println ""
  out.println "\t\treturn $lowerClassName;"
  out.println "\t}"
  out.println ""
  out.println "\t//========================= Getters & Setters =============================="
  out.println "\t@Override"
  out.println "\tpublic String getFullTableName() {"
  out.println "\t\treturn FULL_TABLE_NAME;"
  out.println "\t}"
  out.println ""
  out.println "\t//-------------------- Columns --------------------"
  generateGettersAndSetters(out, className, fields)
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
    if (it.type == "BigDecimal") {
      out.println "\t\tif(!ignoreColumns.contains(Column.${it.colname}.COLUMN_INDEX) && !(${it.name} == null || other$className.${it.name} == null ? ${it.name} == other$className.${it.name} : ${it.name}.compareTo(other$className.${it.name}) == 0)) { // BigDecimal's .equals(Object) method takes precision into account."
    } else {
      out.println "\t\tif(!ignoreColumns.contains(Column.${it.colname}.COLUMN_INDEX) && !Objects.equals(this.${it.name}, other$className.${it.name})) {"
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
    if (it.type == "BigDecimal") {
      out.print "\t\t\t\t(${it.name} == null || other.${it.name} == null ? ${it.name} == other.${it.name} : ${it.name}.compareTo(other.${it.name}) == 0) /* BigDecimal's .equals(Object) method takes precision into account. */"
    } else {
      out.print "\t\t\t\tObjects.equals(${it.name}, other.${it.name})"
    }
    if (index < fields.size() - 1) {
      out.println " &&"
    } else {
      out.println ";"
    }
  }
  out.println "\t}"
  out.println ""
  out.print "\t@Override\n" +
          "\tpublic int hashCode() {\n" +
          "\t\treturn Objects.hash("
  fields.eachWithIndex() { it, index ->
    out.print it.name
    if (index < fields.size() - 1) {
      out.print ",\n\t\t\t\t\t\t\t"
    }
  }
  out.println ");\n" +
          "\t}"
  out.println ""
  out.println "\t@Override"
  out.println "\tpublic String toString() {"
  out.println "\t\treturn \"${className}{\" +"
  fields.eachWithIndex() { it, index ->
    out.print "\t\t\t\"${it.name}='\" + ${it.name} + \"'"
    if (index < fields.size() - 1) {
      out.print ", "
    }
    out.println "\" +"
  }
  out.println "\t\t'}';"
  out.println "\t}"
  out.println ""
  out.println "\t//========================= Queries ========================================"
  generateQueryMethods(out, className)
  out.println ""
  out.println "\t//========================= Inserts ========================================"
  generateInsertMethods(out)
  out.println ""
  out.println "\t//========================= Cloneable ======================================"
  out.println "\t@Override"
  out.println "\tpublic $className clone() {"
  out.println "\t\ttry { return ($className) super.clone(); }"
  out.println "\t\tcatch(CloneNotSupportedException e) { throw new RuntimeException(e); }"
  out.println "\t}"
  out.println "}"
}

def generateImports(out) {

  out.println "import oracle.spatial.geometry.JGeometry;"
  out.println "import org.apache.logging.log4j.LogManager;"
  out.println "import org.apache.logging.log4j.Logger;"
  out.println "import org.springframework.jdbc.core.JdbcTemplate;"
  out.println "import xyz.swatt.asserts.ArgumentChecks;"
  out.println "import xyz.swatt.exceptions.TooManyResultsException;"
  out.println "import xyz.swatt.log.LogMethods;"
  out.println "import xyz.swatt.pojo.SqlPojo;"
  out.println ""
  out.println "import javax.annotation.Generated;"
  out.println "import java.math.BigDecimal;"
  out.println "import java.sql.ResultSet;"
  out.println "import java.sql.ResultSetMetaData;"
  out.println "import java.sql.SQLException;"
  out.println "import java.sql.Timestamp;"
  out.println "import java.time.LocalDateTime;"
  out.println "import java.time.LocalTime;"
  out.println "import java.util.*;"
  out.println "import java.util.stream.Collectors;"
}

def generateGettersAndSetters(out, className, fields) {

  out.println "\t@Override"
  out.println "\tpublic Object getColumnValue(int _columnIndex) {"
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
    if (it.type == "BigDecimal") {
      out.println "\tpublic $className set${it.name.capitalize()}(Number _${it.name}) {"
      out.println "\t\tthis.${it.name} = _${it.name} == null ? null : new BigDecimal(_${it.name}.toString());"
      out.println "\t\treturn this;"
      out.println "\t}"
    } else {
      out.println "\tpublic $className set${it.name.capitalize()}(${it.type} _${it.name}) {"
      out.println "\t\tthis.${it.name} = _${it.name};"
      out.println "\t\treturn this;"
      out.println "\t}"
    }
  }
}

def generateStaticMethods(out, className) {

  out.println "\t/**\n" +
          "\t * Will insert the given POJO as a row in the appropriate table, in the given Database (DB).\n" +
          "\t *\n" +
          "\t * @param _jdbcTemplate\n" +
          "\t * \t\tThe DB Connection to use.\n" +
          "\t * @param _insertData\n" +
          "\t * \t\tThe Data to insert into the Database as a row.\n" +
          "\t *\n" +
          "\t * @throws RuntimeException If the number of Affected Rows is not 1.\n" +
          "\t */\n" +
          "\tpublic static void insert(JdbcTemplate _jdbcTemplate, $className _insertData) {\n" +
          "\t\t_insertData.insert(_jdbcTemplate);\n" +
          "\t}\n"
  out.println "\t/**\n" +
          "\t * Will query a given Database (DB) for the row that match the given Column Values.\n" +
          "\t *\n" +
          "\t * @param _jdbcTemplate\n" +
          "\t * \t\tThe DB Connection to use.\n" +
          "\t * @param _queryData\n" +
          "\t * \t\tThis Object's values will be used as criteria, for the select query.\n" +
          "\t * @param _columns\n" +
          "\t * \t\tIf provided, these will be the only columns that will be used as selection criteria;\n" +
          "\t * \t\totherwise, all of the {@code _queryData} Object's non-null values will be used.\n" +
          "\t *\n" +
          "\t * @return The {@link $className} row found, or {@code null} if no row was found.\n" +
          "\t *\n" +
          "\t * @throws TooManyResultsException\n" +
          "\t * \t\tIf more then 1 row is found by this query.\n" +
          "\t */\n" +
          "\tpublic static $className queryForRow(JdbcTemplate _jdbcTemplate, $className _queryData, Column... _columns) {\n" +
          "\t\t\n" +
          "\t\t//------------------------ Pre-Checks ----------------------------------\n" +
          "\t\t//ArgumentChecks.notNull(_jdbcTemplate, \"JDBC Template\"); // Validated in sub-method.\n" +
          "\t\t//ArgumentChecks.notNull(_queryData, \"Query Data\"); // Validated in sub-method.\n" +
          "\t\t\n" +
          "\t\t//------------------------ CONSTANTS -----------------------------------\n" +
          "\t\t\n" +
          "\t\t//------------------------ Variables -----------------------------------\n" +
          "\t\t$className row;\n" +
          "\t\t\n" +
          "\t\tList<$className> rows = queryForRows(_jdbcTemplate, _queryData, _columns);\n" +
          "\t\t\n" +
          "\t\t//------------------------ Code ----------------------------------------\n" +
          "\t\tswitch(rows.size()) {\n" +
          "\t\t\tcase 0:\n" +
          "\t\t\t\trow = null;\n" +
          "\t\t\t\tbreak;\n" +
          "\t\t\tcase 1:\n" +
          "\t\t\t\trow = rows.get(0);\n" +
          "\t\t\t\tbreak;\n" +
          "\t\t\tdefault:\n" +
          "\t\t\t\tthrow new TooManyResultsException(\"Expected 1 row but found \" + rows.size() + \"!\");\n" +
          "\t\t}\n" +
          "\t\t\n" +
          "\t\treturn row;\n" +
          "\t}\n" +
          "\t\n" +
          "\t/**\n" +
          "\t * @param _jdbcTemplate\n" +
          "\t * \t\tThe DB Connection to use.\n" +
          "\t *\n" +
          "\t * @return All of the {@link $className} rows in the given Database.\n" +
          "\t */\n" +
          "\tpublic static List<$className> queryForAllRows(JdbcTemplate _jdbcTemplate) {\n" +
          "\t\treturn queryForAllRows(_jdbcTemplate, false);\n" +
          "\t}\n" +
          "\t\n" +
          "\t/**\n" +
          "\t * @param _jdbcTemplate\n" +
          "\t * \t\tThe DB Connection to use.\n" +
          "\t * @param _randomize\n" +
          "\t * \t\tIf {@code true}, then the rows returned will be in random order.\n" +
          "\t *\n" +
          "\t * @return All of the {@link $className} rows in the given Database.\n" +
          "\t */\n" +
          "\tpublic static List<$className> queryForAllRows(JdbcTemplate _jdbcTemplate, boolean _randomize) {\n" +
          "\t\t\n" +
          "\t\t//------------------------ Pre-Checks ----------------------------------\n" +
          "\t\tArgumentChecks.notNull(_jdbcTemplate, \"JDBC Template\");\n" +
          "\t\t\n" +
          "\t\t//------------------------ CONSTANTS -----------------------------------\n" +
          "\t\t\n" +
          "\t\t//------------------------ Variables -----------------------------------\n" +
          "\t\tList<$className> rows;\n" +
          "\t\t\n" +
          "\t\t//------------------------ Code ----------------------------------------\n" +
          "\t\trows = _jdbcTemplate.query(\"select * from \" + FULL_TABLE_NAME + (_randomize ? \" ORDER BY dbms_random.value\" : \"\"), new $className());\n" +
          "\t\t\n" +
          "\t\treturn rows;\n" +
          "\t}\n" +
          "\t\n" +
          "\t/**\n" +
          "\t * Will get the requested number of rows.\n" +
          "\t * <p>\n" +
          "\t * <b>Note:</b> This mwthod is synchronized on the given JdbcTemplate.\n" +
          "\t * </p>\n" +
          "\t *\n" +
          "\t * @param _jdbcTemplate\n" +
          "\t * \t\tThe DB Connection to use.\n" +
          "\t * @param _limit\n" +
          "\t * \t\tThe maximum number of rows to return.\n" +
          "\t *\n" +
          "\t * @return All of the {@link $className} rows in the given Database.\n" +
          "\t */\n" +
          "\tpublic static List<$className> queryForRows(JdbcTemplate _jdbcTemplate, int _limit) {\n" +
          "\t\treturn queryForRows(_jdbcTemplate, _limit, false);\n" +
          "\t}\n" +
          "\t\n" +
          "\t/**\n" +
          "\t * Will get the requested number of rows.\n" +
          "\t * <p>\n" +
          "\t * <b>Note:</b> This method is synchronized on the given JdbcTemplate.\n" +
          "\t * </p>\n" +
          "\t *\n" +
          "\t * @param _jdbcTemplate\n" +
          "\t * \t\tThe DB Connection to use.\n" +
          "\t * @param _limit\n" +
          "\t * \t\tThe maximum number of rows to return.\n" +
          "\t * @param _randomize\n" +
          "\t * \t\tIf {@code true}, then the rows returned will be in random order.\n" +
          "\t *\n" +
          "\t * @return All of the {@link $className} rows in the given Database.\n" +
          "\t */\n" +
          "\tpublic static List<$className> queryForRows(JdbcTemplate _jdbcTemplate, int _limit, boolean _randomize) {\n" +
          "\t\t\n" +
          "\t\t//------------------------ Pre-Checks ----------------------------------\n" +
          "\t\tArgumentChecks.notNull(_jdbcTemplate, \"JDBC Template\");\n" +
          "\t\tif(_limit < 1) {\n" +
          "\t\t\tthrow new IllegalArgumentException(\"Given limot must be positive!\");\n" +
          "\t\t}\n" +
          "\t\t\n" +
          "\t\t//------------------------ CONSTANTS -----------------------------------\n" +
          "\t\t\n" +
          "\t\t//------------------------ Variables -----------------------------------\n" +
          "\t\tList<$className> rows;\n" +
          "\t\t\n" +
          "\t\t//------------------------ Code ----------------------------------------\n" +
          "\t\tsynchronized(_jdbcTemplate) { // Max Rows may not get reset in time, if another thread accesses this JDBC Template, without lock.\n" +
          "\t\t\t\n" +
          "\t\t\tint oldMaxRows = _jdbcTemplate.getMaxRows();\n" +
          "\t\t\t\n" +
          "\t\t\ttry {\n" +
          "\t\t\t\t_jdbcTemplate.setMaxRows(_limit); // TODO: \"FETCH NEXT \" + _limit + \" ROWS ONLY\".\n" +
          "\t\t\t\trows = queryForAllRows(_jdbcTemplate, _randomize);\n" +
          "\t\t\t}\n" +
          "\t\t\tfinally {\n" +
          "\t\t\t\t_jdbcTemplate.setMaxRows(oldMaxRows);\n" +
          "\t\t\t}\n" +
          "\t\t}\n" +
          "\t\t\n" +
          "\t\treturn rows;\n" +
          "\t}\n" +
          "\t\n" +
          "\t/**\n" +
          "\t * Will query a given Database (DB) for rows that match the given Column Values.\n" +
          "\t * <p>\n" +
          "\t * <b>Note:</b> This method is synchronized on the given JdbcTemplate, if the given limit is > 0.\n" +
          "\t * </p>\n" +
          "\t *\n" +
          "\t * @param _jdbcTemplate\n" +
          "\t * \t\tThe DB Connection to use.\n" +
          "\t * @param _limit\n" +
          "\t * \t\tThe maximum number of rows to return.\n" +
          "\t * @param _randomize\n" +
          "\t * \t\tIf {@code true}, then the rows returned will be in random order.\n" +
          "\t * @param _queryData\n" +
          "\t * \t\tThis Object's values will be used as criteria, for the select query.\n" +
          "\t * @param _columns\n" +
          "\t * \t\tIf provided, these will be the only columns that will be used as selection criteria; otherwise, all of the {@code _queryData} Object's non-null\n" +
          "\t * \t\tvalues will be used.\n" +
          "\t *\n" +
          "\t * @return The {@link $className} rows found.\n" +
          "\t */\n" +
          "\tpublic static List<$className> queryForRows(JdbcTemplate _jdbcTemplate, int _limit, boolean _randomize, $className _queryData, Column... _columns) {\n" +
          "\t\t\n" +
          "\t\t//------------------------ Pre-Checks ----------------------------------\n" +
          "\t\t//ArgumentChecks.notNull(_jdbcTemplate, \"JDBC Template\"); // Validated in sub-method.\n" +
          "\t\tArgumentChecks.notNull(_queryData, \"Query Data\");\n" +
          "\t\t\n" +
          "\t\t//------------------------ CONSTANTS -----------------------------------\n" +
          "\t\t\n" +
          "\t\t//------------------------ Variables -----------------------------------\n" +
          "\t\t\n" +
          "\t\t//------------------------ Code ----------------------------------------\n" +
          "\t\treturn queryForRows(_jdbcTemplate, _limit, _randomize, Arrays.asList(_queryData), _columns);\n" +
          "\t}\n"

  out.println "\t/**\n" +
          "\t * Will query a given Database (DB) for rows that match the given Column Values.\n" +
          "\t * <p>\n" +
          "\t * <b>Note:</b> This method is synchronized on the given JdbcTemplate, if the given limit is > 0.\n" +
          "\t * </p>\n" +
          "\t *\n" +
          "\t * @param _jdbcTemplate\n" +
          "\t * \t\tThe DB Connection to use.\n" +
          "\t * @param _limit\n" +
          "\t * \t\tThe maximum number of rows to return.\n" +
          "\t * @param _randomize\n" +
          "\t * \t\tIf {@code true}, then the rows returned will be in random order.\n" +
          "\t * @param _queryData\n" +
          "\t * \t\tThis Object's values will be used as criteria, for the select query.\n" +
          "\t * @param _columns\n" +
          "\t * \t\tIf provided, these will be the only columns that will be used as selection criteria; otherwise, all of the {@code _queryData} Object's non-null\n" +
          "\t * \t\tvalues will be used.\n" +
          "\t *\n" +
          "\t * @return The {@link $className} rows found.\n" +
          "\t */\n" +
          "\tpublic static List<$className> queryForRows(JdbcTemplate _jdbcTemplate, int _limit, boolean _randomize, Collection<$className> _queryData,\n" +
          "\t                                                 Column... _columns) {\n" +
          "\t\t\n" +
          "\t\t//------------------------ Pre-Checks ----------------------------------\n" +
          "\t\tArgumentChecks.notNull(_jdbcTemplate, \"JDBC Template\");\n" +
          "\t\tArgumentChecks.notEmpty(_queryData, \"Query Data\");\n" +
          "\t\t\n" +
          "\t\t//------------------------ CONSTANTS -----------------------------------\n" +
          "\t\t\n" +
          "\t\t//------------------------ Variables -----------------------------------\n" +
          "\t\tboolean firstQueryDataObject = true, ignoreNull = _columns == null || _columns.length < 1;\n" +
          "\t\t\n" +
          "\t\tStringBuilder queryStringBuilder = new StringBuilder(\"select * from \" + FULL_TABLE_NAME + \" where \");\n" +
          "\t\t\n" +
          "\t\tColumn[] columnsToParse = ignoreNull ? Column.values() : _columns;\n" +
          "\t\tList<Object> args = new ArrayList(columnsToParse.length);\n" +
          "\t\tList<$className> rows;\n" +
          "\t\t\n" +
          "\t\t//------------------------ Code ----------------------------------------\n" +
          "\t\tfor($className queryData : _queryData) {\n" +
          "\t\t\t\n" +
          "\t\t\tif(firstQueryDataObject) {\n" +
          "\t\t\t\tfirstQueryDataObject = false;\n" +
          "\t\t\t}\n" +
          "\t\t\telse {\n" +
          "\t\t\t\tqueryStringBuilder.append(\" or \");\n" +
          "\t\t\t}\n" +
          "\t\t\tqueryStringBuilder.append(\"(\");\n" +
          "\t\t\t\n" +
          "\t\t\tboolean firstColumnWithData = true;\n" +
          "\t\t\t\n" +
          "\t\t\tfor(Column column : columnsToParse) {\n" +
          "\t\t\t\t\n" +
          "\t\t\t\tObject value = queryData.getColumnValue(column);\n" +
          "\t\t\t\tif(value != null || !ignoreNull) {\n" +
          "\t\t\t\t\t\n" +
          "\t\t\t\t\tif(firstColumnWithData) {\n" +
          "\t\t\t\t\t\tfirstColumnWithData = false;\n" +
          "\t\t\t\t\t}\n" +
          "\t\t\t\t\telse {\n" +
          "\t\t\t\t\t\tqueryStringBuilder.append(\" and \");\n" +
          "\t\t\t\t\t}\n" +
          "\t\t\t\t\t\n" +
          "\t\t\t\t\tqueryStringBuilder.append(column.getColumnName());\n" +
          "\t\t\t\t\t\n" +
          "\t\t\t\t\tif(value == null) {\n" +
          "\t\t\t\t\t\tqueryStringBuilder.append(\" IS NULL\");\n" +
          "\t\t\t\t\t}\n" +
          "\t\t\t\t\telse {\n" +
          "\t\t\t\t\t\tqueryStringBuilder.append(\"=?\");\n" +
          "\t\t\t\t\t\targs.add(value);\n" +
          "\t\t\t\t\t}\n" +
          "\t\t\t\t}\n" +
          "\t\t\t}\n" +
          "\t\t\t\n" +
          "\t\t\tqueryStringBuilder.append(\")\");\n" +
          "\t\t}\n" +
          "\t\t\n" +
          "\t\tif(_randomize) {\n" +
          "\t\t\tqueryStringBuilder.append(\" ORDER BY dbms_random.value\");\n" +
          "\t\t}\n" +
          "\t\t\n" +
          "\t\tif(_limit > 0) {\n" +
          "\t\t\tsynchronized(_jdbcTemplate) { // Max Rows may not get reset in time, if another thread accesses this JDBC Template, without lock.\n" +
          "\t\t\t\t\n" +
          "\t\t\t\tint oldMaxRows = _jdbcTemplate.getMaxRows();\n" +
          "\t\t\t\t\n" +
          "\t\t\t\ttry {\n" +
          "\t\t\t\t\t_jdbcTemplate.setMaxRows(_limit);\n" +
          "\t\t\t\t\trows = _jdbcTemplate.query(queryStringBuilder.toString(), args.toArray(), _queryData.iterator().next());\n" +
          "\t\t\t\t}\n" +
          "\t\t\t\tfinally {\n" +
          "\t\t\t\t\t_jdbcTemplate.setMaxRows(oldMaxRows);\n" +
          "\t\t\t\t}\n" +
          "\t\t\t}\n" +
          "\t\t}\n" +
          "\t\telse {\n" +
          "\t\t\trows = _jdbcTemplate.query(queryStringBuilder.toString(), args.toArray(), _queryData.iterator().next());\n" +
          "\t\t}\n" +
          "\t\t\n" +
          "\t\treturn rows;\n" +
          "\t}\n"

  out.println "\t/**\n" +
          "\t * Will query a given Database (DB) for rows that match the given Column Values.\n" +
          "\t *\n" +
          "\t * @param _jdbcTemplate\n" +
          "\t * \t\tThe DB Connection to use.\n" +
          "\t * @param _queryData\n" +
          "\t * \t\tThis Object's values will be used as criteria, for the select query.\n" +
          "\t * @param _columns\n" +
          "\t * \t\tIf provided, these will be the only columns that will be used as selection criteria; otherwise, all of the {@code _queryData} Object's non-null\n" +
          "\t * \t\tvalues will be used.\n" +
          "\t *\n" +
          "\t * @return The {@link $className} rows found.\n" +
          "\t */\n" +
          "\tpublic static List<$className> queryForRows(JdbcTemplate _jdbcTemplate, $className _queryData, Column... _columns) {\n" +
          "\t\t\n" +
          "\t\t//------------------------ Pre-Checks ----------------------------------\n" +
          "\t\t//ArgumentChecks.notNull(_jdbcTemplate, \"JDBC Template\"); // Validated in sub-method.\n" +
          "\t\tArgumentChecks.notNull(_queryData, \"Query Data\");\n" +
          "\t\t\n" +
          "\t\t//------------------------ CONSTANTS -----------------------------------\n" +
          "\t\t\n" +
          "\t\t//------------------------ Variables -----------------------------------\n" +
          "\t\t\n" +
          "\t\t//------------------------ Code ----------------------------------------\n" +
          "\t\treturn queryForRows(_jdbcTemplate, Arrays.asList(_queryData), _columns);\n" +
          "\t}\n" +
          "\t\n" +
          "\t/**\n" +
          "\t * Will query a given Database (DB) for rows that match the given Column Values.\n" +
          "\t *\n" +
          "\t * @param _jdbcTemplate\n" +
          "\t * \t\tThe DB Connection to use.\n" +
          "\t * @param _queryData\n" +
          "\t * \t\tThis Object's values will be used as criteria, for the select query.\n" +
          "\t * @param _columns\n" +
          "\t * \t\tIf provided, these will be the only columns that will be used as selection criteria; otherwise, all of the {@code _queryData} Object's non-null\n" +
          "\t * \t\tvalues will be used.\n" +
          "\t *\n" +
          "\t * @return The {@link $className} rows found.\n" +
          "\t */\n" +
          "\tpublic static List<$className> queryForRows(JdbcTemplate _jdbcTemplate, Collection<$className> _queryData, Column... _columns) {\n" +
          "\t\treturn queryForRows(_jdbcTemplate, -1, false, _queryData, _columns);\n" +
          "\t}"
}

def generateQueryMethods(out, className) {

  out.println "    /**\n" +
          "     * Will query a given Database (DB) for rows that match this Object's values.\n" +
          "     *\n" +
          "     * @param _jdbcTemplate The DB Connection to use.\n" +
          "     * @param _columns If provided, these will be the only columns that will be used as selection criteria;\n" +
          "     *                 otherwise, all of this Object's non-null values will be used.\n" +
          "     *\n" +
          "     * @return The {@link $className} rows found.\n" +
          "     */\n" +
          "    public List<$className> queryForRows(JdbcTemplate _jdbcTemplate, Column... _columns) {\n" +
          "        return queryForRows(_jdbcTemplate, this, _columns);\n" +
          "    }"
  out.println ""
  out.println "    /**\n" +
          "     * Will query a given Database (DB) for the row that match this Object's values.\n" +
          "     *\n" +
          "     * @param _jdbcTemplate The DB Connection to use.\n" +
          "     * @param _columns If provided, these will be the only columns that will be used as selection criteria;\n" +
          "     *                 otherwise, all of this Object's non-null values will be used.\n" +
          "     *\n" +
          "     * @return The {@link $className} row found, or {@code null} if no row was found.\n" +
          "     *\n" +
          "     * @throws TooManyResultsException If more then 1 row is found by this query.\n" +
          "     */\n" +
          "    public $className queryForRow(JdbcTemplate _jdbcTemplate, Column... _columns) {\n" +
          "        return queryForRow(_jdbcTemplate, this, _columns);\n" +
          "    }"
}

def generateInsertMethods(out) {

  out.println "\t/**\n" +
          "\t * Will insert this POJO as a row in the appropriate table, in the given Database (DB).\n" +
          "\t *\n" +
          "\t * @param _jdbcTemplate\n" +
          "\t * \t\tThe DB Connection to use.\n" +
          "\t *\n" +
          "\t * @throws RuntimeException If the number of Affected Rows is not 1.\n" +
          "\t */\n" +
          "\tpublic void insert(JdbcTemplate _jdbcTemplate) {\n" +
          "\t\t\n" +
          "\t\t//------------------------ Pre-Checks ----------------------------------\n" +
          "\t\tArgumentChecks.notNull(_jdbcTemplate, \"JDBC Template\");\n" +
          "\t\t\n" +
          "\t\t//------------------------ CONSTANTS -----------------------------------\n" +
          "\t\t\n" +
          "\t\t//------------------------ Variables -----------------------------------\n" +
          "\t\tboolean first = true;\n" +
          "\t\tint affectedRowsCount;\n" +
          "\t\t\n" +
          "\t\tString queryString;\n" +
          "\t\tStringBuilder queryStringBuilder = new StringBuilder(\"insert into \" + FULL_TABLE_NAME + \" values (\");\n" +
          "\t\t\n" +
          "\t\tArrayList<Object> args = new ArrayList<>(Column.values().length);\n" +
          "\t\t\n" +
          "\t\t//------------------------ Code ----------------------------------------\n" +
          "\t\tfor(Column column : Column.values()) {\n" +
          "\t\t\t\n" +
          "\t\t\tObject value = getColumnValue(column);\n" +
          "\t\t\t\n" +
          "\t\t\tif(first) {\n" +
          "\t\t\t\t\n" +
          "\t\t\t\tqueryStringBuilder.append('?');\n" +
          "\t\t\t\t\n" +
          "\t\t\t\tfirst = false;\n" +
          "\t\t\t}\n" +
          "\t\t\telse {\n" +
          "\t\t\t\tqueryStringBuilder.append(\", ?\");\n" +
          "\t\t\t}\n" +
          "\t\t\t\n" +
          "\t\t\targs.add(value);\n" +
          "\t\t}\n" +
          "\t\t\n" +
          "\t\tqueryStringBuilder.append(')');\n" +
          "\t\t\n" +
          "\t\tqueryString = queryStringBuilder.toString();\n" +
          "\t\t\n" +
          "\t\taffectedRowsCount = _jdbcTemplate.update(queryString, args.toArray());\n" +
          "\t\tif(affectedRowsCount != 1) {\n" +
          "\t\t\tthrow new RuntimeException(\"Affected Rows Count = \" + affectedRowsCount + \"!\");\n" +
          "\t\t}\n" +
          "\t}"
}

// TODO: Write Delete Method(s).

//---------- Helper Methods ----------
def calcFields(table) {
  DasUtil.getColumns(table).reduce([]) { fields, col ->
    def spec = Case.LOWER.apply(col.getDataType().getSpecification())
    def typeStr = typeMapping.find { p, t -> p.matcher(spec).find() }.value
    fields += [[
                       colname: col.getName(),
                       name   : javaName(col.getName(), false),
                       type   : typeStr,
                       spec   : spec,
                       annos  : ""]]
  }
}

def javaName(str, capitalize) {
  def s = com.intellij.psi.codeStyle.NameUtil.splitNameIntoWords(str)
          .collect { Case.LOWER.apply(it).capitalize() }
          .join("")
          .replaceAll(/[^\p{javaJavaIdentifierPart}[_]]/, "_")
  capitalize || s.length() == 1 ? s : Case.LOWER.apply(s[0]) + s[1..-1]
}
