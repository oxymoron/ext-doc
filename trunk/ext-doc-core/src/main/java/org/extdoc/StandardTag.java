package org.extdoc;

/**
 * User: Andrey Zubkov
 * Date: 26.01.2009
 * Time: 20:10:32
 */
public class StandardTag implements Tag{

    private String name = null;
    private String text = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
