package extdoc.jsdoc.tags;

/**
 * User: Andrey Zubkov
 * Date: 30.10.2008
 * Time: 23:33:11
 */
public class TagImpl implements Tag{

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
}
