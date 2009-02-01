package org.extdoc;

import java.util.List;
import java.util.Map;

/**
 * User: Andrey Zubkov
 * Date: 26.01.2009
 * Time: 20:15:23
 */
public class StandardTagFactory implements TagFactory{

    private int parts = 1;

    public StandardTagFactory(){}

    public StandardTagFactory(Integer parts) {
        this.parts = parts;
    }

    public Tag createTag(String tagText) {
        String[] str = StringUtils.split(tagText, parts +1);
        StandardTag tag = new StandardTag();
        tag.setName(str[0]);
        
        return null;
    }

    public Tag createDescription(String description) {
        StandardTag tag = new StandardTag();
        tag.setText(description);
        return tag;
    }
}
