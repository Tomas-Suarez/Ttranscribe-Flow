package com.dev.transcribeflow.settings;

import lombok.Getter;

@Getter
public enum Language {
    EN("English", "en-US", "en"),
    ES("Spanish", "es-ES", "es"),
    JA("Japanese", "ja-JP", "ja"),
    FR("French", "fr-FR", "fr"),
    DE("German", "de-DE", "de"),
    PT("Portuguese", "pt-BR", "pt");

    private final String displayName;
    private final String isoCode;
    private final String shortCode;

    Language(String displayName, String isoCode, String shortCode) {
        this.displayName = displayName;
        this.isoCode = isoCode;
        this.shortCode = shortCode;
    }

    public static Language fromIsoCode(String isoCode) {
        for (Language lang : Language.values()) {
            if (lang.isoCode.equalsIgnoreCase(isoCode) || lang.shortCode.equalsIgnoreCase(isoCode)) {
                return lang;
            }
        }
        return EN;
    }

    @Override
    public String toString() {
        return displayName + " (" + shortCode + ")";
    }
}