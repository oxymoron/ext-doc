package extdoc.jsdoc.tree;

import extdoc.jsdoc.docs.DocClass;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Andrey Zubkov
 * Date: 01.11.2008
 * Time: 16:48:21
 */
public class TreePackage {

    private String name;

    public TreePackage(String name) {
        this.name = name;
    }

    protected List<TreeClass> classes =
            new ArrayList<TreeClass>();

    private List<TreePackage> packages =
            new ArrayList<TreePackage>();

    public List<TreeClass> getClasses() {
        return classes;
    }

    public List<TreePackage> getPackages() {
        return packages;
    }

    public void addClass(DocClass docClass){
        addClass(docClass.packageName, docClass);
    }

    public void addClass(String packageName, DocClass docClass){        
        if (packageName.equals("")){
            classes.add(new TreeClass(docClass));
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
     */
    protected TreePackage addPackage(String packageName){
        for(TreePackage p: packages){
            if (p.getName().equals(packageName)) return p;
        }
        TreePackage p = new TreePackage(packageName);
        packages.add(p);
        return p;
    }

    public String getName() {
        return name;
    }
}
