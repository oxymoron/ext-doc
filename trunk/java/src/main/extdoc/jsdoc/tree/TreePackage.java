package extdoc.jsdoc.tree;

import extdoc.jsdoc.docs.DocClass;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Andrey Zubkov
 * Date: 01.11.2008
 * Time: 16:48:21
 */
@XmlRootElement
public class TreePackage {

    private String name;

    public List<TreeClass> classes =
            new ArrayList<TreeClass>();

    public List<TreePackage> packages =
            new ArrayList<TreePackage>();

    public void addClass(DocClass docClass){
        addClass(docClass.packageName, docClass);
    }

    public void addClass(String packageName, DocClass docClass){        
        if (packageName.equals("")){
            TreeClass treeClass = new TreeClass();
            treeClass.setDocClass(docClass);
            classes.add(treeClass);
        }else{
            int i=0;
            int len = packageName.length();
            while (i<len && packageName.charAt(i)!='.'){
                i++;
            }
            String pkg = packageName.substring(0,i);
            String remains = (i<len)?packageName.substring(i+1,len):"";
            TreePackage p = addPackage(pkg);
            p.addClass(remains, docClass);
        }
    }

    /**
     * Returns existing or creates new package if not exists
     * @return returns new or existing package
     */
    protected TreePackage addPackage(String packageName){
        for(TreePackage p: packages){
            if (p.getName().equals(packageName)) return p;
        }
        TreePackage p = new TreePackage();
        p.setName(packageName);
        packages.add(p);
        return p;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
