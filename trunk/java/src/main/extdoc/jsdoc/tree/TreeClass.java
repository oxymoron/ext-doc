package extdoc.jsdoc.tree;

import extdoc.jsdoc.docs.DocClass;

/**
 * User: Andrey Zubkov
 * Date: 01.11.2008
 * Time: 17:02:23
 */
public class TreeClass {
    
    private DocClass docClass;

    public TreeClass(DocClass docClass) {
        this.docClass = docClass;
    }

    public DocClass getDocClass() {
        return docClass;
    }

}
