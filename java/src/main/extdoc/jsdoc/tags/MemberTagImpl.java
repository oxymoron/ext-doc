package extdoc.jsdoc.tags;

/**
 * User: Andrey Zubkov
 * Date: 01.11.2008
 * Time: 3:00:25
 */
public class MemberTagImpl extends TagImpl{

    private String className;

    private String methodName;

    public MemberTagImpl(String name, String text) {
        super(name, text);
        String[] str = divideAtWhite(text, 2);
        className = str[0];
        methodName = str[1];
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }
}
