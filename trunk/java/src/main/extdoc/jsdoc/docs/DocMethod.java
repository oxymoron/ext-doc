package extdoc.jsdoc.docs;

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
    public List<Param> params = new ArrayList <Param>();
    public String className;
    public String returnType;
    public String returnDescription;
    public boolean isStatic;
}
