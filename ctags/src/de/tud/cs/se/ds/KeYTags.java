package de.tud.cs.se.ds;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO
 * 
 * @author Dominic Scheurer
 */
public class KeYTags {
    
    private static final String RULE_SCOPE_UNSCOPED_LONG_NAME = "Unscoped Rules";
    private static final String RULE_SCOPE_KEYWORD = "\\rules";
    private static final String RULE_SCOPE_TLT = "c";
    private static final String RULE_TLT = "r";
    private static final String FUNCTIONS_KEYWORD = "\\functions";
    private static final String PREDICATES_KEYWORD = "\\predicates";
    private static final String SCHEMA_VARIABLES_TLT = "v";
    private static final String SCHEMA_VARIABLES_KEYWORD = "\\schemaVariables";
    private static final String SORTS_KEYWORD = "\\sorts";
    private static final String SORTS_TLT = "s";
    private static final String SEPARATOR = "\t";
    private static final String PREDICATES_TLT = "p";
    private static final String FUNCTIONS_TLT = "f";

    private static enum Scopes {
        TOP_LEVEL, SORTS, SORT, RULES, RULE, FUNCTIONS, FUNCTION, PREDICATES, PREDICATE, SCHEMA_VARIABLES, SCHEMA_VARIABLE, COMMENT
    }

    /* Expected call structure:
     * '/home/dscheurer/.vim/bundle/vim-key/ctags/KeYTags.jar' '-f' '-' '--format=2' '--excmd=pattern' '--fields=nksSaf' '--extra=' '--sort=no' '--append=no' FILE.key
     */
    public static void main(String[] args) {
        
        File keyFile = null;
        
        for (final String arg: args) {
            if (arg.startsWith("-")) {
                // By now, we ignore all arguments except
                // for the file name
            } else {
                // This should be the file name
                File tmp = new File(arg);
                if (arg.endsWith(".key") && tmp.exists()) {
                    keyFile = tmp;
                }
            }
        }
        
        if (keyFile == null) {
            System.err.println("Please supply the .key file to parse");
        }
        
        try {
            System.out.println(extractTagFileContent(keyFile));
        }
        catch (IOException e) {
            System.err.println("Error while reading file '" + keyFile.getAbsolutePath() + "':");
            System.err.println(e.getLocalizedMessage());
        }
    }
    
    private static String extractTagFileContent(File keyFile) throws IOException {
        final StringBuilder sb = new StringBuilder();
        final BufferedReader br = new BufferedReader(new FileReader(keyFile));
        
        String line;
        int lineNo = 0, indent = 0;
        Scopes scope = Scopes.TOP_LEVEL, scopeBeforeComment = Scopes.TOP_LEVEL;
        
        while ((line = br.readLine()) != null) {
            lineNo++;
            final String origLine = line;
            
            // Remove line comments
            line = line.replaceAll("//.*$", "").trim();
            
            if (line.isEmpty()) {
                continue;
            }
            
            // Split into parts
            line = line.replaceAll("\\{", "\\\n{\\\n");
            line = line.replaceAll("\\}\\s*;?", "\\\n}\\\n");
            line = line.replaceAll("/\\*", "\\\n/*\\\n");
            line = line.replaceAll("\\*/", "\\\n*/\\\n");
            final String[] parts = line.split("\n");
            
            for (String part : parts) {
                part = part.trim();
                
                if (part.isEmpty()) {
                    continue;
                }
                
                // For now, we ignore the "\lemma" directive.
                // See, for instance, seqRules.key for an example of the 
                // usage of \lemma.
                
                if (part.equals("/*")) {
                    scopeBeforeComment = scope;
                    scope = Scopes.COMMENT;
                    continue;
                }
                
                if (part.equals("*/")) {
                    if (scope != Scopes.COMMENT) {
                        System.err.println("Syntax error: Unbalanced multi-line comment at line " + lineNo);
                    }
                    
                    scope = scopeBeforeComment;
                    continue;
                }
                
                if (scope == Scopes.COMMENT) {
                    continue;
                }
                
                if (part.equals("{")) {
                    indent++;
                    continue;
                }

                if (part.equals("}")) {
                    indent--;
                    continue;
                }
                
                if (indent < 0) {
                    System.err.println("Syntax error: Wrong balancing of braces at line " + lineNo);
                    indent = 0; // We try to go on...
                    continue;
                }
                
                if (indent == 0) {
                    scope = Scopes.TOP_LEVEL;
                }
                
                // TOP-LEVEL DIRECTIVES //
                if (scope == Scopes.TOP_LEVEL) {
                    if (part.contains(SORTS_KEYWORD)) {
                        scope = Scopes.SORTS;
                        continue;
                    }
                    
                    if (part.contains(FUNCTIONS_KEYWORD)) {
                        scope = Scopes.FUNCTIONS;
                        continue;
                    }
                    
                    if (part.contains(PREDICATES_KEYWORD)) {
                        scope = Scopes.PREDICATES;
                        continue;
                    }
                    
                    if (part.contains(SCHEMA_VARIABLES_KEYWORD)) {
                        scope = Scopes.SCHEMA_VARIABLES;
                        continue;
                    }
                    
                    if (part.contains(RULE_SCOPE_KEYWORD)) {
                        String tagName = RULE_SCOPE_UNSCOPED_LONG_NAME;
                        final Matcher scopeMatcher = Pattern.compile(".*\\(([^\\)]+)\\).*").matcher(part);
                        if (scopeMatcher.matches()) {
                            tagName = scopeMatcher.group(1);
                        }
                        
                        sb.append(tagName).append(SEPARATOR)
                                .append(keyFile.getName()).append(SEPARATOR)
                                .append("/^").append(escape(origLine)).append("$/;\"")
                                .append(SEPARATOR).append(RULE_SCOPE_TLT).append(SEPARATOR)
                                .append("line:").append(lineNo).append("\n");
                        
                        scope = Scopes.RULES;
                        continue;
                    }
                }
                
                // SORT DECLARATIONS //
                if (scope == Scopes.SORT && indent == 1) {
                    scope = Scopes.SORTS;
                }
                
                if (scope == Scopes.SORTS && indent == 1) {
                    final String lineWoBreaks = line.replace("\n", "");
                    Matcher m = Pattern.compile("\\s*(?:(\\S+)\\s+)?(\\S+)\\s*(\\\\extends\\s*\\S+)?;\\s*$").matcher(lineWoBreaks);
                    
                    if (!m.matches()) {
                        System.err.println("Syntax error: Bad sort declaration: '" + lineWoBreaks + "' at line " + lineNo);
                    }

                          String sortType = m.group(1) != null ? 
                                  (" : " + m.group(1).trim() + (m.group(3) != null ?
                                          " " + m.group(3).trim() : "")) : "";
                    final String sortName = m.group(2);
                        
                    sb.append(sortName).append(sortType)
                            .append(SEPARATOR).append(keyFile.getName())
                            .append(SEPARATOR).append("/^")
                            .append(escape(origLine)).append("$/;\"")
                            .append(SEPARATOR).append(SORTS_TLT)
                            .append(SEPARATOR).append("line:")
                            .append(lineNo).append("\n");
                    
                    break;
                }
                
                // PREDICATE DECLARATIONS //
                if (scope == Scopes.PREDICATE && indent == 1) {
                    scope = Scopes.PREDICATES;
                }
                
                if (scope == Scopes.PREDICATES && indent == 1) {
                    final String lineWoBreaks = line.replace("\n", "");
                    Matcher m = Pattern.compile("^\\s*([^\\(]+)\\s*\\(([^\\)]+)\\)\\s*;\\s*$").matcher(lineWoBreaks);
                    
                    if (!m.matches()) {
                        System.err.println("Syntax error: Bad predicate declaration: '" + lineWoBreaks + "' at line " + lineNo);
                    }

                          String predicateType = m.group(2).replaceAll("\\s*", "");
                    final String predicateName = m.group(1);
                        
                    sb.append(predicateName).append(" : ").append(predicateType)
                            .append(SEPARATOR).append(keyFile.getName())
                            .append(SEPARATOR).append("/^")
                            .append(escape(origLine)).append("$/;\"")
                            .append(SEPARATOR).append(PREDICATES_TLT)
                            .append(SEPARATOR).append("line:")
                            .append(lineNo).append("\n");
                    
                    break;
                }
                
                // FUNCTION DECLARATIONS //
                if (scope == Scopes.FUNCTION && indent == 1) {
                    scope = Scopes.FUNCTIONS;
                }
                
                if (scope == Scopes.FUNCTIONS && indent == 1) {
                    final String lineWoBreaks = line.replace("\n", "");
                    Matcher m = Pattern.compile("^\\s*(?:\\\\\\S+)?\\s*([a-zA-Z_0-9]+)\\s*([a-zA-Z_0-9]+)(?:\\{[^\\}]+\\})?\\s*(?:\\(([^\\)]+)\\))?\\s*;\\s*$").matcher(lineWoBreaks);
                    
                    if (!m.matches()) {
                        System.err.println("Syntax error: Bad function declaration: '" + lineWoBreaks + "' at line " + lineNo);
                    }

                          String functionType = (m.group(3) == null ? "" :
                              m.group(3).replaceAll("\\s+", "") + " -> ") + m.group(1);
                    final String functionName = m.group(2);
                        
                    sb.append(functionName).append(" : ").append(functionType)
                            .append(SEPARATOR).append(keyFile.getName())
                            .append(SEPARATOR).append("/^")
                            .append(escape(origLine)).append("$/;\"")
                            .append(SEPARATOR).append(FUNCTIONS_TLT)
                            .append(SEPARATOR).append("line:")
                            .append(lineNo).append("\n");
                    
                    break;
                }
                
                // SCHEMA VARIABLE DECLARATIONS //
                if (scope == Scopes.SCHEMA_VARIABLE && indent == 1) {
                    scope = Scopes.SCHEMA_VARIABLES;
                }
                
                if (scope == Scopes.SCHEMA_VARIABLES && indent == 1) {
                    final String lineWoBreaks = line.replace("\n", "");
                    Matcher m = Pattern.compile("^\\s*(\\\\[a-zA-Z\\[\\]]+\\s*(?:\\{[^\\}]+\\})?)([^;]+);\\s*$").matcher(lineWoBreaks);
                    
                    if (!m.matches()) {
                        System.err.println("Syntax error: Bad schema variable declaration: '" + lineWoBreaks + "' at line " + lineNo);
                    }

                          String   svType  = m.group(1).trim();
                    final String[] svNames = m.group(2).split(",\\s*");
                    
                    for (String svName: svNames) {
                        svName = svName.trim();
                        
                        if (svName.contains(" ")) {
                            String[] svNameParts = svName.split(" ");
                            svType += " " + svNameParts[0];
                            svName = svNameParts[1];
                        }
                        
                        sb.append(svName).append(" : ").append(svType)
                                .append(SEPARATOR).append(keyFile.getName())
                                .append(SEPARATOR).append("/^")
                                .append(escape(origLine)).append("$/;\"")
                                .append(SEPARATOR).append(SCHEMA_VARIABLES_TLT)
                                .append(SEPARATOR).append("line:")
                                .append(lineNo).append("\n");
                    }
                    
                    scope = Scopes.SCHEMA_VARIABLE;
                    break;
                }
                
                // RULE DECLARATIONS //
                if (scope == Scopes.RULE && indent == 1) {
                    scope = Scopes.RULES;
                }
                
                if (scope == Scopes.RULES && indent == 1) {
                    if (!Pattern.compile("[a-zA-Z_0-9]+").matcher(part).matches()) {
                        System.err.println("Syntax error: Bad rule name '" + part + "' at line " + lineNo);
                    }
                    
                    sb.append(part).append(SEPARATOR).append(keyFile.getName())
                            .append(SEPARATOR).append("/^")
                            .append(escape(origLine)).append("$/;\"")
                            .append(SEPARATOR).append(RULE_TLT)
                            .append(SEPARATOR).append("line:").append(lineNo)
                            .append("\n");
                    
                    scope = Scopes.RULE;
                    continue;
                }
            }
        }
        
        br.close();
        
        return sb.toString();
    }
    
    private static String escape(String str) {
        final String[] toReplace = {
                "\\"
        };
        
        for (String repl: toReplace) {
            str = str.replace(repl, "\\" + repl);
        }
        
        return str;
    }
    
    static class Pair<A, B> {
        private A first;
        private B second;
        
        public Pair(A first, B second) {
            super();
            this.first = first;
            this.second = second;
        }
        
        public A getFirst() {
            return first;
        }
        public void setFirst(A first) {
            this.first = first;
        }
        
        public B getSecond() {
            return second;
        }
        public void setSecond(B second) {
            this.second = second;
        }
    }
}
