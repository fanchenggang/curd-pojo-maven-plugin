package io.github.fancg.maven;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author fancg
 * @date 2025-07-04
 */
public class FileParse {

    @Getter
    private List<String> filePathList;
    private final String matchPath;
    private Tree tree;
    private Log log;

    public Log getLog() {
        if (this.log == null) {
            this.log = new SystemStreamLog();
        }

        return this.log;
    }

    protected static class Tree {
        String path;
        boolean last;
        List<Tree> childList = new ArrayList<>();
    }

    public FileParse(String matchPath) {
        this.matchPath = matchPath;
        this.filePathList = new ArrayList<>();
        parse();
    }

    private void parse() {
        createTree();
        printTree();
    }

    private void printTree() {
        // System.out.println(tree.path);
        printTree(tree, "", 0);
    }

    private void printTree(Tree tree, String path, int level) {
//        for (int i = 0; i < level; i++) {
//            System.out.print(" ");
//        }
        // System.out.println(tree.path);
        if (tree.last) {
            String lastPath = path + "/" + tree.path;

            Path dirPath = Paths.get(lastPath);
            //  System.out.println(lastPath);
            if (Files.exists(dirPath)) {
                //    System.out.println(lastPath);
                getLog().debug("parse path:" + lastPath);
                filePathList.add(lastPath);
            }
        }
        tree.childList.forEach(c -> {
            printTree(c, (path.isEmpty() ? "" : path + "/") + tree.path, level + 1);
        });
    }

    private void createTree() {
        //   treeList = new ArrayList<>();
        String[] paths = matchPath.split("/");
        tree = new Tree();
        tree.path = paths[0];
        //tree.childList = parseTree(paths[1], paths[2]);
        parseTree(tree, paths, 1);
    }

    private void parseTree(Tree tree, String[] paths, int index) {
        if (index == paths.length) {
            tree.last = true;
            return;
        }
        tree.childList = new ArrayList<>();

        String path = paths[index];
        if ("*".equals(path)) {
            String[] childPaths = getDirList(paths, index);
            for (String childPath : childPaths) {
                Tree child = new Tree();
                child.path = childPath;
                tree.childList.add(child);
                parseTree(child, paths, index + 1);
            }
        }
        if ("**".equals(path)) {

        } else {
            Tree child = new Tree();
            child.path = paths[index];
            tree.childList.add(child);
            parseTree(child, paths, index + 1);
        }

    }


    @SneakyThrows
    private String[] getDirList(String[] paths, int index) {
        // 转换为 Path 对象
        String path = Arrays.stream(paths).limit(index).collect(Collectors.joining("/"));
        Path dirPath = Paths.get(path);
        // 检查路径是否存在
//        if (!Files.exists(dirPath)) {
//            throw new Exception("路径不存在: " + path);
//        }
        DirectoryStream<Path> pathDirectoryStream = Files.newDirectoryStream(dirPath, p -> p.toFile().isDirectory());
        return Lists.newArrayList(pathDirectoryStream).stream().map(a -> a.toFile().getName()).toArray(String[]::new);
    }

}
