package io.github.fancg.maven;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.printer.PrettyPrinter;
import com.github.javaparser.printer.configuration.PrettyPrinterConfiguration;
import com.google.common.collect.Lists;
import io.github.fancg.maven.entity.Annotation;
import io.github.fancg.maven.entity.Schema;
import io.github.fancg.maven.entity.Source;
import io.github.fancg.maven.util.StringUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author: cg
 * @date: 2024-10-30 13:18
 * @describe: TODO
 **/
public class Service {
    private Log log;

    static ParserConfiguration parserConfiguration = new ParserConfiguration();
    static JavaParser javaParser = new JavaParser(parserConfiguration);
    // 初始化打印配置
    static PrettyPrinterConfiguration prettyPrinterConfiguration = new PrettyPrinterConfiguration();
    // 创建打印器
    static PrettyPrinter prettyPrinter = new PrettyPrinter(prettyPrinterConfiguration);

    static Map<String, String> javaTypeMap = new HashMap<>();
    static Map<String, String> jdbcTypeMap = new HashMap<>();
    private Map<String, List<ColumnInfo>> schemaColumnsMap = new HashMap<>();
    private String basedirPath;

    public void setSchemaColumnsMap(Map<String, List<ColumnInfo>> schemaColumnsMap) {
        this.schemaColumnsMap = schemaColumnsMap;
    }

    public void setBasedirPath(String basedirPath) {
        this.basedirPath = basedirPath;
    }

    public Log getLog() {
        if (this.log == null) {
            this.log = new SystemStreamLog();
        }

        return this.log;
    }


    static {
        javaTypeMap.put("int", "Integer");
        javaTypeMap.put("varchar", "String");
        javaTypeMap.put("json", "JSONArray");
        javaTypeMap.put("decimal", "BigDecimal");
        javaTypeMap.put("bigint", "Long");
        javaTypeMap.put("datetime", "Date");
        javaTypeMap.put("tinyint", "Integer");

        jdbcTypeMap.put("int", "INTEGER");
        jdbcTypeMap.put("bigint", "INTEGER");
        jdbcTypeMap.put("varchar", "VARCHAR");
        jdbcTypeMap.put("json", "VARCHAR");
        jdbcTypeMap.put("decimal", "DECIMAL");
        jdbcTypeMap.put("tinyint", "INTEGER");
        jdbcTypeMap.put("datetime", "TIMESTAMP");
    }


    public void sync(List<Schema> schemas) throws Exception {
        for (Schema schema : schemas) {
            for (Source source : schema.getSources()) {
                List<Annotation> annotations = source.getAnnotations();
                sync(StringUtils.splitToList(schema.getTableInfo().getAuthors()),
                        StringUtils.splitToList(schema.getTableInfo().getIgnoreColumns())
                        , source, annotations);
            }
        }
    }


    public void sync(List<String> authors, List<String> ignoreColumns, Source source, List<Annotation> annotations) throws Exception {
        getLog().debug("ignoreColumns:" + ignoreColumns);
        //扫描path下的所有java对象
        FileParse fileParse = new FileParse(source.getPath());
        List<String> filePathList = fileParse.getFilePathList();
        for (String path : filePathList) {
            File filePath = new File(basedirPath + "/" + path);
            File[] files = filePath.listFiles();
            if (files == null) {
                getLog().warn("path:" + filePath.getAbsolutePath() + " 不存在");
                return;
            }
            for (File file : files) {
                if (file.isDirectory()) {
                    continue;
                }
                sync(authors, ignoreColumns, source, file, annotations);
            }
        }
    }


    private void sync(List<String> authors, List<String> ignoreColumns, Source source, File file, List<Annotation> annotations) throws Exception {
        getLog().debug("Java source path:" + file.getAbsolutePath());
        ParseResult<CompilationUnit> parseResult = javaParser.parse(file);
        CompilationUnit compilationUnit = parseResult.getResult().get();
        if (!isAuthors(authors, compilationUnit)) {
            getLog().debug("跳过文件:" + file.getAbsolutePath());
            return;
        }

        List<FieldDeclaration> fields = compilationUnit.findAll(FieldDeclaration.class);
        List<ColumnInfo> addColumnInfoList = Lists.newArrayList();
        List<String> fieldNameList = Lists.newArrayList();
        for (FieldDeclaration field : fields) {
            String fieldName = field.getVariable(0).getName().asString();

            fieldNameList.add(fieldName);
        }
        //获取类名
        String name = compilationUnit.getType(0).getName().asString();
        //如果配置了后缀 VO DTO ... 要截取掉在匹配
        if (!StringUtils.isEmpty(source.getSubSuffix())) {
            name = name.substring(0, name.length() - source.getSubSuffix().length());
        }
        String tableName = StringUtils.camelToUnderline(name);
        List<ColumnInfo> columnInfoList = schemaColumnsMap.get(tableName);
        getLog().debug("tableName: " + tableName + ", columnSize: " + (columnInfoList == null ? 0 : columnInfoList.size()));
        if (columnInfoList == null) {
            return;
        }
        for (ColumnInfo columnInfo : columnInfoList) {
            if (!ignoreColumns.isEmpty() && ignoreColumns.contains(columnInfo.getColumnName())) {
                continue;
            }
            if (!fieldNameList.contains(StringUtils.underlineToCamel(columnInfo.getColumnName()))) {
                columnInfo.setJavaName(StringUtils.underlineToCamel(columnInfo.getColumnName()));
                columnInfo.setJdbcType(jdbcTypeMap.get(columnInfo.getDataType()));
                addColumnInfoList.add(columnInfo);
            }
        }

        if (addColumnInfoList.isEmpty()) {
            getLog().debug(tableName + ":没有缺失字段");
            return;
        }
        for (ColumnInfo columnInfo : addColumnInfoList) {
            getLog().info("缺少字段:" + columnInfo.getTableName() + ":" + columnInfo.getColumnName() + ":" + columnInfo.getDataType() + ":" + columnInfo.getColumnComment());
        }

        modifyEntity(compilationUnit, addColumnInfoList, annotations);
        if (source.getXmlPath() != null && !"".equals(source.getXmlPath())) {
            modifyXml(source.getXmlPath() + "/" + name + "Mapper.xml", addColumnInfoList);
        }
    }

    public boolean isAuthors(List<String> authors, CompilationUnit compilationUnit) {
        //如果没有限定 返回true
        if (authors.isEmpty()) {
            return true;
        }
        ClassOrInterfaceDeclaration typeDeclaration = (ClassOrInterfaceDeclaration) compilationUnit.getTypes().get(0);
        Optional<Comment> comment = typeDeclaration.getComment();
        String content = comment.get().getContent();
        for (String author : authors) {
            if (content.contains("@author: " + author)) {
                return true;
            }
        }
        return false;
    }

    public void modifyEntity(CompilationUnit compilationUnit, List<ColumnInfo> columnInfoList, List<Annotation> annotations) throws FileNotFoundException {
        for (ColumnInfo columnInfo : columnInfoList) {
            FieldDeclaration fieldDeclaration = new FieldDeclaration();
            NodeList<VariableDeclarator> variableDeclarators = new NodeList<>();
            String type = javaTypeMap.get(columnInfo.getDataType());
            if (type == null) {
                getLog().warn("不支持的数据类型:" + columnInfo.getDataType());
                type = "Object";
            }
            VariableDeclarator variableDeclarator = new VariableDeclarator(new ClassOrInterfaceType(type), StringUtils.underlineToCamel(columnInfo.getColumnName()));
            variableDeclarators.add(variableDeclarator);
            fieldDeclaration.setVariables(variableDeclarators);

            NodeList<Modifier> modifiers = new NodeList<>();
            modifiers.add(new Modifier(Modifier.Keyword.PRIVATE));
            fieldDeclaration.setModifiers(modifiers);
            NodeList<AnnotationExpr> annotationExprs = new NodeList<>();
            if (annotations != null) {
                for (Annotation annotation : annotations) {
                    if (annotation.getClassName().endsWith("ApiModelProperty")) {
                        NormalAnnotationExpr normalAnnotationExpr = new NormalAnnotationExpr();
                        normalAnnotationExpr.setName("ApiModelProperty");
                        NodeList<MemberValuePair> memberValuePairs = new NodeList<>();
                        memberValuePairs.add(new MemberValuePair("value", new StringLiteralExpr(columnInfo.getColumnComment())));
                        normalAnnotationExpr.setPairs(memberValuePairs);
                        annotationExprs.add(normalAnnotationExpr);
                    } else if (annotation.getClassName().endsWith("TableField")) {
                        NormalAnnotationExpr normalAnnotationExpr = new NormalAnnotationExpr();
                        normalAnnotationExpr.setName("TableField");
                        NodeList<MemberValuePair> memberValuePairs = new NodeList<>();
                        memberValuePairs.add(new MemberValuePair("value", new StringLiteralExpr(columnInfo.getColumnName())));
                        if (columnInfo.getDataType().equals("json")) {
                            memberValuePairs.add(new MemberValuePair("typeHandler", new ClassExpr(new ClassOrInterfaceType("Fastjson2TypeHandler"))));
                        }
                        normalAnnotationExpr.setPairs(memberValuePairs);
                        annotationExprs.add(normalAnnotationExpr);
                    } else if (annotation.getClassName().endsWith("Excel")) {
                        NormalAnnotationExpr normalAnnotationExpr = new NormalAnnotationExpr();
                        normalAnnotationExpr.setName("Excel");
                        NodeList<MemberValuePair> memberValuePairs = new NodeList<>();
                        memberValuePairs.add(new MemberValuePair("name", new StringLiteralExpr(columnInfo.getColumnComment())));
                        normalAnnotationExpr.setPairs(memberValuePairs);
                        annotationExprs.add(normalAnnotationExpr);
                    }
                }
                fieldDeclaration.setAnnotations(annotationExprs);
            }
            fieldDeclaration.setJavadocComment(columnInfo.getColumnComment());
            compilationUnit.getTypes().getLast().get().addMember(fieldDeclaration);
        }
        String modifiedCode = prettyPrinter.print(compilationUnit);
        Path path = compilationUnit.getStorage().get().getPath();
        getLog().info("修改文件:" + path.getFileName());
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path.toFile(), false), StandardCharsets.UTF_8));
        try {
            bufferedWriter.write(modifiedCode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                bufferedWriter.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public void modifyXml(String inputXmlPath, List<ColumnInfo> columnInfoList) throws Exception {
        // 加载XML文件
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        FileInputStream fileInputStream = new FileInputStream(inputXmlPath);
        Document document = builder.parse(fileInputStream);
        DocumentType doctype = document.getDoctype();
        // 修改XML内容
        Element rootElement = document.getDocumentElement();
        Node resultMap = rootElement.getElementsByTagName("resultMap").item(0);

        for (ColumnInfo columnInfo : columnInfoList) {
            Element element = document.createElement("result");
            element.setAttribute("column", columnInfo.getColumnName());
            element.setAttribute("property", columnInfo.getJavaName());
            if (columnInfo.getDataType().equals("json")) {
                element.setAttribute("typeHandler", "com.ruoyi.perf.common.Fastjson2TypeHandler");
            } else {
                element.setAttribute("jdbcType", columnInfo.getJdbcType());
            }

            resultMap.appendChild(element);
        }

        // 输出修改后的XML文件
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        // transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");

        transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, doctype.getPublicId());
        transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype.getSystemId());

        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(Files.newOutputStream(new File(inputXmlPath).toPath()));
        transformer.transform(source, result);
        // 关闭流
        fileInputStream.close();
        getLog().info("修改文件:" + inputXmlPath);
    }


}
