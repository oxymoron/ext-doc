package extdoc.jsdoc.docs;

import extdoc.jsdoc.tags.TagParam;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Andrey Zubkov
 * Date: 25.10.2008
 * Time: 15:14:28
 */
public class DocMethod{
    public String name;
    public String description;
    public List<TagParam> params = new ArrayList <TagParam>();
    public String className;
    public String returnType;
    public String returnDescription;
    public boolean isStatic;
}
