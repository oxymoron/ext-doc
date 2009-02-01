package org.extdoc;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;

import java.util.List;

/**
 * User: Andrey Zubkov
 * Date: 25.01.2009
 * Time: 16:12:07
 */
public class MainTest {

    @Test
    public void testSources(){
        BeanFactory factory = new XmlBeanFactory(
                new FileSystemResource("test/context/context.xml"));
        Parser parser = (Parser) factory.getBean("parser");
        parser.parse();
    }
    
}
