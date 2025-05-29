# curd-pojo-maven-plugin

A Maven plugin that synchronously updates POJOs after new database fields are added during Java application development.
> 一款在Java应用开发过程中 当数据库字段新增后 同步更新POJO的maven插件.

### Features

After new database fields are added, synchronously update to POJOs through comparison and support adding annotations to
the fields
> 当数据库字段新增后,通过比较同步更新POJO,支持给字段添加注解

### demo
```xml
 <build>
        <plugins>
            <plugin>
                <groupId>io.github.fanchenggang</groupId>
                <artifactId>curd-pojo-maven-plugin</artifactId>
                <version>20250225.A</version>
                <configuration>
                     <dataSource>
                         <url>jdbc:mysql://127.0.0.1:3306/****?useUnicode=true&amp;characterEncoding=utf8&amp;zeroDateTimeBehavior=convertToNull&amp;useSSL=true&amp;serverTimezone=GMT%2B8</url>
                         <username>****</username>
                         <password>******</password>
                     </dataSource>
                    <schemas>
                        <schema>
                            <name>test</name> <!-- database name-->
                            <tableInfo>
                                <ignoreColumns>created_time,updated_time,deleted</ignoreColumns>
                            </tableInfo>
                            <sources>
                                <source>
                                    <path>src/main/java/com/ruoyi/perf/reassign/entity</path>
                                    <xmlPath>src/main/java/com/ruoyi/perf/reassign/mapper</xmlPath>
                                    <annotations>
                                        <annotation>
                                            <className>com.baomidou.mybatisplus.annotation.TableField</className>
                                        </annotation>
                                    </annotations>
                                </source>
                                <source>
                                    <path>src/main/java/com/ruoyi/perf/reassign/domain/vo</path>
                                    <subSuffix>VO</subSuffix>
                                    <annotations>
                                        <annotation>
                                            <className>io.swagger.annotations.ApiModelProperty</className>
                                        </annotation>
                                    </annotations>
                                </source>
                            </sources>
                        </schema>
                    </schemas>
                </configuration>
                <executions>
                    <execution>
                        <id>first</id>
                        <phase>none</phase>
                        <goals>
                            <goal>sync</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```
