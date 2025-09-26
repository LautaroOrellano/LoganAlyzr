package com.loganalizyr.model;

public class KeywordCriteria {
    private boolean useRegex;
    private boolean useLiteral;
    private boolean caseSensitive;

    public KeywordCriteria(boolean useRegex, boolean useLiteral, boolean caseSensitive) {
        this.useRegex = useRegex;
        this.useLiteral = useLiteral;
        this.caseSensitive = caseSensitive;
    }

    public boolean isUseRegex() {
        return useRegex;
    }

    public void setUseRegex(boolean useRegex) {
        this.useRegex = useRegex;
    }

    public boolean isUseLiteral() {
        return useLiteral;
    }

    public void setUseLiteral(boolean useLiteral) {
        this.useLiteral = useLiteral;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }


}
