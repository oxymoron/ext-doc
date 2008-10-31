package extdoc.jsdoc.tags;

/**
 * User: Andrey Zubkov
 * Date: 01.11.2008
 * Time: 1:58:03
 */
public class CfgTagImpl extends TagImpl{

    private String cfgName;

    private String cfgType;

    private String cfgDescription;

    public CfgTagImpl(String name, String text) {
        super(name, text);
        String[] str = divideAtWhite(text, 3);
        cfgType = removeBrackets(str[0]);
        cfgName = str[1];
        cfgDescription =str[2];

    }

    public String getCfgName() {
        return cfgName;
    }

    public String getCfgType() {
        return cfgType;
    }

    public String getCfgDescription() {
        return cfgDescription;
    }
}
