package extdoc.jsdoc.docs;

import extdoc.jsdoc.tags.TagParam;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Andrey Zubkov
 * Date: 25.10.2008
 * Time: 15:14:36
 */
public class DocEvent{
    public String name;
    public String description;
    public List<TagParam> params = new ArrayList<TagParam>();
    public String className;
}