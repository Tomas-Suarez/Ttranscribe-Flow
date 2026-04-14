package com.dev.transcribeflow.core.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageUtils {

    private final MessageSource messageSource;

    /**
     * * Retrieves a localized message from the resource bundle.
     * @param code The key in the .properties
     * @param args Dynamic arguments to fill placeholders in the message (optional).
     * @return The translated message based on the current Locale
     */
    public String getMessage(String code, Object... args){
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
}
