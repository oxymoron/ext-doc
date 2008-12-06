package extdoc.jsdoc.util;

/**
 * User: Andrey Zubkov
 * Date: 06.12.2008
 * Time: 13:07:38
 */
public class StringUtils {

    public static class ClsAttrName{
        public String cls = "";
        public String attr = "";
        public String name = "";
    }

    enum LinkStates {CLS, ATTR, SKIPWHITE, NAME}
    /**
     * Processes link in format: [cls]#[attrib] [newName]
     * @param str Source String
     * @return Object containing class name, attribute (method, property,
     * etc.) and name (to be displayed)
     */
    public static ClsAttrName processLink(String str){
        ClsAttrName res  = new ClsAttrName();
        LinkStates state = LinkStates.CLS;
        int len = str.length(); 
        int start = 0;
        for(int i=0;i<len;i++){
            char ch = str.charAt(i);
            switch(state){
                case CLS:
                    if (ch == '#'){
                        res.cls = str.substring(start, i);
                        start = i+1;
                        state = LinkStates.ATTR;
                    }else if (Character.isWhitespace(ch)){
                        res.cls = str.substring(start, i);
                        start = i+1;
                        state = LinkStates.SKIPWHITE;
                    }
                    break;
                case ATTR:
                    if (!Character.isWhitespace(ch)) break;
                    res.attr = str.substring(start, i);
                    state = LinkStates.SKIPWHITE;                                            
                    // fall through
                case SKIPWHITE:
                    if (!Character.isWhitespace(ch)){
                        start = i;
                        state = LinkStates.NAME;                        
                    }
                    break;
            }
            if (state == LinkStates.NAME){
                res.name = str.substring(start, len);
                break;
            }
        }

        // process remaining
        switch(state){
            case CLS:
                res.cls = str.substring(start, len);
                break;
            case ATTR:
                res.attr = str.substring(start, len);
                break;            
        }

        return res;
    }

}
