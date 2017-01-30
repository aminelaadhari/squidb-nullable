/*
 * Copyright 2015, Yahoo Inc.
 * Copyrights licensed under the Apache 2.0 License.
 * See the accompanying LICENSE file for terms.
 */
package com.aminelaadhari.squidb.nullable;

import com.yahoo.aptutils.model.CoreTypes;
import com.yahoo.aptutils.model.DeclaredTypeName;
import com.yahoo.aptutils.utils.AptUtils;
import com.yahoo.aptutils.writer.JavaFileWriter;
import com.yahoo.aptutils.writer.expressions.Expression;
import com.yahoo.aptutils.writer.expressions.Expressions;
import com.yahoo.aptutils.writer.parameters.MethodDeclarationParameters;
import com.yahoo.squidb.annotations.ModelMethod;
import com.yahoo.squidb.processor.TypeConstants;
import com.yahoo.squidb.processor.data.ModelSpec;
import com.yahoo.squidb.processor.plugins.Plugin;
import com.yahoo.squidb.processor.plugins.PluginEnvironment;
import com.yahoo.squidb.processor.plugins.defaults.JavadocPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 * A {@link Plugin} that controls copying public static methods or methods annotated with {@link ModelMethod}
 * to the generated model. It is enabled by default but can be disabled by passing
 * {@link PluginEnvironment#OPTIONS_DISABLE_DEFAULT_METHOD_HANDLING 'disableModelMethod'} as one of the
 * values for the 'squidbOptions' key.
 */
public class ModelMethodPlugin extends Plugin {

    private final List<ExecutableElement> modelMethods = new ArrayList<>();
    private final List<ExecutableElement> staticModelMethods = new ArrayList<>();

    public ModelMethodPlugin(ModelSpec<?> modelSpec, PluginEnvironment pluginEnv) {
        super(modelSpec, pluginEnv);
        parseModelMethods();
    }

    @Override
    public void addRequiredImports(Set<DeclaredTypeName> imports) {
        utils.accumulateImportsFromElements(imports, modelMethods);
        utils.accumulateImportsFromElements(imports, staticModelMethods);
    }

    @Override
    public void emitMethods(JavaFileWriter writer) throws IOException {
        for (ExecutableElement e : modelMethods) {
            emitModelMethod(writer, e, Modifier.PUBLIC);
        }
        for (ExecutableElement e : staticModelMethods) {
            emitModelMethod(writer, e, Modifier.PUBLIC, Modifier.STATIC);
        }
    }

    private void emitModelMethod(JavaFileWriter writer, ExecutableElement e, Modifier... modifiers)
            throws IOException {
        MethodDeclarationParameters params = utils.methodDeclarationParamsFromExecutableElement(e, modifiers);

        ModelMethod methodAnnotation = e.getAnnotation(ModelMethod.class);
        List<Object> arguments = new ArrayList<>();
        if (methodAnnotation != null) {
            String name = methodAnnotation.name();
            if (!AptUtils.isEmpty(name)) {
                params.setMethodName(name);
            }
            params.getArgumentTypes().remove(0);
            params.getArgumentNames().remove(0);
            arguments.add(0, "this");
        }
        arguments.addAll(params.getArgumentNames());
        Expression methodCall = Expressions.staticMethod(modelSpec.getModelSpecName(),
                e.getSimpleName().toString(), arguments);
        if (!CoreTypes.VOID.equals(params.getReturnType())) {
            methodCall = methodCall.returnExpr();
        }
        JavadocPlugin.writeJavadocFromElement(pluginEnv, writer, e);
        if (e.getAnnotation(Nullable.class) != null) {
            writer.writeAnnotation(new DeclaredTypeName(Nullable.class.getName()));
        }
        if (e.getAnnotation(Nonnull.class) != null) {
            writer.writeAnnotation(new DeclaredTypeName(Nonnull.class.getName()));
        }

//        List<String> argumentNames = params.getArgumentNames();
//        List<? extends TypeName> argumentTypes = params.getArgumentTypes();
//        List<TypeName> types = new ArrayList<>(argumentTypes.size());
//        for (int i = 0; i < argumentNames.size(); i++) {
//            for (VariableElement variableElement : e.getParameters()) {
//                if (argumentNames.get(i).equals(variableElement.getSimpleName().toString())) {
//                    TypeName argumentType = argumentTypes.get(i);
//                    if (argumentType instanceof DeclaredTypeName) {
//                        if (variableElement.getAnnotation(Nullable.class) != null) {
//                            types.add(i, new AnnotatedDeclaredTypeName((DeclaredTypeName) argumentType, Nullable.class));
//                        }
//
//                        if (variableElement.getAnnotation(Nonnull.class) != null) {
//                            types.add(i, new AnnotatedDeclaredTypeName((DeclaredTypeName) argumentType, Nonnull.class));
//                        }
//                    } else {
//                        types.add(argumentType);
//                    }
//                }
//            }
//        }
//        params.setArgumentTypes(types);

        writer.beginMethodDefinition(params)
                .writeStatement(methodCall)
                .finishMethodDefinition();
    }

    private void parseModelMethods() {
        List<? extends Element> enclosedElements = modelSpec.getModelSpecElement().getEnclosedElements();
        for (Element e : enclosedElements) {
            if (e instanceof ExecutableElement) {
                checkExecutableElement((ExecutableElement) e, modelMethods, staticModelMethods,
                        modelSpec.getGeneratedClassName());
            }
        }
    }

    private void checkExecutableElement(ExecutableElement e, List<ExecutableElement> modelMethods,
                                        List<ExecutableElement> staticModelMethods, DeclaredTypeName modelClass) {
        Set<Modifier> modifiers = e.getModifiers();
        if (e.getKind() == ElementKind.CONSTRUCTOR) {
            // Don't copy constructors
            return;
        }
        if (!modifiers.contains(Modifier.STATIC)) {
            utils.getMessager().printMessage(Diagnostic.Kind.WARNING,
                    "Model spec objects should never be instantiated, so non-static methods are meaningless. " +
                            "Did you mean to make this a static method?", e);
            return;
        }
        ModelMethod methodAnnotation = e.getAnnotation(ModelMethod.class);
        // Private static methods may be unannotated if they are called by a public annotated method.
        // Don't assume error if method is private
        if (methodAnnotation == null) {
            if (modifiers.contains(Modifier.PUBLIC)) {
                staticModelMethods.add(e);
            } else if (!modifiers.contains(Modifier.PRIVATE)) {
                utils.getMessager().printMessage(Diagnostic.Kind.WARNING,
                        "This method will not be added to the model definition. " +
                                "Did you mean to annotate this method with @ModelMethod?", e);
            }
        } else {
            List<? extends VariableElement> params = e.getParameters();
            if (params.size() == 0) {
                modelSpec.logError("@ModelMethod methods must have an abstract model as their first argument", e);
            } else {
                VariableElement firstParam = params.get(0);
                TypeMirror paramType = firstParam.asType();
                if (!checkFirstArgType(paramType, modelClass)) {
                    modelSpec.logError("@ModelMethod methods must have an abstract model as their first argument", e);
                } else {
                    modelMethods.add(e);
                }
            }
        }
    }

    private boolean checkFirstArgType(TypeMirror type, DeclaredTypeName generatedClassName) {
        if (type instanceof ErrorType) {
            return true;
        }
        if (!(type instanceof DeclaredType)) {
            return false;
        }

        DeclaredTypeName typeName = (DeclaredTypeName) utils.getTypeNameFromTypeMirror(type);

        return typeName.equals(generatedClassName) || typeName.equals(TypeConstants.ABSTRACT_MODEL);
    }
}