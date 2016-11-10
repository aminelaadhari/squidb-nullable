package com.aminelaadhari.squidb.nullable;

import com.yahoo.squidb.processor.data.ModelSpec;
import com.yahoo.squidb.processor.plugins.PluginBundle;
import com.yahoo.squidb.processor.plugins.PluginEnvironment;

import java.util.Arrays;

public class JsrPlugin extends PluginBundle {
    public JsrPlugin(ModelSpec<?> modelSpec, PluginEnvironment pluginEnv) {
        super(modelSpec, pluginEnv, Arrays.asList(new PropertyAnnotationPlugin(modelSpec, pluginEnv),
                new ModelMethodPlugin(modelSpec, pluginEnv)));
    }
}
