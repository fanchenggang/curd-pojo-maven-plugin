package com.fancg.maven.plugin.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author: cg
 * @date: 2024-10-30 12:35
 * @describe: TODO
 **/
@ToString
@Getter
@Setter
public class DataSource {
    private String url;
    private String username;
    private String password;
}
