package com.autodoc.core.languages;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaScriptPruner implements LanguagePruner {
    @Override
    public String prune(String content) {
        // Regex simplificada para vaciar el cuerpo de funciones/métodos JS/TS
        // Busca patrones como: function name(args) { ... } o (args) => { ... } o class { method(args) { ... } }
        // Nota: Esta es una aproximación robusta para firmas de exportación
        String regex = "\\{([^{}]*|\\{[^{}]*\\})*\\}";
        Pattern pattern = Pattern.compile("(?<=\\)|=>|class [ \\w]+|constructor\\([ \\w,]*\\))\\s*" + regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        return matcher.replaceAll(" { /* [AUTODOC: Lógica interna podada para ahorro de tokens] */ }");
    }
}
