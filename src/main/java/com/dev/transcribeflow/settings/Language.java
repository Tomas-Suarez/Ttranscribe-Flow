package com.dev.transcribeflow.settings;

import lombok.Getter;

@Getter
public enum Language {
    EN("English", "en-US"),
    ES("Spanish", "es-ES"),
    JA("Japanese", "ja-JP"),
    FR("French", "fr-FR"),
    DE("German", "de-DE"),
    PT("Portuguese", "pt-BR");

    private final String displayName;
    private final String isoCode;

    Language(String displayName, String isoCode) {
        this.displayName = displayName;
        this.isoCode = isoCode;
    }

    public static Language fromIsoCode(String isoCode) {
        for (Language lang : Language.values()) {
            if (lang.isoCode.equalsIgnoreCase(isoCode)) {
                return lang;
            }
        }
        return EN;
    }

    @Override
    public String toString() {
        return displayName + " (" + isoCode + ")";
    }
}