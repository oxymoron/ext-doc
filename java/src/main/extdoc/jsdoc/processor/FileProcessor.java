package extdoc.jsdoc.processor;

import extdoc.jsdoc.docs.*;
import extdoc.jsdoc.tags.*;
import extdoc.jsdoc.tags.impl.Comment;
import extdoc.jsdoc.tplschema.*;
import extdoc.jsdoc.tree.TreePackage;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.text.MessageFormat;
import java.util.*;


/**
 * User: Andrey Zubkov
 * Date: 25.10.2008
 * Time: 4:41:12
 */
public class FileProcessor{

    public List<DocClass> classes = new ArrayList<DocClass>();
    public List<DocCfg> cfgs = new ArrayList<DocCfg>();
    public List<DocProperty> properties =
            new ArrayList<DocProperty>();
    public List<DocMethod> methods = new ArrayList<DocMethod>();
    public List<DocEvent> events = new ArrayList<DocEvent>();

    private TreePackage tree = new TreePackage();

    private final static String OUT_FILE_EXTENSION = "html";
    private final static boolean GENERATE_DEBUG_XML = true;
    private final static String COMPONENT_NAME =
            "Ext.Component";

    private String className;
    private String shortClassName;
    private String currFile;

    private static final String START_LINK = "{@link";    

    private static enum LinkStates {READ, LINK}

    private static final String
            MEMBER_REFERENCE_TPL =
            "<a href=\"output/{0}.html#{1}\" " +
                    "ext:member=\"{1}\" ext:cls=\"{0}\">{1}</a>";
    private static final String
            CLASS_AND_MEMBER_REFERENCE_TPL =
            "<a href=\"output/{0}.html#{1}\" " +
                    "ext:member=\"{1}\" ext:cls=\"{0}\">{0}.{1}</a>";
    private static final String
            CLASS_AND_MEMBER_REFERENCE_TPL_SHORT =
            "{0}.{1}";
    private static final String
            CLASS_REFERENCE_TPL =
            "<a href=\"output/{0}.html\" " +
                    "ext:cls=\"{0}\">{0}</a>";

    private static final int DESCR_MAX_LENGTH = 117;
    
    private String[] processLink(String text){
        int len = text.length();
        boolean found = false;
        int cut;
        for(cut=0;cut<len;cut++){
            char ch = text.charAt(cut);
            if (ch == '#' || Character.isWhitespace(ch)){
                found = true;
                break;
            }
        }
        
        String cls = found?text.substring(0,cut):text;
        String attr = found?text.substring(cut+1):"";

        String longText, shortText;
        if (found){
            if (cls.isEmpty()){
                longText = MessageFormat.format(
                        MEMBER_REFERENCE_TPL, className, attr);
                shortText = attr;
            }else{
                longText = MessageFormat.format(
                        CLASS_AND_MEMBER_REFERENCE_TPL, cls, attr);
                shortText = 
                        MessageFormat.format(
                                CLASS_AND_MEMBER_REFERENCE_TPL_SHORT,
                                    cls, attr);
            }
        }else{
            longText = MessageFormat.format(CLASS_REFERENCE_TPL, cls);
            shortText = cls;
        }
        return new String[]{longText, shortText};
    }

    private Description inlineLinks(String content){
        return inlineLinks(content, false);
    }

    /**
     * Replaces inline tag @link to actual html links and returns shot and/or
     *  long versions.
     * @param content description content
     * @param alwaysGenerateShort forces to generate short version for
     * Methods and events
     * @return short and long versions
     */
    private Description inlineLinks(String content,
                                                                boolean alwaysGenerateShort){

        if (content==null) return null;
        LinkStates state = LinkStates.READ;
        StringBuilder sbHtml = new StringBuilder();
        StringBuilder sbText = new StringBuilder();
        StringBuilder buffer = new StringBuilder();        
        for (int i=0;i<content.length();i++){
            char ch = content.charAt(i);
            switch (state){
                case READ:
                    if (endsWith(buffer, START_LINK)){
                        String substr = buffer.substring(
                                            0, buffer.length() - START_LINK.length());
                        sbHtml.append(substr);
                        sbText.append(substr);
                        buffer.setLength(0);
                        state = LinkStates.LINK;
                        break;
                    }
                    buffer.append(ch);
                    break;
                case LINK:
                    if(ch=='}'){
                        String[] str = processLink(buffer.toString()); 
                        sbHtml.append(str[0]);
                        sbText.append(str[1]);
                        buffer.setLength(0);
                        state = LinkStates.READ;
                        break;
                    }
                    buffer.append(ch);
                    break;
            }
        }

        // append remaining
        sbHtml.append(buffer);
        sbText.append(buffer);

        String sbString = sbText.toString().replaceAll("<\\S*?>","");        

        Description description = new Description();
        description.longDescr =  sbHtml.toString();
        if(alwaysGenerateShort){
            description.hasShort = true;
            description.shortDescr =
                sbString.length()>DESCR_MAX_LENGTH?
                        new StringBuilder()
                            .append(sbString.substring(0, DESCR_MAX_LENGTH))
                            .append("...").toString()
                :sbString;
        }else{
            description.hasShort = sbString.length()>DESCR_MAX_LENGTH;
            description.shortDescr =
                description.hasShort?
                        new StringBuilder()
                            .append(sbString.substring(0, DESCR_MAX_LENGTH))
                            .append("...").toString()
                :null;
        }
        return description;
    }


    /**
     *  Read params from list of param tags and add them to list of params
     *  Just simplifies param processing for class, method and event
     * @param paramTags tags
     * @param  params target list of params
     */
    private void readParams(List<ParamTag> paramTags,
                                                        List<Param> params){
        for(ParamTag paramTag: paramTags){
            Param param = new Param();
            param.name = paramTag.getParamName();
            param.type = paramTag.getParamType();
            Description descr = inlineLinks(paramTag.getParamDescription());
            param.description = descr!=null?descr.longDescr:null;
            param.optional = paramTag.isOptional();
            params.add(param);
        }
    }


    /**
     * Separates fullclass name to package and class
     * By this rule:
     * Ext => pkg: "" cls: "Ext"
     * Ext.Button => pkg: "Ext" cls: "Button"
     * Ext.util.Observable => pkg: "Ext.util" cls: "Observable"
     * Ext.layout.BorderLayout.Region => pkg: "Ext.layout" cls: "BorderLayout.Region"
     * Ext.Updater.BasicRenderer => pkg: "Ext" cls: "Updater.BasicRenderer"
     * @param className Class name to parse
     * @return Array of strings [0] package [1] class
     */
    private String[] separatePackage(String className){
        String [] str = new String[2];
        String[] items = className.split("\\.");
        if (items.length == 1){
            str[0] = "";
            str[1] = className;
        }else{
            StringBuilder pkg = new StringBuilder(items[0]);
            StringBuilder cls = new StringBuilder(items[items.length - 1]);
            for(int i=items.length-2;i>0;i--){
                if (Character.isUpperCase(items[i].charAt(0))){
                    // if starts with capital it is a part of class name
                    cls.insert(0, '.');
                    cls.insert(0, items[i]);
                }else{
                    // insert remaining package name
                    for(int j =1;j<=i;j++){
                        pkg.append('.');
                        pkg.append(items[j]);
                    }
                    break;
                }
            }
            str[0] = pkg.toString();
            str[1] = cls.toString();
        }
        return str;
    }

    private String[] separateByLastDot(String className){
        String[] str = new String[2];
        int len = className.length();
        int i = len-1;
        while(i>=0 && className.charAt(i)!='.') i--;
        str[0] = (i>0)?className.substring(0,i):"";
        str[1] = className.substring(i+1,len);
        return str;
    }

    /**
     * Process class 
     * @param comment Comment
     */
    private void processClass(Comment comment){

        DocClass cls = new DocClass();
        
        ClassTag classTag = comment.tag("@class");
        Tag singletonTag = comment.tag("@singleton");
        ExtendsTag extendsTag = comment.tag("@extends");
        Tag constructorTag = comment.tag("@constructor");
        List<ParamTag> paramTags = comment.tags("@param");

        cls.className = classTag.getClassName();
        String[] str = separatePackage(cls.className);
        cls.packageName = str[0];
        cls.shortClassName = str[1];
        cls.definedIn = currFile;
        cls.singleton = singletonTag!=null;
        String description = classTag.getClassDescription();
        if (description==null && extendsTag!=null){
            description = extendsTag.getClassDescription();
        }
        Description descr = inlineLinks(description);
        cls.description = descr!=null?descr.longDescr:null;
        cls.parentClass =
                (extendsTag!=null)?extendsTag.getClassName():null;
        cls.hasConstructor = constructorTag!=null;
        if (constructorTag!=null){
            cls.constructorDescription = inlineLinks(constructorTag.text(), true);
            readParams(paramTags, cls.params);
        }

        // Skip private classes
        if (!comment.hasTag("@private")
                && !comment.hasTag("@ignore")) {
            classes.add(cls);
        }
        className = cls.className;
        shortClassName = cls.shortClassName;

        // Process cfg declared inside class definition
        // goes after global className set
        List<CfgTag> innerCfgs =  comment.tags("@cfg");
        for (CfgTag innerCfg: innerCfgs){
            DocCfg cfg = getDocCfg(innerCfg);
            cfgs.add(cfg);
        }
        
    }

    /**
     * Helper method to process cfg in separate comment and in class
     * definition
     */
    private DocCfg getDocCfg(CfgTag tag){
        DocCfg cfg = new DocCfg();
        cfg.name = tag.getCfgName();
        cfg.type = tag.getCfgType();
        cfg.description = inlineLinks(tag.getCfgDescription());
        cfg.optional = tag.isOptional();
        cfg.className = className;
        cfg.shortClassName = shortClassName;
        return cfg;
    }

    /**
     * Process cfg
     * @param comment Comment
     */
    private void processCfg(Comment comment){
        // Skip private
        if (comment.hasTag("@private")
                || comment.hasTag("@ignore")) return;
        CfgTag tag = comment.tag("@cfg");
        DocCfg cfg = getDocCfg(tag);
        cfg.hide = comment.tag("@hide")!=null
                || (cfg.description!=null 
                    && cfg.description.longDescr.startsWith("@hide"));
        cfgs.add(cfg);
    }

    /**
     * Process property 
     * @param comment Comment
     * @param extraLine first word form the line after comment
     */
    private void processProperty(Comment comment,String extraLine){
        // Skip private
        if (comment.hasTag("@private")
                || comment.hasTag("@ignore")) return;

        
        DocProperty property = new DocProperty();

        Tag propertyTag = comment.tag("@property");
        TypeTag typeTag = comment.tag("@type");

        property.name = separateByLastDot(extraLine)[1];
        if (propertyTag!=null
                && propertyTag.text()!=null 
                && propertyTag.text().length()>0){
            property.name = propertyTag.text();
        }
        property.type = typeTag.getType();
        property.description = inlineLinks(comment.getDescription());
        property.className = className;
        property.shortClassName = shortClassName;
        property.hide = comment.tag("@hide")!=null;
        properties.add(property);
    }

    /**
     * Process method 
     * @param comment Comment
     * @param extraLine first word form the line after comment
     */
    private void processMethod(Comment comment, String extraLine){
        // Skip private
        if (comment.hasTag("@private")
                || comment.hasTag("@ignore")) return;


        DocMethod method = new DocMethod();

        Tag methodTag = comment.tag("@method");
        Tag staticTag = comment.tag("@static");
        List<ParamTag> paramTags = comment.tags("@param");
        ReturnTag returnTag = comment.tag("@return");
        MemberTag memberTag = comment.tag("@member");

        // should be first because @member may redefine class
        method.className = className;
        method.shortClassName = shortClassName;
        method.name = separatePackage(extraLine)[1];
        if (methodTag!=null){
            if (!methodTag.text().isEmpty()){
                method.name = methodTag.text();
            }
        }
        if (memberTag!=null){
            String name = memberTag.getMethodName();
            if (name!=null){
                method.name = name;
            }
            method.className = memberTag.getClassName();
            method.shortClassName =
                    separatePackage(method.className)[1];            
        }
        method.isStatic = (staticTag!=null);

        // renaming if static
//        if(method.isStatic){
//            method.name = new StringBuilder()
//                    .append(shortClassName)
//                    .append('.')
//                    .append(separateByLastDot(extraLine)[1])
//                    .toString();
//        }

        method.description = inlineLinks(comment.getDescription(), true);
        if (returnTag!=null){
            method.returnType =returnTag.getReturnType();
            method.returnDescription =returnTag.getReturnDescription();
        }
        readParams(paramTags, method.params);
        method.hide = comment.tag("@hide")!=null;
        methods.add(method);
    }

    /**
     * Process event
     * @param comment Comment
     */
    private void processEvent(Comment comment){
        // Skip private
        if (comment.hasTag("@private")
                || comment.hasTag("@ignore")) return;

        
        DocEvent event = new DocEvent();
        EventTag eventTag = comment.tag("@event");
        List<ParamTag> paramTags = comment.tags("@param");
        event.name = eventTag.getEventName();
        event.description = inlineLinks(eventTag.getEventDescription(), true);
        readParams(paramTags, event.params);
        event.className = className;
        event.shortClassName = shortClassName;
        event.hide = comment.tag("@hide")!=null;
        events.add(event);
    }

    /**
     *  Determine type of comment and process it
     * @param content text inside / ** and * /
     * @param extraLine first word form the line after comment 
     */
    private void processComment(String content, String extraLine){
        if (content==null) return;
        Comment comment = new Comment(content);
        if(comment.hasTag("@class")){
            processClass(comment);
        }else if(comment.hasTag("@event")){
            processEvent(comment);
        }else if(comment.hasTag("@cfg")){
            processCfg(comment);
        }else if(comment.hasTag("@type")){
            processProperty(comment, extraLine);        
        }else{
            processMethod(comment, extraLine);            
        }
        
    }

    private enum State {CODE, COMMENT}
    private enum ExtraState {SKIP, SPACE, READ}   

    private static final String START_COMMENT = "/**";
    private static final String END_COMMENT = "*/";

    /**
     * Checks if StringBuilder ends with string
     */
    private boolean endsWith(StringBuilder sb, String str){
        int len = sb.length();
        int strLen = str.length();        
        return (len>=strLen && sb.substring(len-strLen).equals(str));
    }

    /**
     * Checks if char is white space in terms of extra line of code after
     * comments
     * @param ch character
     * @return true if space or new line or * or / or ' etc...
     */
    private boolean isWhite(char ch){
        return !Character.isLetterOrDigit(ch) && ch!='.';
    }

    /**
     * Processes one file with state machine
     * @param fileName Source Code file name
     */
    private void processFile(String fileName){
        try {
            File file = new File(new File(fileName).getAbsolutePath());
            currFile = file.getName();
            System.out.println(
                    MessageFormat.format("Processing: {0}", currFile));
            BufferedReader reader =
                    new BufferedReader(new FileReader(file));
            int numRead;
            State state = State.CODE;
            ExtraState extraState = ExtraState.SKIP;            
            StringBuilder buffer = new StringBuilder();
            StringBuilder extraBuffer = new StringBuilder();            
            String comment=null;
            char ch;
            while((numRead=reader.read())!=-1){
                ch =(char)numRead;
                buffer.append(ch);
                switch(state){
                    case CODE:
                        switch (extraState){
                            case SKIP:
                                break;
                            case SPACE:
                                if (isWhite(ch)){
                                    break;
                                }
                                extraState = ExtraState.READ;
                                /* fall through */
                            case READ:
                                if (isWhite(ch)){
                                    extraState = ExtraState.SKIP;
                                    break;
                                }
                                extraBuffer.append(ch);
                                break;
                        }                                            
                        if (endsWith(buffer, START_COMMENT)){
                            if (comment!=null){
                                // comment is null before the first comment starts
                                // so we do not process it
                                processComment(comment, extraBuffer.toString());
                            }
                            extraBuffer.setLength(0);
                            buffer.setLength(0);
                            state = State.COMMENT;
                        }
                        break;
                    case COMMENT:
                       if (endsWith(buffer, END_COMMENT)){
                            comment =
                                    buffer.substring(0,
                                            buffer.length()-END_COMMENT.length());
                            buffer.setLength(0);
                            state = State.CODE;
                            extraState = ExtraState.SPACE;
                        }
                        break;
                }
            }
            processComment(comment, extraBuffer.toString());
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createClassHierarchy(){
        for(DocClass docClass: classes){
            for(DocClass cls: classes){
                if(docClass.className.equals(cls.parentClass)){
                    ClassDescr subClass = new ClassDescr();
                    subClass.className = cls.className;
                    subClass.shortClassName = cls.shortClassName;
                    docClass.subClasses.add(subClass);
                    cls.parent = docClass;
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

    private <T extends DocAttribute> boolean isOverridden(T doc,
                                                          List<T> docs){
        if (doc.name == null) return false;
        for(DocAttribute attr:docs){
            if (doc.name.equals(attr.name)) return true;
        }
        return false;
    }

    private <T extends DocAttribute> void removeHidden
                                                                                        (List<T> docs){
        for(ListIterator<T> it = docs.listIterator(); it.hasNext();){
            if (it.next().hide)
                it.remove();
        }
    }

    private <T extends DocAttribute> void addInherited
                                                    (List<T> childDocs, List<T> parentDocs){
        for(T attr: parentDocs) {
            if (!isOverridden(attr, childDocs)){
                childDocs.add(attr);
            }
        }
    }


    private void injectInherited(){
        for(DocClass cls: classes){
            DocClass parent = cls.parent;
            while(parent!=null){
                ClassDescr superClass = new ClassDescr();
                superClass.className = parent.className;
                superClass.shortClassName = parent.shortClassName;
                cls.superClasses.add(superClass);
                if (parent.className.equals(COMPONENT_NAME)){
                    cls.component = true;
                }
                addInherited(cls.cfgs, parent.cfgs);
                addInherited(cls.properties, parent.properties);
                addInherited(cls.methods, parent.methods);
                addInherited(cls.events, parent.events);
                parent = parent.parent;
            }
            removeHidden(cls.cfgs);
            removeHidden(cls.properties);
            removeHidden(cls.methods);
            removeHidden(cls.events);

            // sorting
            Collections.sort(cls.cfgs);
            Collections.sort(cls.properties);
            Collections.sort(cls.methods);
            Collections.sort(cls.events);

        }
    }

    private void createPackageHierarchy(){
        for(DocClass cls: classes){
            tree.addClass(cls);
        }
    }

    private void showStatistics(){
        for (Map.Entry<String, Integer> e : Comment.allTags.entrySet()){
            System.out.println(e.getKey() + ": " + e.getValue());        
        }
    }

    public void process(String fileName){
        try {
            File xmlFile = new File(new File(fileName).getAbsolutePath());
            FileInputStream fileInputStream = new FileInputStream(xmlFile);
            JAXBContext jaxbContext =
                    JAXBContext.newInstance("extdoc.jsdoc.schema");
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            extdoc.jsdoc.schema.Doc doc =
                    (extdoc.jsdoc.schema.Doc) unmarshaller.
                            unmarshal(fileInputStream);
            extdoc.jsdoc.schema.Source source = doc.getSource();
            List<extdoc.jsdoc.schema.File> files = source.getFile();
            for(extdoc.jsdoc.schema.File file: files){
                processFile(xmlFile.getParent()+ File.separator +file.getSrc());
            }
            showStatistics();
            fileInputStream.close();
            createClassHierarchy();
            injectInherited();
            createPackageHierarchy();
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


     private void copyDirectory(File sourceLocation , File targetLocation)
        throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }

            String[] children = sourceLocation.list();
            for (String child : children) {
                copyDirectory(new File(sourceLocation, child),
                        new File(targetLocation, child));
            }
        } else {

            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }

    public void saveToFolder(String folderName, String templateFileName){
        new File(folderName).mkdirs();
        try {

            File templateFile =
                    new File(new File(templateFileName).getAbsolutePath());
            String templateFolder = templateFile.getParent();

            // Read template.xml
            JAXBContext jaxbTplContext =
                    JAXBContext.newInstance("extdoc.jsdoc.tplschema");
            Unmarshaller unmarshaller = jaxbTplContext.createUnmarshaller();
            Template template = (Template) unmarshaller.
                        unmarshal(new FileInputStream(templateFile));
            ClassTemplate classTemplate = template.getClassTemplate();
            String classTplFileName = new StringBuilder()
                    .append(templateFolder)
                    .append(File.separator)
                    .append(classTemplate.getTpl())
                    .toString();
            String classTplTargetDir = new StringBuilder()
                    .append(folderName)
                    .append(File.separator)
                    .append(classTemplate.getTargetDir())
                    .toString();
            TreeTemplate treeTemplate = template.getTreeTemplate();
            String treeTplFileName = new StringBuilder()
                    .append(templateFolder)
                    .append(File.separator)
                    .append(treeTemplate.getTpl())
                    .toString();
            String treeTplTargetFile = new StringBuilder()
                    .append(folderName)
                    .append(File.separator)
                    .append(treeTemplate.getTargetFile())
                    .toString();

            new File(classTplTargetDir).mkdirs();

            // Copy resources
            Resources resources = template.getResources();

            List<Copy> dirs = resources.getCopy();

            for(Copy dir : dirs){
                String src = new StringBuilder()
                    .append(templateFolder)
                    .append(File.separator)
                    .append(dir.getSrc())
                    .toString();
                String dst = new StringBuilder()
                    .append(folderName)
                    .append(File.separator)
                    .append(dir.getDst())
                    .toString();
                copyDirectory(new File(src), new File(dst));
            }

            // Marshall and transform classes
            JAXBContext jaxbContext =
                    JAXBContext.newInstance("extdoc.jsdoc.docs");
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(
                    Marshaller.JAXB_FORMATTED_OUTPUT,
                    true
            );
            DocumentBuilderFactory builderFactory =
                    DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);

            TransformerFactory factory = TransformerFactory.newInstance();
            Templates transformation = 
                    factory
                            .newTemplates (new StreamSource(classTplFileName)) ;
            Transformer transformer = transformation.newTransformer();

            DocumentBuilder docBuilder = builderFactory.newDocumentBuilder();

            for(DocClass docClass: classes){
                System.out.println("Saving: " + docClass.className);
                String targetFileName = new StringBuilder()
                        .append(classTplTargetDir)
                        .append(File.separator)
                        .append(docClass.className)
                        .append('.')
                        .append(OUT_FILE_EXTENSION)
                        .toString();
                Document doc = docBuilder.newDocument();
                marshaller.marshal(docClass, doc);
                if (GENERATE_DEBUG_XML){
                    marshaller.marshal(docClass, new File(targetFileName+"_"));
                }
                Result fileResult = new StreamResult(new File(targetFileName));
                transformer.transform(new DOMSource(doc), fileResult);
                transformer.reset();
            }

            // Marshall and transform tree
            JAXBContext jaxbTreeContext =
                    JAXBContext.newInstance("extdoc.jsdoc.tree");
            Marshaller treeMarshaller = jaxbTreeContext.createMarshaller();
            treeMarshaller.setProperty(
                    Marshaller.JAXB_FORMATTED_OUTPUT,
                    true
            );

            Templates treeTransformation =
                    factory
                            .newTemplates (new StreamSource(treeTplFileName)) ;
            Transformer treeTransformer =
                    treeTransformation.newTransformer();
            Document doc =
                        builderFactory.newDocumentBuilder().newDocument();
            treeMarshaller.marshal(tree, doc);
            if (GENERATE_DEBUG_XML){
                    treeMarshaller.
                            marshal(tree, new File(treeTplTargetFile+"_"));
            }
            Result fileResult = new StreamResult(new File(treeTplTargetFile));
            treeTransformer.transform(new DOMSource(doc), fileResult);

        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
