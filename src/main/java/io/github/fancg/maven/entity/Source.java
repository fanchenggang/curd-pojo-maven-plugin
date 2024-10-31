package io.github.fancg.maven.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author: cg
 * @date: 2024-10-30 12:54
 * @describe: TODO
 **/
@ToString
@Getter
@Setter
public class Source {
    private String path;
    private String xmlPath;
    private String subSuffix;
    private List<Annotation> annotations;
}
