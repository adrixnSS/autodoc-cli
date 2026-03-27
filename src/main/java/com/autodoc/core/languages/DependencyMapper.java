package com.autodoc.core.languages;

import java.util.HashMap;
import java.util.Map;

public class DependencyMapper {
    private final Map<String, String> classSignatures;

    public DependencyMapper() {
        this.classSignatures = new HashMap<>();
    }

    public void registerClass(String className, String signature) {
        this.classSignatures.put(className, signature);
    }

    public String getSignature(String className) {
        return this.classSignatures.get(className);
    }

    public Map<String, String> getAllSignatures() {
        return classSignatures;
    }
    
    public boolean contains(String className) {
        return classSignatures.containsKey(className);
    }
}
