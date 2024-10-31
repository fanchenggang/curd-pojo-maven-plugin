package io.github.fancg.maven;

import io.github.fancg.maven.entity.DataSource;
import io.github.fancg.maven.entity.Schema;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Hello world!
 */
@Mojo(name = "sync")
public class MainMojo extends AbstractMojo {


    @Parameter
    private DataSource dataSource;

    @Parameter
    private List<Schema> schemas;


    @Parameter( defaultValue = "${project.basedir}", readonly = true )
    private File basedir;


    @Override
    public void execute() {
        getLog().info("dataSource.url: " + dataSource.getUrl());
        getLog().info("project.basedir: " + basedir.getAbsolutePath());
        ReadTable readTable = new ReadTable();
        Service service = new Service();
        for (Schema sa : schemas) {
            Map<String, java.util.List<ColumnInfo>> schemaColumns = readTable.getColumnInfoMap(dataSource, sa.getName());
            getLog().debug("获取表数量:" + schemaColumns.size());
            try {
                service.setBasedirPath(basedir.getAbsolutePath());
                service.setSchemaColumnsMap(schemaColumns);
                service.sync(schemas);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }
}
