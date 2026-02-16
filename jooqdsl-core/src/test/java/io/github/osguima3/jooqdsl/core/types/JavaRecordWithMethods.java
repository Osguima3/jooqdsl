package io.github.osguima3.jooqdsl.core.types;

public record JavaRecordWithMethods(String value) { 
    
    public String getOther() {
        return "other";
    }
}
