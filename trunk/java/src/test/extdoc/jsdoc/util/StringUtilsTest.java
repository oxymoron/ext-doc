package extdoc.jsdoc.util;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * User: Andrey Zubkov
 * Date: 06.12.2008
 * Time: 14:34:24
 */
public class StringUtilsTest {

    @Test
    public void testProcessLinkFull(){
        StringUtils.ClsAttrName res  =
                StringUtils.processLink("Ext.Button#set setMethod");
        assertEquals(res.cls, "Ext.Button");
        assertEquals(res.attr, "set");
        assertEquals(res.name, "setMethod");
    }
    
    @Test
    public void testProcessLinkEmpty(){
        StringUtils.ClsAttrName res  = StringUtils.processLink("");
        assertEquals(res.cls, "");
        assertEquals(res.attr, "");
        assertEquals(res.name, "");
    }

    @Test
    public void testProcessLinkClsMethod(){
        StringUtils.ClsAttrName res  =
                StringUtils.processLink("Ext.Button#set");
        assertEquals(res.cls, "Ext.Button");
        assertEquals(res.attr, "set");
        assertEquals(res.name, "");
    }

    @Test
    public void testProcessLinkMethodName(){
        StringUtils.ClsAttrName res  =
                StringUtils.processLink("#set setMethod");
        assertEquals(res.cls, "");
        assertEquals(res.attr, "set");
        assertEquals(res.name, "setMethod");
    }

    @Test
    public void testProcessLinkMethod(){
        StringUtils.ClsAttrName res  =
                StringUtils.processLink("#set   ");
        assertEquals(res.cls, "");
        assertEquals(res.attr, "set");
        assertEquals(res.name, "");
    }

    @Test
    public void testProcessLinkSkipWhite(){
        StringUtils.ClsAttrName res  =
                StringUtils.processLink("Ext.Button#set        setMethod");
        assertEquals(res.cls, "Ext.Button");
        assertEquals(res.attr, "set");
        assertEquals(res.name, "setMethod");
    }

        @Test
    public void testProcessLinkClass(){
        StringUtils.ClsAttrName res  =
                StringUtils.processLink("Ext.Button");
        assertEquals(res.cls, "Ext.Button");
        assertEquals(res.attr, "");
        assertEquals(res.name, "");
    }

    @Test
    public void testProcessLinkClassName(){
        StringUtils.ClsAttrName res  =
                StringUtils.processLink("Ext.Button button");
        assertEquals(res.cls, "Ext.Button");
        assertEquals(res.attr, "");
        assertEquals(res.name, "button");
    }


}
