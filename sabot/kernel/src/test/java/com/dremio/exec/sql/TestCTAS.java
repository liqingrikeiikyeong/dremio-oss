/*
 * Copyright (C) 2017-2018 Dremio Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dremio.exec.sql;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.PrintStream;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;

import com.dremio.PlanTestBase;
import com.dremio.exec.proto.UserBitShared.DremioPBError.ErrorType;

public class TestCTAS extends PlanTestBase {
  @Test // DRILL-2589
  public void withDuplicateColumnsInDef1() throws Exception {
    ctasErrorTestHelper("CREATE TABLE %s.%s AS SELECT region_id, region_id FROM cp.\"region.json\"",
        String.format("Duplicate column name [%s]", "region_id")
    );
  }

  @Test // DRILL-2589
  public void withDuplicateColumnsInDef2() throws Exception {
    ctasErrorTestHelper("CREATE TABLE %s.%s AS SELECT region_id, sales_city, sales_city FROM cp.\"region.json\"",
        String.format("Duplicate column name [%s]", "sales_city")
    );
  }

  @Test // DRILL-2589
  public void withDuplicateColumnsInDef3() throws Exception {
    ctasErrorTestHelper(
        "CREATE TABLE %s.%s(regionid, regionid) " +
            "AS SELECT region_id, sales_city FROM cp.\"region.json\"",
        String.format("Duplicate column name [%s]", "regionid")
    );
  }

  @Test // DRILL-2589
  public void withDuplicateColumnsInDef4() throws Exception {
    ctasErrorTestHelper(
        "CREATE TABLE %s.%s(regionid, salescity, salescity) " +
            "AS SELECT region_id, sales_city, sales_city FROM cp.\"region.json\"",
        String.format("Duplicate column name [%s]", "salescity")
    );
  }

  @Test // DRILL-2589
  public void withDuplicateColumnsInDef5() throws Exception {
    ctasErrorTestHelper(
        "CREATE TABLE %s.%s(regionid, salescity, SalesCity) " +
            "AS SELECT region_id, sales_city, sales_city FROM cp.\"region.json\"",
        String.format("Duplicate column name [%s]", "SalesCity")
    );
  }

  @Test // DRILL-2589
  public void whenInEqualColumnCountInTableDefVsInTableQuery() throws Exception {
    ctasErrorTestHelper(
        "CREATE TABLE %s.%s(regionid, salescity) " +
            "AS SELECT region_id, sales_city, sales_region FROM cp.\"region.json\"",
        "table's field list and the table's query field list have different counts."
    );
  }

  @Test // DRILL-2589
  @Ignore
  public void whenTableQueryColumnHasStarAndTableFiledListIsSpecified() throws Exception {
    ctasErrorTestHelper(
        "CREATE TABLE %s.%s(regionid, salescity) " +
            "AS SELECT region_id, * FROM cp.\"region.json\"",
        "table's query field list has a '*', which is invalid when table's field list is specified."
    );
  }

  @Test // DRILL-2422
  public void createTableWhenATableWithSameNameAlreadyExists() throws Exception{
    final String newTblName = "createTableWhenTableAlreadyExists";

    try {
      final String ctasQuery =
          String.format("CREATE TABLE %s.%s AS SELECT * from cp.\"region.json\"", TEMP_SCHEMA, newTblName);

      test(ctasQuery);

      errorMsgTestHelper(ctasQuery,
          String.format("A table or view with given name [%s.%s] already exists", TEMP_SCHEMA, newTblName));
    } finally {
      FileUtils.deleteQuietly(new File(getDfsTestTmpSchemaLocation(), newTblName));
    }
  }

  @Test // DRILL-2422
  public void createTableWhenAViewWithSameNameAlreadyExists() throws Exception{
    final String newTblName = "createTableWhenAViewWithSameNameAlreadyExists";

    try {
      test(String.format("CREATE VIEW %s.%s AS SELECT * from cp.\"region.json\"", TEMP_SCHEMA, newTblName));

      final String ctasQuery =
          String.format("CREATE TABLE %s.%s AS SELECT * FROM cp.\"employee.json\"", TEMP_SCHEMA, newTblName);

      errorMsgTestHelper(ctasQuery,
          String.format("A table or view with given name [%s.%s] already exists",
              "dfs_test", newTblName));
    } finally {
      test(String.format("DROP VIEW %s.%s", TEMP_SCHEMA, newTblName));
    }
  }

  @Test
  public void ctasPartitionWithEmptyList() throws Exception {
    final String newTblName = "ctasPartitionWithEmptyList";
    final String ctasQuery = String.format("CREATE TABLE %s.%s PARTITION BY AS SELECT * from cp.\"region.json\"", TEMP_SCHEMA, newTblName);

    try {
      errorTypeTestHelper(ctasQuery, ErrorType.PARSE);
    } finally {
      FileUtils.deleteQuietly(new File(getDfsTestTmpSchemaLocation(), newTblName));
    }
  }

  @Test // DRILL-3377
  public void partitionByCtasColList() throws Exception {
    final String newTblName = "partitionByCtasColList";

    try {
      final String ctasQuery = String.format("CREATE TABLE %s.%s (cnt, rkey) PARTITION BY (cnt) " +
          "AS SELECT count(*), n_regionkey from cp.\"tpch/nation.parquet\" group by n_regionkey",
          TEMP_SCHEMA, newTblName);

      test(ctasQuery);

      final String selectFromCreatedTable = String.format(" select cnt, rkey from %s.%s", TEMP_SCHEMA, newTblName);
      final String baselineQuery = "select count(*) as cnt, n_regionkey as rkey from cp.\"tpch/nation.parquet\" group by n_regionkey";
      testBuilder()
          .sqlQuery(selectFromCreatedTable)
          .unOrdered()
          .sqlBaselineQuery(baselineQuery)
          .build()
          .run();
    } finally {
      FileUtils.deleteQuietly(new File(getDfsTestTmpSchemaLocation(), newTblName));
    }
  }

  @Test // DX - 16118
  public void testParquetComplexWithNull() throws Exception {
    final String newTblName = "parquetComplexWithNull";

    try {
      final String ctasQuery = String.format("CREATE TABLE %s.%s AS SELECT index from " +
          "dfs.\"${WORKING_PATH}/src/test/resources/complex_with_null.parquet\" where play_name is not null",
        TEMP_SCHEMA, newTblName);

      test(ctasQuery);

      final String selectFromCreatedTable = String.format("select count(*) as cnt from %s.%s where index is null", TEMP_SCHEMA, newTblName);
      testBuilder()
        .sqlQuery(selectFromCreatedTable)
        .unOrdered()
        .baselineColumns("cnt")
        .baselineValues(111396l)
        .build()
        .run();
    } finally {
      FileUtils.deleteQuietly(new File(getDfsTestTmpSchemaLocation(), newTblName));
    }
  }

  @Test // DRILL-3374
  public void partitionByCtasFromView() throws Exception {
    final String newTblName = "partitionByCtasColList2";
    final String newView = "partitionByCtasColListView";
    try {
      final String viewCreate = String.format("create or replace view %s.%s (col_int, col_varchar)  " +
          "AS select cast(n_nationkey as int), cast(n_name as varchar(30)) from cp.\"tpch/nation.parquet\"",
          TEMP_SCHEMA, newView);

      final String ctasQuery = String.format("CREATE TABLE %s.%s PARTITION BY (col_int) AS SELECT * from %s.%s",
          TEMP_SCHEMA, newTblName, TEMP_SCHEMA, newView);

      test(viewCreate);
      test(ctasQuery);

      final String baselineQuery = "select cast(n_nationkey as int) as col_int, cast(n_name as varchar(30)) as col_varchar " +
        "from cp.\"tpch/nation.parquet\"";
      final String selectFromCreatedTable = String.format("select col_int, col_varchar from %s.%s", TEMP_SCHEMA, newTblName);
      testBuilder()
          .sqlQuery(selectFromCreatedTable)
          .unOrdered()
          .sqlBaselineQuery(baselineQuery)
          .build()
          .run();

      final String viewDrop = String.format("DROP VIEW %s.%s", TEMP_SCHEMA, newView);
      test(viewDrop);
    } finally {
      FileUtils.deleteQuietly(new File(getDfsTestTmpSchemaLocation(), newTblName));
    }
  }

  @Test // DRILL-3382
  public void ctasWithQueryOrderby() throws Exception {
    final String newTblName = "ctasWithQueryOrderby";

    try {
      final String ctasQuery = String.format("CREATE TABLE %s.%s   " +
          "AS SELECT n_nationkey, n_name, n_comment from cp.\"tpch/nation.parquet\" order by n_nationkey",
          TEMP_SCHEMA, newTblName);

      test(ctasQuery);

      final String selectFromCreatedTable = String.format(" select n_nationkey, n_name, n_comment from %s.%s", TEMP_SCHEMA, newTblName);
      final String baselineQuery = "select n_nationkey, n_name, n_comment from cp.\"tpch/nation.parquet\" order by n_nationkey";

      testBuilder()
          .sqlQuery(selectFromCreatedTable)
          .ordered()
          .sqlBaselineQuery(baselineQuery)
          .build()
          .run();
    } finally {
      FileUtils.deleteQuietly(new File(getDfsTestTmpSchemaLocation(), newTblName));
    }
  }

  @Test // DRILL-4392
  public void ctasWithPartition() throws Exception {
    final String newTblName = "nation_ctas";

    try {
      final String ctasQuery = String.format("CREATE TABLE %s.%s   " +
          "partition by (n_regionkey) AS SELECT n_nationkey, n_regionkey from cp.\"tpch/nation.parquet\" order by n_nationkey limit 1",
          TEMP_SCHEMA, newTblName);

      test(ctasQuery);

      final String selectFromCreatedTable = String.format(" select * from %s.%s", TEMP_SCHEMA, newTblName);

      test(String.format("select * from %s.%s", TEMP_SCHEMA, newTblName));

      testBuilder()
          .sqlQuery(selectFromCreatedTable)
          .ordered()
          .baselineColumns("dir0", "n_nationkey", "n_regionkey")
          .baselineValues("0", 0, 0)
          .build()
          .run();
    } finally {
      FileUtils.deleteQuietly(new File(getDfsTestTmpSchemaLocation(), newTblName));
    }
  }

  @Test
  public void ctasFailures() throws Exception {
    String inputTable = "ctasFailureInput";
    String fileName = "f.json";
    File directory = new File(getDfsTestTmpSchemaLocation(), inputTable);
    directory.mkdir();
    PrintStream ps = new PrintStream(new File(directory, fileName));

    for (int i = 0; i < 100_000; i++) {
      ps.println("{ a : 1 }");
    }
    ps.println("{ a : ");
    ps.close();

    try {
      test("create table dfs_test.ctasFailure as select * from dfs_test." + inputTable);
    } catch (Exception e) {
      // no op
    }

    String outputTable = "ctasFailure";

    assertFalse(new File(getDfsTestTmpSchemaLocation(), outputTable).exists());
  }

  private static void ctasErrorTestHelper(final String ctasSql, final String expErrorMsg) throws Exception {
    final String createTableSql = String.format(ctasSql, TEMP_SCHEMA, "testTableName");
    errorMsgTestHelper(createTableSql, expErrorMsg);
  }
}
