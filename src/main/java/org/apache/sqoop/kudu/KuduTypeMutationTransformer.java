/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.sqoop.kudu;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.Time;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kudu.client.Insert;
import org.apache.kudu.client.KuduTable;
import org.apache.kudu.client.PartialRow;
import org.apache.kudu.ColumnSchema;
import org.apache.kudu.Schema;
import org.apache.kudu.Type;

/**
 * MutationTransformer that generate Insert operations for Kudu.
 */
public class KuduTypeMutationTransformer extends MutationTransformer {

  public static final Log LOG = LogFactory
      .getLog(KuduTypeMutationTransformer.class.getName());


  public KuduTypeMutationTransformer() {
  }

  /**
   * Generates a list of Insert operations for Kudu.
   * @param fields
   *            a map of field names to values to insert.
   * @return A collection of Insert operations.
   * @throws IOException
   */
  @Override
  public List<Insert> getInsertCommand(Map<String, Object> fields)
      throws IOException {

    KuduTable table = getKuduTable();
    Schema schema = table.getSchema();
    List<ColumnSchema> columnSchemaList = schema.getColumns();
    Map<String, Type> columnTypeMap = new HashMap<String, Type>();
    Insert insert = table.newInsert();
    PartialRow row = insert.getRow();

    for (ColumnSchema columnSchema : columnSchemaList) {
      columnTypeMap.put(columnSchema.getName(), columnSchema.getType());
    }

    for (Map.Entry<String, Object> fieldEntry : fields.entrySet()) {

      String colName = fieldEntry.getKey();
      if (!columnTypeMap.containsKey(colName)) {
        throw new IOException("Could not find column  " + colName
            + " of type in table " + table.getName());
      }
      Type columnType = columnTypeMap.get(colName);
      Object val = fieldEntry.getValue();

       

      switch (columnType.getName()) {
        case "binary":
          row.addBinary(colName, (byte[]) val);
          break;
        case "bool":
          row.addBoolean(colName, (boolean) val);
          break;
        case "double":
        	if( val instanceof java.math.BigDecimal){
        		   row.addDouble(colName, val==null?0.0:((java.math.BigDecimal) val).doubleValue());
        	}else{
        		   row.addDouble(colName, val==null?0.0:(Double) val);
        	}
       
          break;
        case "float":
          row.addFloat(colName, val==null?0.0f:(Float) val);
          break;
        case "int8":
        case "int16":
        case "int32":
        	if(val ==null){ val=0;}
            if(val instanceof java.lang.Long) {
  			Integer intVal = ((java.lang.Long) val).intValue();
  	        row.addInt(colName, intVal);
  		  }else{
  			row.addInt(colName, val==null?0:(int) val);
  			  
  		  }
            break;
        case "int64":
        	if(val ==null){ val=0L;};
  	        row.addLong(colName, (Long)val);
            break;
        case "string":
          row.addString(colName, String.valueOf(val));
          break;
        case "unixtime_micros":
          if(val!=null){
        	  row.addLong(colName, ((java.sql.Timestamp) val).getTime()*1000);
          }else{
        	  LOG.error("unixtime_micros colName: "+colName+ "val: "+val );
          }
          break;
        case "timestamp":
          row.addLong(colName, ((java.sql.Timestamp) val).getTime());
          break;
        default:
          LOG.error("No Mapping found for:" + colName
              + " for type: " + columnType.getName());

      }
    }

    return Collections.singletonList(insert);
  }


}
