package com.axamit.springboot.demo.demorestservice.database;

import org.springframework.data.relational.core.mapping.NamingStrategy;
import org.springframework.data.relational.core.mapping.PersistentPropertyPathExtension;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class CustomPhysicalNamingStrategy implements NamingStrategy {

    /*
    @Override
    public String getSchema() {
        return "";
    }

    @Override
    public String getTableName(Class<?> type) {
        return join(splitAndReplace(type.getSimpleName()));
    }

    @Override
    public String getColumnName(RelationalPersistentProperty property) {
        return join(splitAndReplace(property.getName()));
    }

    @Override
    public String getReverseColumnName(RelationalPersistentProperty property) {
        return null;
    }

    @Override
    public String getReverseColumnName(PersistentPropertyPathExtension path) {
        return null;
    }

    @Override
    public String getKeyColumn(RelationalPersistentProperty property) {
        return join(splitAndReplace(property.getName()));
    }

    private List<String> splitAndReplace(String name) {
        List<String> result = new LinkedList<>();
        for ( String part : splitCamelCaseString( name ) ) {
            if ( part == null || part.trim().isEmpty() ) {
                continue;
            }
            result.add( part.toLowerCase(Locale.ROOT) );
        }
        return result;
    }

    public static List<String> splitCamelCaseString(String s){
        List<String> result = Arrays.asList(s.split("(?<=\\p{Ll})(?=\\p{Lu})") );
        return result;
    }

    private String join(List<String> parts) {
        boolean firstPass = true;
        String separator = "";
        StringBuilder joined = new StringBuilder();
        for (String part : parts) {
            joined.append(separator).append(part);
            if (firstPass) {
                firstPass = false;
                separator = "_";
            }
        }
        return joined.toString();
    }
*/
}