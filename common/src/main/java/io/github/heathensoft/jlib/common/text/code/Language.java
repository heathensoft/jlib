package io.github.heathensoft.jlib.common.text.code;

import io.github.heathensoft.jlib.common.text.Ascii;

/**
 * @author Frederik Dahl
 * 16/05/2024
 */


public enum Language {

    C, JAVA, GLSL;

    private static Ascii.Trie c_keywords;
    private static Ascii.Trie c_datatypes;
    private static Ascii.Trie java_keywords;
    private static Ascii.Trie java_datatypes;
    private static Ascii.Trie glsl_keywords;
    private static Ascii.Trie glsl_datatypes;


    public synchronized Ascii.Trie keywords() {
        Ascii.Trie keywords = null;
        switch (this) {
            case C -> {
                if (c_keywords == null) {
                    c_keywords = new Ascii.Trie();
                    String[] arr = c_keywords();
                    for (String str : arr) {
                        boolean inserted = c_keywords.tryInsert(str);
                    }
                } keywords = c_keywords;
            }
            case JAVA -> {
                if (java_keywords == null) {
                    java_keywords = new Ascii.Trie();
                    String[] arr = java_keywords();
                    for (String str : arr) {
                        boolean inserted = java_keywords.tryInsert(str);
                    }
                } keywords = java_keywords;
            }
            case GLSL -> {
                if (glsl_keywords == null) {
                    glsl_keywords = new Ascii.Trie();
                    String[] arr = glsl_keywords();
                    for (String str : arr) {
                        boolean inserted = glsl_keywords.tryInsert(str);
                    }
                } keywords = glsl_keywords;
            }
        } return keywords;
    }

    public synchronized Ascii.Trie datatypes() {
        Ascii.Trie datatypes = null;
        switch (this) {
            case C -> {
                if (c_datatypes == null) {
                    c_datatypes = new Ascii.Trie();
                    String[] arr = c_datatypes();
                    for (String str : arr) {
                        boolean inserted = c_datatypes.tryInsert(str);
                    }
                } datatypes = c_datatypes;
            }
            case JAVA -> {
                if (java_datatypes == null) {
                    java_datatypes = new Ascii.Trie();
                    String[] arr = java_datatypes();
                    for (String str : arr) {
                        boolean inserted = java_datatypes.tryInsert(str);
                    }
                } datatypes = java_datatypes;
            }
            case GLSL -> {
                if (glsl_datatypes == null) {
                    glsl_datatypes = new Ascii.Trie();
                    String[] arr = glsl_datatypes();
                    for (String str : arr) {
                        boolean inserted = glsl_datatypes.tryInsert(str);
                    }
                } datatypes = glsl_datatypes;
            }
        } return datatypes;
    }

    public static String[] java_datatypes() {
        return new String[] {"boolean","double","byte",
                "int","short","char","void","long","float"};
    }

    public static String[] java_keywords() {
        return new String[] { "abstract","continue","for","new","switch","assert",
                "default","goto","package","synchronized","do","if","private","this",
                "break","implements","protected","throw","else","import", "public",
                "throws","case","enum","instanceof","return","transient", "catch",
                "extends","try","final","interface","static","class","finally",
                "strictfp","volatile","native","super","while" };
    }

    public static String[] c_datatypes() {
        return new String[] {"bool","double","int","long","char","short","void","float",
                "uint16_t","int16_t","uint32_t","int32_t","int32_t","signed","unsigned"};
    }

    public static String[] c_keywords() {
        return new String[] { "auto","break","case","const","continue","default",
                "do","else","enum","extern","for","goto","if","register","return",
                "sizeof","static","struct","switch","typedef","union","volatile","while" };
    }

    public static String[] glsl_datatypes() {
        return new String[] {"boolean","double","byte",
                "int","short","char","void","long","float"};
    }

    public static String[] glsl_keywords() {
        return new String[] { "abstract","continue","for","new","switch","assert",
                "default","goto","package","synchronized","do","if","private","this",
                "break","implements","protected","throw","else","import", "public",
                "throws","case","enum","instanceof","return","transient", "catch",
                "extends","try","final","interface","static","class","finally",
                "strictfp","volatile","native","super","while" };
    }


}
