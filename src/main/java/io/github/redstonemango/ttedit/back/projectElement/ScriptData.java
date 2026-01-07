package io.github.redstonemango.ttedit.back.projectElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScriptData {

    public static final Pattern REGISTER_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*");
    public static final Pattern REGISTER_EXCLUDE_CHARS_PATTERN = Pattern.compile("[^a-zA-Z0-9_]");

    private Type type;

    // HEAD ELEMENT
    private @Nullable List<BranchCondition> conditions;
    private @Nullable List<ScriptData> actions;

    // PLAY ELEMENT
    private @Nullable Set<String> sounds;

    // JUMP ELEMENT
    private @Nullable String jumpTarget;

    // REGISTER, NEGATE & TIME ELEMENT
    private @Nullable String register;

    // ONLY REGISTER ELEMENT (also needs '@Nullable String register')
    private @Nullable Action action;
    private @Nullable String value;

    // ONLY TIME ELEMENT (also needs '@Nullable String register')
    private @Nullable String modulo;

    public ScriptData() {}

    public void initializeFields() throws ProjectLoadException {
        if (type == null) {
            // Fallback to detection
            if (conditions != null) type = Type.HEAD;
            else if (sounds != null) type = Type.PLAY;
            else if (jumpTarget != null) type = Type.JUMP;
            else if (register != null) type = Type.NEGATE;
            else throw new ProjectLoadException("No script type supplied! Automated type detection failed.");
        }

        // Ugly
        switch (type) {
            case HEAD -> {
                sounds = null;
                jumpTarget = null;
                register = null;
                action = null;
                value = null;
                if (conditions == null) conditions = new ArrayList<>();
                for (BranchCondition condition : conditions) {
                    condition.initializeFields();
                }
                if (actions == null) actions = new ArrayList<>();
                for (ScriptData action : actions) {
                    action.initializeFields();
                }
            }
            case PLAY -> {
                jumpTarget = null;
                conditions = null;
                actions = null;
                register = null;
                action = null;
                value = null;
                modulo = null;
                if (sounds == null) sounds = new HashSet<>();
            }
            case JUMP -> {
                conditions = null;
                sounds = null;
                actions = null;
                register = null;
                action = null;
                value = null;
                modulo = null;
                if (jumpTarget == null) jumpTarget = "";
            }
            case REGISTER -> {
                conditions = null;
                sounds = null;
                actions = null;
                jumpTarget = null;
                modulo = null;
                if (register == null) register = "";
                if (action == null) action = Action.SET;
                if (value == null) value = "";
            }
            case NEGATE -> {
                conditions = null;
                sounds = null;
                actions = null;
                jumpTarget = null;
                action = null;
                value = null;
                modulo = null;
                if (register == null) register = "";
            }
            case TIME -> {
                conditions = null;
                sounds = null;
                actions = null;
                jumpTarget = null;
                action = null;
                value = null;
                if (register == null) register = "";
                if (modulo == null) modulo = "";
            }
        }
    }
    public void loadRegisters(Set<String> registerSet) {
        switch (type) {
            case REGISTER -> {
                if (matchesRegisterPattern(register)) registerSet.add(register);
                if (matchesRegisterPattern(value)) registerSet.add(value);
            }
            case NEGATE -> {
                if (matchesRegisterPattern(register)) registerSet.add(register);
            }
            case HEAD -> {
                for (BranchCondition condition : conditions) {
                    if (matchesRegisterPattern(condition.getArgA())) {
                        registerSet.add(condition.getArgA());
                    }
                    if (matchesRegisterPattern(condition.getArgB())) {
                        registerSet.add(condition.getArgB());
                    }
                }
                for (ScriptData action : actions) {
                    action.loadRegisters(registerSet);
                }
            }
        }
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public @Nullable List<ScriptData> getActions() {
        return actions;
    }

    public void setActions(@Nullable List<ScriptData> actions) {
        this.actions = actions;
    }

    public @Nullable List<BranchCondition> getConditions() {
        return conditions;
    }

    public void setConditions(@Nullable List<BranchCondition> conditions) {
        this.conditions = conditions;
    }

    public @Nullable Set<String> getSounds() {
        return sounds;
    }

    public void setSounds(@Nullable Set<String> sounds) {
        this.sounds = sounds;
    }

    public @Nullable String getJumpTarget() {
        return jumpTarget;
    }

    public void setJumpTarget(@Nullable String jumpTarget) {
        this.jumpTarget = jumpTarget;
    }

    public @Nullable String getRegister() {
        return register;
    }

    public void setRegister(@Nullable String register) {
        this.register = register;
    }

    public @Nullable Action getAction() {
        return action;
    }

    public void setAction(@Nullable Action action) {
        this.action = action;
    }

    public @Nullable String getValue() {
        return value;
    }

    public void setValue(@Nullable String value) {
        this.value = value;
    }

    public @Nullable String getModulo() {
        return modulo;
    }

    public void setModulo(@Nullable String modulo) {
        this.modulo = modulo;
    }

    public static boolean matchesRegisterPattern(String s) {
        return REGISTER_PATTERN.matcher(s).matches();
    }

    public static String forceRegisterPattern(String s) {
        if (matchesRegisterPattern(s)) return s;
        s = REGISTER_EXCLUDE_CHARS_PATTERN.matcher(s).replaceAll("");
        if (!matchesRegisterPattern(s)) s = s.isBlank() ? "reg" : "reg_" + s;
        return s;
    }


    public enum Type { HEAD, PLAY, JUMP, REGISTER, NEGATE, TIME }
    public enum Action {
        SET("value"),
        ADDITION("itself plus"),
        SUBSTRACTION("itself minus"),
        MULTIPLICATION("itself times"),
        DIVISION("itself divided by"),
        MODULO("itself modulo"),
        OR("itself bitwise-OR with"),
        AND("itself bitwise-AND with"),
        XOR("itself bitwise-XOR with");

        private final String literal;

        Action(String literal) {
            this.literal = literal;
        }
        public String getLiteral() {
            return literal;
        }
    }
}
