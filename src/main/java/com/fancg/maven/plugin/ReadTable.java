package com.fancg.maven.plugin;

import com.fancg.maven.plugin.entity.DataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: cg
 * @date: 2024-10-30 11:55
 * @describe: TODO
 **/

public class ReadTable {
    public Map<String, List<ColumnInfo>> getColumnInfoMap(DataSource source, String schema) {
        try {
            List<ColumnInfo> columnInfoList = getColumnInfoList(source, schema);
//            Map<String,Map<String,ColumnInfo>> result = new HashMap<>();
//            columnInfoList.stream().collect(Collectors.groupingBy(ColumnInfo::getTableName))
//                    .forEach((k,v)-> result.put(k,v.stream().collect(Collectors.toMap(ColumnInfo::getColumnName, c->c))));
            return columnInfoList.stream().collect(Collectors.groupingBy(ColumnInfo::getTableName));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<ColumnInfo> getColumnInfoList(DataSource source, String tableSchema) throws SQLException {
        return queryRunner(source).query("SELECT table_name tableName,column_name columnName,data_type dataType,column_comment columnComment FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=? order by ordinal_position asc",
                new BeanListHandler<>(ColumnInfo.class), tableSchema);
    }

    public QueryRunner queryRunner(DataSource source) throws SQLException {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUrl(source.getUrl());
        dataSource.setUser(source.getUsername());
        dataSource.setPassword(source.getPassword());
        dataSource.getConnection();
        return new QueryRunner(dataSource);
    }
}
