package org.extdoc;

/**
 * User: Andrey Zubkov
 * Date: 26.01.2009
 * Time: 13:04:36
 */
public class StringUtils {

    public static String wildcardToRegex(String wildcard){
       StringBuilder s = new StringBuilder(wildcard.length());
       s.append('^');
       for (int i = 0, is = wildcard.length(); i < is; i++) {
           char c = wildcard.charAt(i);
           switch(c) {
               case '*':
                   s.append(".*");
                   break;
               case '?':
                   s.append(".");
                   break;
               // escape special regexp-characters
               case '(': case ')': case '[': case ']': case '$':
               case '^': case '.': case '{': case '}': case '|':
               case '\\':
                   s.append("\\");
                   s.append(c);
                   break;
               default:
                   s.append(c);
                   break;
           }
       }
       s.append('$');
       return(s.toString());
    }


      /**
     * Checks that a string buffer ends up with a given string. It may sound
     * trivial with the existing
     * JDK API but the various implementation among JDKs can make those
     * methods extremely resource intensive
     * and perform poorly due to massive memory allocation and copying. See
     * @param buffer the buffer to perform the check on
     * @param suffix the suffix
     * @return  <code>true</code> if the character sequence represented by the
     *          argument is a suffix of the character sequence represented by
     *          the StringBuffer object; <code>false</code> otherwise. Note that the
     *          result will be <code>true</code> if the argument is the
     *          empty string.
     */
    public static boolean endsWith(StringBuilder buffer, String suffix) {
        if (suffix.length() > buffer.length()) {
            return false;
        }
        // this loop is done on purpose to avoid memory allocation performance
        // problems on various JDKs
        // StringBuffer.lastIndexOf() was introduced in jdk 1.4 and
        // implementation is ok though does allocation/copying
        // StringBuffer.toString().endsWith() does massive memory
        // allocation/copying on JDK 1.5
        // See http://issues.apache.org/bugzilla/show_bug.cgi?id=37169
        int endIndex = suffix.length() - 1;
        int bufferIndex = buffer.length() - 1;
        while (endIndex >= 0) {
            if (buffer.charAt(bufferIndex) != suffix.charAt(endIndex)) {
                return false;
            }
            bufferIndex--;
            endIndex--;
        }
        return true;
    }

    public static String firstWord(String text){
        for(int i=0; i<text.length();i++){
            char ch = text.charAt(i);
            if (Character.isWhitespace(ch)){
                return text.substring(0, i);
            }
        }
        return text;
    }

    public static String[] split(String text, int parts) {
        String[] str = new String[parts];
        int c = 0;
        int start = 0;
        boolean skipWhite = true;
        for(int i=0;i<text.length();i++){
            char ch = text.charAt(i);
            boolean isWhite = Character.isWhitespace(ch);
            if (isWhite){
                if(!skipWhite){
                    str[c] = text.substring(start, i);
                    start = i;
                    c++;
                }
                start++;
            }else if(c >= parts-1){
                break;
            }
            skipWhite = isWhite;
        }
        str[c] = text.substring(start, text.length());
        return str;
    }

    
}
