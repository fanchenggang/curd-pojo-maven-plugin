package io.github.fancg.maven.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
/**
 * @author: cg
 * @date: 2024-10-30 12:52
 * @describe: TODO
 **/
@ToString
@Getter
@Setter
public class Schema {
    private String name;
    private List<Source> sources;
    private TableInfo tableInfo;
}
