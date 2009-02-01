package org.extdoc;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;

public class Main
{
    public static void main( String[] args )
    {
        BeanFactory factory =
                new XmlBeanFactory(new FileSystemResource("context.xml"));
        Context context = (Context) factory.getBean("context");        
    }
}
