package org.extdoc;

/**
 * User: Andrey Zubkov
 * Date: 26.01.2009
 * Time: 21:48:51
 */
public interface TagResolver {
    public TagFactory getTagFactory(String tagText);
}
