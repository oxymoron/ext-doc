package extdoc.jsdoc.tags;

/**
 * User: Andrey Zubkov
 * Date: 01.11.2008
 * Time: 2:07:54
 */
public class TypeTagImpl extends TagImpl{

    private String type;

    public TypeTagImpl(String name, String text) {
        super(name, text);
        type = removeBrackets(text);        
    }

    public String getType() {
        return type;
    }
}
