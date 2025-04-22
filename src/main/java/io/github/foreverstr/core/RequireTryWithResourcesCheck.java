package io.github.foreverstr.core;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

public class RequireTryWithResourcesCheck extends AbstractCheck {
    @Override
    public int[] getDefaultTokens() {
        return new int[]{TokenTypes.LITERAL_NEW};
    }

    @Override
    public int[] getAcceptableTokens() {
        return getDefaultTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return getDefaultTokens();
    }

    @Override
    public void visitToken(DetailAST ast) {
        System.out.println("=== 检测到 new 表达式 ===");
        printAST(ast, 0); // 打印AST结构

        String className = getClassName(ast);
        System.out.println("解析类名: " + className);

        boolean isAnnotated = isAnnotatedWithRequire(ast);
        System.out.println("类是否被注解: " + isAnnotated);

        boolean inTry = isInTryWithResources(ast);
        System.out.println("是否在try-with-resources中: " + inTry);

        if (isAnnotated && !inTry) {
            log(ast, "必须使用 try-with-resources 管理: " + className);
        }
    }

    private void printAST(DetailAST node, int indent) {
        System.out.printf("%s%s (%d)%n", "  ".repeat(indent), TokenUtil.getTokenName(node.getType()), node.getType());
        DetailAST child = node.getFirstChild();
        while (child != null) {
            printAST(child, indent + 1);
            child = child.getNextSibling();
        }
    }
    private boolean isAnnotatedWithRequire(DetailAST ast) {
        // 找到当前类的定义节点
        DetailAST classDef = getContainingClass(ast);
        if (classDef == null) return false;

        // 检查当前类及其父类是否带有注解
        return hasAnnotationInHierarchy(classDef);
    }

    private DetailAST getContainingClass(DetailAST node) {
        while (node != null && node.getType() != TokenTypes.CLASS_DEF) {
            node = node.getParent();
        }
        return node;
    }

    private boolean hasAnnotationInHierarchy(DetailAST classDef) {
        // 检查当前类
        if (hasAnnotation(classDef, "RequireTryWithResources")) {
            return true;
        }

        // 递归检查父类
        DetailAST superClass = getSuperClass(classDef);
        if (superClass != null) {
            String superClassName = resolveSuperClassName(superClass);
            return isAnnotatedSuperClass(superClassName);
        }
        return false;
    }

    private DetailAST getSuperClass(DetailAST classDef) {
        return null;
    }

    private boolean hasAnnotation(DetailAST classDef, String annotationName) {
        DetailAST modifiers = classDef.findFirstToken(TokenTypes.MODIFIERS);
        if (modifiers == null) return false;

        for (DetailAST child = modifiers.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getType() == TokenTypes.ANNOTATION) {
                DetailAST ident = child.getFirstChild().getFirstChild(); // 处理 @org.example.RequireTryWithResources
                if (ident != null && annotationName.equals(ident.getText())) {
                    return true;
                }
            }
        }
        return false;
    }
    private boolean isAnnotatedSuperClass(String superClassName) {
        try {
            // 在AST中找到父类定义
            DetailAST superClassDef = findClassDefinition(superClassName);
            return superClassDef != null && hasAnnotationInHierarchy(superClassDef);
        } catch (Exception e) {
            return false;
        }
    }

    private DetailAST findClassDefinition(String superClassName) {
        return null;
    }

    // 简化的父类名解析（需根据项目结构完善）
    private String resolveSuperClassName(DetailAST superClassNode) {
        return superClassNode.getText(); // 假设父类名直接可用，实际需处理全限定名
    }

    private boolean isInTryWithResources(DetailAST ast) {
        DetailAST current = ast;
        while (current != null) {
            if (current.getType() == TokenTypes.LITERAL_TRY) {
                DetailAST resources = current.findFirstToken(TokenTypes.RESOURCES);
                if (resources != null) {
                    // 检查所有资源声明
                    DetailAST resource = resources.getFirstChild();
                    while (resource != null) {
                        if (resource.getType() == TokenTypes.RESOURCE) {
                            // 检查资源变量初始化表达式是否包含当前new语句
                            DetailAST varDef = resource.findFirstToken(TokenTypes.VARIABLE_DEF);
                            DetailAST assign = varDef.findFirstToken(TokenTypes.ASSIGN);
                            if (assign != null && assign.getFirstChild().equals(ast)) {
                                return true;
                            }
                        }
                        resource = resource.getNextSibling();
                    }
                }
            }
            current = current.getParent();
        }
        return false;
    }

    private String getClassName(DetailAST newAst) {
        DetailAST typeNode = newAst.findFirstToken(TokenTypes.TYPE);
        if (typeNode == null) return "UNKNOWN";

        return resolveTypeName(typeNode.getFirstChild());
    }

    private String resolveTypeName(DetailAST node) {
        if (node.getType() == TokenTypes.DOT) {
            return resolveTypeName(node.getFirstChild()) + "." + node.getLastChild().getText();
        } else if (node.getType() == TokenTypes.IDENT) {
            return node.getText();
        }
        return "UNRESOLVED";
    }

    private void processDotNode(DetailAST dotNode, StringBuilder builder) {
        if (dotNode.getFirstChild().getType() == TokenTypes.DOT) {
            processDotNode(dotNode.getFirstChild(), builder);
        } else {
            builder.append(dotNode.getFirstChild().getText()).append('.');
        }
        builder.append(dotNode.getLastChild().getText()).append('.');
    }
}
