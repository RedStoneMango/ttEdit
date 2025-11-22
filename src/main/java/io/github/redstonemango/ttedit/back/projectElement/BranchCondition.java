package io.github.redstonemango.ttedit.back.projectElement;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BranchCondition {

    private Type type;
    private Comparison comparison;
    private String argA;
    private String argB;

    public BranchCondition() {}

    public void initializeFields() {
        if (type == null) {
            type = Type.STATIC;
        }
        if (comparison == null) {
            comparison = Comparison.EQUAL;
        }
        if (argA == null) {
            argA = "";
        }
        if (argB == null) {
            argB = "";
        }
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Comparison getComparison() {
        return comparison;
    }

    public void setComparison(Comparison comparison) {
        this.comparison = comparison;
    }

    public String getArgA() {
        return argA;
    }

    public void setArgA(String argA) {
        this.argA = argA;
    }

    public String getArgB() {
        return argB;
    }

    public void setArgB(String argB) {
        this.argB = argB;
    }

    public enum Type { STATIC, DYNAMIC }
    public enum Comparison {
        EQUAL("equal to"),
        UNEQUAL("not equal to"),
        MORE("more than"),
        MORE_EQUAL("more or equal to"),
        LESS("less than"),
        LESS_EQUAL("less or equal to");

        private final String literal;

        Comparison(String literal) {
            this.literal = literal;
        }
        public String getLiteral() {
            return literal;
        }
    }

}
