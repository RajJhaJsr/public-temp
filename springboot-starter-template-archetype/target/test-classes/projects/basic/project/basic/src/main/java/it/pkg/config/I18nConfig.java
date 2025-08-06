package it.pkg.config;

import it.pkg.interceptor.webserver.LocaleResolutionFilter;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.server.i18n.FixedLocaleContextResolver;
import org.springframework.web.server.i18n.LocaleContextResolver;

import java.util.Locale;

@Configuration
public class I18nConfig {
    
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages/messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(3600); // Cache for 1 hour
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setDefaultLocale(Locale.ENGLISH);
        return messageSource;
    }
    
    @Bean
    public LocaleContextResolver localeContextResolver() {
        FixedLocaleContextResolver localeResolver = new FixedLocaleContextResolver(Locale.ENGLISH);
        return localeResolver;
    }
    
    // Custom WebFilter for locale resolution in reactive apps
    @Bean
    public LocaleResolutionFilter localeResolutionWebFilter() {
        return new LocaleResolutionFilter();
    }
}