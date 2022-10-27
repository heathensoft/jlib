package io.github.heathensoft.jlib.common.io;


import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Frederik Dahl
 * 11/10/2022
 */


public class Settings {
 
    private static final String BOOL_TRUE = "y";
    private static final String BOOL_FALSE = "n";
    
    private final File file;
    private final Properties table;
    
    
    public Settings(File file) {
        this.table = new Properties();
        this.file = file;
    }
    
    public void load() throws IOException {
        try(FileReader reader = new FileReader(file.path().toString())) {
            table.load(reader);
        }
    }
    
    public void save() throws IOException {
        try(FileWriter writer = new FileWriter(file.path().toString())) {
            table.store(writer,null);
        }
    }
    
    public void validate() throws IOException {
        validate(false);
    }
    
    public void validate(boolean replace) throws IOException {
        file.validate(replace);
    }
    
    public Properties table() {
        return table;
    }
    
    public String getString(String key) {
        return table.getProperty(key);
    }
    
    public String getString(String key, String defaultValue) {
        return table.getProperty(key, defaultValue);
    }
    
    public Boolean getBool(String key) {
        String setting = table.getProperty(key);
        if (setting.equals(BOOL_TRUE)) return true;
        if (setting.equals(BOOL_FALSE)) return false;
        return null;
    }
    
    public Boolean getBool(String key, boolean defaultValue) {
        String defaultString = defaultValue ? BOOL_TRUE : BOOL_FALSE;
        String setting = table.getProperty(key,defaultString);
        if (setting.equals(BOOL_TRUE)) return true;
        if (setting.equals(BOOL_FALSE)) return false;
        return defaultValue;
    }
    
    public Integer getInt(String key) {
        String setting = table.getProperty(key);
        try { return Integer.parseInt(setting);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public Integer getInt(String key, int defaultValue) {
        String defaultString = Integer.toString(defaultValue);
        String setting = table.getProperty(key,defaultString);
        try { return Integer.parseInt(setting);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    public Float getFloat(String key) {
        String setting = table.getProperty(key);
        try { return Float.parseFloat(setting);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public Float getFloat(String key, float defaultValue) {
        String defaultString = Float.toString(defaultValue);
        String setting = table.getProperty(key,defaultString);
        try { return Float.parseFloat(setting);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    public String setString(String key, String value) {
        Object prev = table.setProperty(key, value);
        if (prev instanceof String) return (String) prev;
        return null;
    }
    
    public Boolean setBool(String key, boolean value) {
        Object prev = table.setProperty(key, value ? BOOL_TRUE : BOOL_FALSE);
        if (prev instanceof String prevString) {
            if (prevString.equals(BOOL_TRUE)) return true;
            if (prevString.equals(BOOL_FALSE)) return false;
        } return null;
    }
    
    public Integer setInt(String key, int value) {
        Object prev = table.setProperty(key, Integer.toString(value));
        if (prev instanceof String prevString) {
            try { return Integer.parseInt(prevString);
            } catch (NumberFormatException ignored) { }
        } return null;
    }
    
    public Float setFloat(String key, float value) {
        Object prev = table.setProperty(key, Float.toString(value));
        if (prev instanceof String prevString) {
            try { return Float.parseFloat(prevString);
            } catch (NumberFormatException ignored) { }
        } return null;
    }
    
}
