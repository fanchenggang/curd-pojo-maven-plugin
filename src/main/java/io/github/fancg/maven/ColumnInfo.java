package io.github.fancg.maven;

import lombok.Getter;
import lombok.Setter;

/**
 * @author: cg
 * @date: 2024-08-17 23:30
 * @describe: TODO
 **/
@Getter
@Setter
public class ColumnInfo {
   private String tableName;
   private String columnName;
   private String dataType;
   private String jdbcType;
   private String javaName;
   private String columnComment;
}
