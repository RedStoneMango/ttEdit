package io.github.redstonemango.ttedit.front.scriptEditor;

import org.jetbrains.annotations.Nullable;

public abstract class AbstractScriptActionElement extends AbstractScriptElement {

    public AbstractScriptActionElement(boolean preview, @Nullable AbstractScriptElement parent, boolean isHead,
                                       ScriptElementMeta meta) {
        super(preview, parent, isHead, meta);
    }
}
