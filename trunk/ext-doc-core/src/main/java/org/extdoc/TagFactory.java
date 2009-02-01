package org.extdoc;

/**
 * User: Andrey Zubkov
 * Date: 26.01.2009
 * Time: 20:15:01
 */
public interface TagFactory {
    public Tag createTag(String tagText);
    public Tag createDescription(String description);
}
