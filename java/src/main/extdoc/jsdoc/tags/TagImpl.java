package extdoc.jsdoc.tags;

/**
 * User: Andrey Zubkov
 * Date: 30.10.2008
 * Time: 23:33:11
 */
class TagImpl implements Tag{

    private String name;

    private String text;

    public TagImpl(String name, String text) {
        this.name = name;
        this.text = text;
    }

    public String name() {
        return name;
    }

    public String text() {
        return text;
    }

    String[] divideAtWhite(String text, int parts) {
        String[] str = new String[parts];
        int c = 0;
        int start = 0;
        char ch;
        for(int i=0;i<text.length();i++){
            ch = text.charAt(i);
            if (c < parts-1 && Character.isWhitespace(ch)){
                str[c] = text.substring(start, i);
                start = i+1;
                c++;
            }
        }
        str[parts-1] = text.substring(start, text.length());
        return str;
    }

}
