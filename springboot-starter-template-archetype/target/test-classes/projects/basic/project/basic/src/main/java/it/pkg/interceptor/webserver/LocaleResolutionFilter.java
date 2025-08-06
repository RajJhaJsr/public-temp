package it.pkg.interceptor.webserver;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class LocaleResolutionFilter implements WebFilter {
    
    private static final String LOCALE_ATTRIBUTE = "LOCALE";
    private final Set<Locale> supportedLocales = Set.of(
        Locale.ENGLISH,
        Locale.FRENCH, 
        Locale.GERMAN,
        new Locale("es"),
        new Locale("zh")
    );
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Locale resolvedLocale = resolveLocale(exchange);
        exchange.getAttributes().put(LOCALE_ATTRIBUTE, resolvedLocale);
        return chain.filter(exchange);
    }
    
    private Locale resolveLocale(ServerWebExchange exchange) {
        // 1. Check query parameter first
        String langParam = exchange.getRequest().getQueryParams().getFirst("lang");
        if (langParam != null && !langParam.isEmpty()) {
            Locale locale = parseLocale(langParam);
            if (isSupported(locale)) {
                return locale;
            }
        }
        
        // 2. Check Accept-Language header
        List<String> acceptLanguageHeaders = exchange.getRequest().getHeaders().get("Accept-Language");
        if (acceptLanguageHeaders != null && !acceptLanguageHeaders.isEmpty()) {
            String acceptLanguage = acceptLanguageHeaders.get(0);
            Locale locale = parseAcceptLanguageHeader(acceptLanguage);
            if (isSupported(locale)) {
                return locale;
            }
        }
        
        // 3. Default to English
        return Locale.ENGLISH;
    }
    
    private Locale parseLocale(String lang) {
        try {
            return Locale.forLanguageTag(lang);
        } catch (Exception e) {
            return Locale.ENGLISH;
        }
    }
    
    private Locale parseAcceptLanguageHeader(String acceptLanguage) {
        try {
            // Parse the first locale from Accept-Language header
            // Format: "en-US,en;q=0.9,fr;q=0.8"
            String firstLocale = acceptLanguage.split(",")[0].split(";")[0].trim();
            return Locale.forLanguageTag(firstLocale);
        } catch (Exception e) {
            return Locale.ENGLISH;
        }
    }
    
    private boolean isSupported(Locale locale) {
        return supportedLocales.contains(locale) || 
               supportedLocales.stream().anyMatch(supported -> 
                   supported.getLanguage().equals(locale.getLanguage()));
    }
    
    public static Locale getLocaleFromExchange(ServerWebExchange exchange) {
        return (Locale) exchange.getAttributes().getOrDefault(LOCALE_ATTRIBUTE, Locale.ENGLISH);
    }
}