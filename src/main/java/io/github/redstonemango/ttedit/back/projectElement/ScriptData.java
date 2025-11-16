package io.github.redstonemango.ttedit.back.projectElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScriptData {

    private Type type;

    // HEAD ELEMENT
    private @Nullable List<BranchCondition> conditions;

    // PLAY ELEMENT
    private @Nullable String sound;

    // JUMP ELEMENT
    private @Nullable String jumpTarget;

    private @Nullable ScriptData child;

    public ScriptData() {}

    public void initializeFields() throws ProjectLoadException {
        if (type == null) {
            // Fallback to detection
            if (conditions != null) type = Type.HEAD;
            else if (sound != null) type = Type.PLAY;
            else if (jumpTarget != null) type = Type.JUMP;
            else throw new ProjectLoadException("No script type supplied! Automated type detection failed.");
        }

        switch (type) {
            case HEAD -> {
                sound = null;
                jumpTarget = null;
                if (conditions == null) conditions = new ArrayList<>();
                for (BranchCondition condition : conditions) {
                    condition.initializeFields();
                }
            }
            case PLAY -> {
                jumpTarget = null;
                conditions = null;
                if (sound == null) sound = "";
            }
            case JUMP -> {
                conditions = null;
                sound = null;
                if (jumpTarget == null) jumpTarget = "";
            }
        }
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public @Nullable ScriptData getChild() {
        return child;
    }

    public void setChild(@Nullable ScriptData child) {
        this.child = child;
    }

    public @Nullable List<BranchCondition> getConditions() {
        return conditions;
    }

    public void setConditions(@Nullable List<BranchCondition> conditions) {
        this.conditions = conditions;
    }

    public @Nullable String getSound() {
        return sound;
    }

    public void setSound(@Nullable String sound) {
        this.sound = sound;
    }

    public @Nullable String getJumpTarget() {
        return jumpTarget;
    }

    public void setJumpTarget(@Nullable String jumpTarget) {
        this.jumpTarget = jumpTarget;
    }

    public enum Type { HEAD, PLAY, JUMP}
}
