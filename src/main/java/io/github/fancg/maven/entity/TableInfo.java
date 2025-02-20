package io.github.fancg.maven.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author qr
 * @date 2024-12-18
 */
@ToString
@Getter
@Setter
public class TableInfo {
    private String tableNames;
    private String ignoreColumns;
    private String authors;
}
