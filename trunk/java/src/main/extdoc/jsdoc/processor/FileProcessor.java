package extdoc.jsdoc.processor;

import extdoc.jsdoc.docs.*;
import extdoc.jsdoc.schema.Doc;
import extdoc.jsdoc.schema.File;
import extdoc.jsdoc.schema.Source;
import extdoc.jsdoc.tags.TagParam;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * User: Andrey Zubkov
 * Date: 25.10.2008
 * Time: 4:41:12
 */
public class FileProcessor{

    public List<DocClass> classes = new ArrayList<DocClass>();
    public List<DocCfg> cfgs = new ArrayList<DocCfg>();
    public List<DocProperty> properties = new ArrayList<DocProperty>();
    public List<DocMethod> methods = new ArrayList<DocMethod>();
    public List<DocEvent> events = new ArrayList<DocEvent>();

    final static String OUT_FILE_EXTENSION = "html";

    final static Pattern commentPattern = Pattern.compile("/\\*\\*((?:.|[\\n\\r])*?)\\*/[\\r\\n](.*)");
    final static Pattern contentFilterPattern = Pattern.compile("[\\r\\n]\\s*\\*\\s*");


    final static Pattern classSigature = Pattern.compile("@class");
    final static Pattern cfgSigature = Pattern.compile("@cfg");
    final static Pattern eventSigature = Pattern.compile("@event");
    final static Pattern propertySigature = Pattern.compile("@property|@type");
    final static Pattern methodSigature = Pattern.compile("@method|@member|@return");

    final static Pattern cedPattern = Pattern.compile("@class (\\S*)[\\r\\n](?:@extends (\\S*)[\\r\\n])?((?:.|[\\r\\n])*?)(?:\\z|[\\r\\n](?=@))");
    final static Pattern singletonPattern = Pattern.compile("@singleton");
    final static Pattern constructorPattern = Pattern.compile("@constructor((?:.|[\\r\\n])*?)(?:\\z|[\\r\\n](?=@))");
    final static Pattern paramPattern = Pattern.compile("@param \\{(\\S*)\\} (\\S*)( \\((?:O|o)ptional\\)|)((?:.|[\\r\\n])*?)(?:\\z|[\\r\\n](?=@))");
    final static Pattern cfgPattern = Pattern.compile("@cfg \\{(\\S*)\\} (\\S*)( \\((?:O|o)ptional\\)|)((?:.|[\\r\\n])*)");
    final static Pattern eventDescrPattern = Pattern.compile("@event (\\S*)((?:.|[\\r\\n])*?)(?:\\z|[\\r\\n](?=@))");
    final static Pattern extraNamePattern = Pattern.compile("\\s*(\\w*)");
    final static Pattern propertyPattern = Pattern.compile("@property (\\S*)");
    final static Pattern typePattern = Pattern.compile("@type \\{?([\\w\\.]*)\\}?");
    final static Pattern descrPattern = Pattern.compile("((?:.|[\\r\\n])*?)(?:\\z|[\\r\\n](?=@))");
    final static Pattern methodPattern = Pattern.compile("@method (\\S*)");
    final static Pattern memberPattern = Pattern.compile("@member (\\S*) (\\S*)");
    final static Pattern returnPattern = Pattern.compile("@return \\{(\\S*)\\}((?:.|[\\r\\n])*?)(?:\\z|[\\r\\n]\\s*(?=@))");

    private String className;

    private String currFile;

    private void processClass(String content) {

        Matcher cedMatcher = cedPattern.matcher(content);
        Matcher singletonMatcher = singletonPattern.matcher(content);
        Matcher constructorMatcher = constructorPattern.matcher(content);
        Matcher paramMatcher = paramPattern.matcher(content);

        cedMatcher.find();
        className = cedMatcher.group(1);

        String parentClass = cedMatcher.group(2);

        DocClass docClass = new DocClass();
        docClass.className = className;
        docClass.definedIn = currFile;
        docClass.parentClass = parentClass!=null?parentClass:"Object";
        docClass.description = cedMatcher.group(3);
        docClass.singleton = singletonMatcher.find();
        docClass.hasConstructor = constructorMatcher.find();
        docClass.constructorDescription = docClass.hasConstructor?constructorMatcher.group(1):null;

        while(paramMatcher.find()){
            TagParam param = new TagParam();
            param.type = paramMatcher.group(1);
            param.name = paramMatcher.group(2);
            param.optional = paramMatcher.group(3).length()>0;
            param.description = paramMatcher.group(4);
            docClass.params.add(param);
        }
        classes.add(docClass);
    }

    private void processCfg(String content) {
        Matcher matcher = cfgPattern.matcher(content);
        DocCfg cfg = new DocCfg();
        if(matcher.find()){
            cfg.type = matcher.group(1);
            cfg.name = matcher.group(2);
            cfg.optional = matcher.group(3).length()>0;
            cfg.description = matcher.group(4);
        }
        cfg.className = className;
        cfgs.add(cfg);
    }

    private void processEvent(String content) {
        Matcher matcher = eventDescrPattern.matcher(content);
        Matcher paramMatcher = paramPattern.matcher(content);
        DocEvent event = new DocEvent();
        if(matcher.find()){
            event.name = matcher.group(1);
            event.description = matcher.group(2);
        }
        while(paramMatcher.find()){
            TagParam param = new TagParam();
            param.type = paramMatcher.group(1);
            param.name = paramMatcher.group(2);
            param.optional = paramMatcher.group(3).length()>0;
            param.description = paramMatcher.group(4);
            event.params.add(param);
        }
        event.className = className;
        events.add(event);
    }

    private void processProperty(String content, String extraLine) {
        Matcher propertyMatcher = propertyPattern.matcher(content);
        Matcher typeMatcher = typePattern.matcher(content);
        Matcher descrMatcher = descrPattern.matcher(content);
        DocProperty property = new DocProperty();
        if(propertyMatcher.find()){
            property.name = propertyMatcher.group(1);
        }else{
            Matcher extraNameMatcher = extraNamePattern.matcher(extraLine);
            property.name = extraNameMatcher.find()?extraNameMatcher.group(1):null;
        }
        property.type = typeMatcher.find()?typeMatcher.group(1):null;
        property.description = descrMatcher.find()?descrMatcher.group(1):null;
        property.className = className;
        properties.add(property);
    }

    private void processMethod(String content, String extraLine) {
        Matcher methodMatcher = methodPattern.matcher(content);
        Matcher memberMatcher = memberPattern.matcher(content);
        Matcher descrMatcher = descrPattern.matcher(content);
        Matcher paramMatcher = paramPattern.matcher(content);
        Matcher returnMatcher = returnPattern.matcher(content);
        DocMethod method = new DocMethod();
        if (methodMatcher.find()){
            method.name = methodMatcher.group(1);
        }else if (memberMatcher.find()){
            method.name = memberMatcher.group(2);
        }else{
            Matcher extraNameMatcher = extraNamePattern.matcher(extraLine);
            method.name = extraNameMatcher.find()?extraNameMatcher.group(1):null;
        }
        method.description = descrMatcher.find()?descrMatcher.group(1):null;
        method.className = className;
        while(paramMatcher.find()){
            TagParam param = new TagParam();
            param.type = paramMatcher.group(1);
            param.name = paramMatcher.group(2);
            param.optional = paramMatcher.group(3).length()>0;
            param.description = paramMatcher.group(4);
            method.params.add(param);
        }
        if (returnMatcher.find()){
            method.returnType = returnMatcher.group(1);
            method.returnDescription = returnMatcher.group(2);
        }
        methods.add(method);
    }

    private void processComment(String content, String extraLine){

        if(classSigature.matcher(content).find()){
            processClass(content);
        }else if(cfgSigature.matcher(content).find()){
            processCfg(content);
        }else if(eventSigature.matcher(content).find()){
            processEvent(content);
        }else if(propertySigature.matcher(content).find()){
            processProperty(content, extraLine);
        }else{
            processMethod(content, extraLine);
        }
    }


    private void processFile(String fileName){
        try {
            java.io.File file = new java.io.File(fileName);
            currFile = file.getName();
            FileChannel fc = new FileInputStream(file).getChannel();
            ByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, (int)fc.size());
            CharBuffer cb = Charset.forName("8859_1").newDecoder().decode(bb);
            Matcher matcher = commentPattern.matcher(cb);
            while(matcher.find()){
                processComment(
                        contentFilterPattern.matcher(matcher.group(1)).replaceAll("\n"),
                        matcher.group(2));
            }
            fc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void populateTree(){
        for(DocClass docClass: classes){
            for(DocClass cls: classes){
                if(docClass.className.equals(cls.parentClass)){
                    docClass.subClasses.add(cls);
                }
            }
            for(DocCfg cfg: cfgs){
                if(docClass.className.equals(cfg.className)){
                    docClass.cfgs.add(cfg);
                }
            }
            for(DocProperty property: properties){
                if(docClass.className.equals(property.className)){
                    docClass.properties.add(property);
                }
            }
            for(DocMethod method: methods){
                if(docClass.className.equals(method.className)){
                    docClass.methods.add(method);
                }
            }
            for(DocEvent event: events){
                if(docClass.className.equals(event.className)){
                    docClass.events.add(event);
                }
            }
        }
    }

    public void process(String fileName){
        try {
            java.io.File xmlFile = new java.io.File(fileName);
            FileInputStream fileInputStream = new FileInputStream(xmlFile);
            JAXBContext jaxbContext = JAXBContext.newInstance("extdoc.jsdoc.schema");
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            Doc doc = (Doc) unmarshaller.unmarshal(fileInputStream);
            Source source = doc.getSource();
            List<File> files = source.getFile();
            for(File file: files){
                processFile(xmlFile.getParent()+ java.io.File.separator +file.getSrc());
            }
            fileInputStream.close();
            populateTree();
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void createResources(String folderName){
       java.io.File file = new java.io.File(folderName);
       file.mkdirs();
    }

    public void saveToFolder(String folderName, String templateFileName) {
        createResources(folderName);
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance("extdoc.jsdoc.docs");
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);

            TransformerFactory factory = TransformerFactory.newInstance();
            Templates transformation = factory.newTemplates (new StreamSource(templateFileName)) ;
            Transformer transformer = transformation.newTransformer();

            for(DocClass docClass: classes){
                String targetFileName = folderName+java.io.File.separator+docClass.className+".html";
                Document doc = builderFactory.newDocumentBuilder().newDocument();
                marshaller.marshal(docClass, doc);
                marshaller.marshal(docClass, new java.io.File(targetFileName+"_"));
                Result fileResult = new StreamResult(new java.io.File(targetFileName));
                transformer.transform(new DOMSource(doc), fileResult);
            }
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }
}
