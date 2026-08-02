package org.example.agents.model;

import java.util.List;

public class UserPreferences {
    private String communicationStyle;
    private String language;
    private List<String> interests;

    public UserPreferences(String style, String lang, List<String> interests) {
        this.communicationStyle = style;
        this.language = lang;
        this.interests = interests;
    }

    public String getCommunicationStyle() {
        return communicationStyle;
    }

    public String getLanguage() {
        return language;
    }

    public List<String> getInterests() {
        return interests;
    }
}
