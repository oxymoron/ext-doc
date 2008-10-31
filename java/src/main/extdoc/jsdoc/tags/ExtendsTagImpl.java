package extdoc.jsdoc.tags;

/**
 * User: Andrey Zubkov
 * Date: 01.11.2008
 * Time: 1:54:12
 */
public class ExtendsTagImpl extends TagImpl{
    
    String className;

    String classDescription;

    public ExtendsTagImpl(String name, String text) {
        super(name, text);
        String[] str = divideAtWhite(text, 2);
        className = str[0];
        classDescription = str[1];
    }

    public String getClassName() {
        return className;
    }

    public String getClassDescription() {
        return classDescription;
    }
}
