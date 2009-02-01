package org.extdoc;

import java.util.Map;

/**
 * User: Andrey Zubkov
 * Date: 26.01.2009
 * Time: 21:49:03
 */
public class StandardTagResolver implements TagResolver{

    private Map<String,TagFactory> tagFactories = null;

    public void setTagFactories(Map<String, TagFactory> tagFactories) {
        this.tagFactories = tagFactories;
    }

    public TagFactory getTagFactory(String tagText) {
        String first = StringUtils.firstWord(tagText);
        if (tagFactories.containsKey(first)){
            return tagFactories.get(first);
        }
        return new StandardTagFactory(1);
    }
}
