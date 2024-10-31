# curd-pojo-maven-plugin
一款在Java应用开发过程中 当数据库字段新增后 同步更新POJO的maven插件

 功能
1. 当数据库字段新增后通过对比同步更新到POJO并支持在字段上添加注解


## 配置示例
```xml
 <build>
        <plugins>
            <plugin>
                <groupId>io.github.fanchenggang</groupId>
                <artifactId>curd-pojo-maven-plugin</artifactId>
                <version>20241031.A</version>
                <configuration>
                     <dataSource>
                         <url>jdbc:mysql://127.0.0.1:3306/****?useUnicode=true&amp;characterEncoding=utf8&amp;zeroDateTimeBehavior=convertToNull&amp;useSSL=true&amp;serverTimezone=GMT%2B8</url>
                         <username>****</username>
                         <password>******</password>
                     </dataSource>
                    <schemas>
                        <schema>
                            <name>test</name> <!-- 数据库名称-->
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
