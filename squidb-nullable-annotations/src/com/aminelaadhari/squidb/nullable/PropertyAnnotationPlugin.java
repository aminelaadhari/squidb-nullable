package com.aminelaadhari.squidb.nullable;

import com.yahoo.aptutils.model.DeclaredTypeName;
import com.yahoo.aptutils.model.TypeName;
import com.yahoo.aptutils.writer.JavaFileWriter;
import com.yahoo.aptutils.writer.parameters.MethodDeclarationParameters;
import com.yahoo.squidb.processor.data.ModelSpec;
import com.yahoo.squidb.processor.plugins.Plugin;
import com.yahoo.squidb.processor.plugins.PluginEnvironment;
import com.yahoo.squidb.processor.plugins.defaults.properties.generators.PropertyGenerator;

import java.io.IOException;
import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PropertyAnnotationPlugin extends Plugin {
    public PropertyAnnotationPlugin(ModelSpec<?> modelSpec, PluginEnvironment pluginEnv) {
        super(modelSpec, pluginEnv);
    }

    @Override
    public void beforeEmitGetter(JavaFileWriter writer, PropertyGenerator propertyGenerator, MethodDeclarationParameters getterParams) throws IOException {
        if (propertyGenerator.getField() != null) {
            if (propertyGenerator.getField().getAnnotation(Nonnull.class) != null) {
                writer.writeAnnotation(new DeclaredTypeName(Nonnull.class.getName()));
            }
            if (propertyGenerator.getField().getAnnotation(Nullable.class) != null) {
                writer.writeAnnotation(new DeclaredTypeName(Nullable.class.getName()));
            }
        }
    }

    @Override
    public void beforeEmitSetter(JavaFileWriter writer, PropertyGenerator propertyGenerator, MethodDeclarationParameters setterParams) throws IOException {
        if (propertyGenerator.getField() != null) {
            if (propertyGenerator.getField().getAnnotation(Nonnull.class) != null) {
                TypeName typeName = setterParams.getArgumentTypes().get(0);
                if (typeName instanceof DeclaredTypeName) {
                    AnnotatedDeclaredTypeName annotatedDeclaredTypeName = new AnnotatedDeclaredTypeName(((DeclaredTypeName) typeName), Nonnull.class);
                    setterParams.setArgumentTypes(Arrays.asList(annotatedDeclaredTypeName));
                }
            }
            if (propertyGenerator.getField().getAnnotation(Nullable.class) != null) {
                TypeName typeName = setterParams.getArgumentTypes().get(0);
                if (typeName instanceof DeclaredTypeName) {
                    AnnotatedDeclaredTypeName annotatedDeclaredTypeName = new AnnotatedDeclaredTypeName(((DeclaredTypeName) typeName), Nullable.class);
                    setterParams.setArgumentTypes(Arrays.asList(annotatedDeclaredTypeName));
                }
            }
        }
    }
}
