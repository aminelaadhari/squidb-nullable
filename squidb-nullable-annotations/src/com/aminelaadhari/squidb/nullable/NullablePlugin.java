package com.aminelaadhari.squidb.nullable;

import com.yahoo.squidb.processor.data.ModelSpec;
import com.yahoo.squidb.processor.plugins.Plugin;
import com.yahoo.squidb.processor.plugins.PluginEnvironment;

public class NullablePlugin extends Plugin {
    @Nonnull boolean isNullable;
    boolean isNonnull;

    public NullablePlugin(ModelSpec<?>  modelSpec, PluginEnvironment pluginEnv) {
        super(modelSpec, pluginEnv);
    }
}
