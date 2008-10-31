package extdoc.jsdoc.docs;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Andrey Zubkov
 * Date: 25.10.2008
 * Time: 5:16:41
 */

@XmlRootElement
public class DocClass{
    public String className;
    public String definedIn;
    public boolean singleton;
    public String description;
    public String parentClass;
    public boolean hasConstructor;
    public String constructorDescription;
    public List<TagParam> params = new ArrayList<TagParam>();
    public List<DocCfg> cfgs = new ArrayList<DocCfg>();
    public List<DocProperty> properties = new ArrayList<DocProperty>();
    public List<DocMethod> methods = new ArrayList<DocMethod>();
    public List<DocEvent> events = new ArrayList<DocEvent>();
    public List<DocClass> subClasses = new ArrayList<DocClass>();
}
