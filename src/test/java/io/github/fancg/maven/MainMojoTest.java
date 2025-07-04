package io.github.fancg.maven;

import com.google.common.collect.Lists;
import io.github.fancg.maven.entity.DataSource;
import io.github.fancg.maven.entity.Schema;
import io.github.fancg.maven.entity.Source;
import io.github.fancg.maven.entity.TableInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fancg
 * @date 2025-07-03
 */
class MainMojoTest {

    DataSource dataSource;
    List<Schema> schemas;
    File basedir;

    @BeforeEach
    void setUp() {
        dataSource = new DataSource();
        dataSource.setUrl("jdbc:mysql://localhost:3306/demo");
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        schemas = new ArrayList<>();
        Schema schema = new Schema();
        schema.setName("");

        Source source1 = new Source();
        source1.setPath("src/test/java/io/github/fancg/maven/*/vo");
        source1.setSubSuffix("Vo");

        Source source2 = new Source();
        source2.setPath("src/test/java/io/github/fancg/maven/vo/*");
        source2.setSubSuffix("Vo");

        TableInfo tableInfo = new TableInfo();
        tableInfo.setIgnoreColumns("created_time");

        schema.setTableInfo(tableInfo);
        schema.setSources(Lists.newArrayList(source1, source2));
        schemas.add(schema);
        basedir = new File("");

    }

    @Test
    void execute() {
        MainMojo mainMojo = new MainMojo();
        mainMojo.init(dataSource, schemas, basedir);
        mainMojo.execute();
    }

    @Test
    void getFiles() throws Exception {
        // 通配符模式（* 表示任意子目录名）
        String path1 = "src/test/java/io/github/fancg/maven/*/vo";
        String path2 = "src/test/java/io/github/fancg/maven/**/vo";
        String path3 = "src/test/java/io/github/fancg/maven/vo/*";
        new FileParse(path2);

    }

}
