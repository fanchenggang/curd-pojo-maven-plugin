package io.github.fancg.maven;

import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fancg
 * @date 2025-07-04
 */
public class PathParse {
    private final PathMatcher pathMatcher;
    private final Path baseDir;

    public PathParse(String pattern, String baseDirStr) {
        // 将用户输入的路径模式转换为 Ant 风格的 glob 模式（Java 标准）
        String globPattern = "glob:" + pattern.replace("\\", "/"); // 统一路径分隔符
        this.pathMatcher = FileSystems.getDefault().getPathMatcher(globPattern);
        this.baseDir = Paths.get(baseDirStr).toAbsolutePath(); // 项目根目录
    }

    // 获取所有匹配的文件路径
    public List<String> getMatchedFiles() throws Exception {
        List<String> result = new ArrayList<>();
        Path absolutePath = baseDir.toAbsolutePath();

        // 递归遍历项目根目录下的所有文件
        Files.walk(baseDir)
                .filter(path -> pathMatcher.matches(baseDir.relativize(path))) // 相对路径匹配
                .forEach(p -> result.add(p.toFile().getPath()
                        .substring(absolutePath.toString().length() + 1)));
        return result;
    }
}



