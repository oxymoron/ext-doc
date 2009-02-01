package org.extdoc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.ArrayList;
import java.text.MessageFormat;

/**
 * User: Andrey Zubkov
 * Date: 26.01.2009
 * Time: 18:14:20
 */
public class StandardTagReader implements TagReader{

    private static Log log = LogFactory.getLog(TagReader.class);

    private String tagMarker = "@";
    private String inlineTagStartsWith = "{@";
    private String inlineTagEndsWith = "}";
    private TagResolver tagResolver = null;

    private enum State{DESCRIPTION, TAG, INLINE_TAG}

    public void setTagMarker(String tagMarker) {
        this.tagMarker = tagMarker;
    }

    public void setInlineTagStartsWith(String inlineTagStartsWith) {
        this.inlineTagStartsWith = inlineTagStartsWith;
    }

    public void setInlineTagEndsWith(String inlineTagEndsWith) {
        this.inlineTagEndsWith = inlineTagEndsWith;
    }

    public void setTagResolver(TagResolver tagResolver) {
        this.tagResolver = tagResolver;
    }

    private Tag createTag(String tagText){
        TagFactory tagFactory = tagResolver.getTagFactory(tagText);
        return tagFactory.createTag(tagText);
    }

    private Tag createDescription(String description){
        return null;//StandardTagFactory.createDescription(description);
    }

    public List<Tag> read(Context context) {
        StringBuilder comment = context.getCommentBuffer();
        StringBuilder buffer = new StringBuilder();
        State state = State.DESCRIPTION;
        List<Tag> tags = new ArrayList<Tag>();
        int len = comment.length();
        for(int i=0;i<len;i++){
            char ch = comment.charAt(i);
            switch(state){
                case DESCRIPTION:
                    if (StringUtils.endsWith(buffer, tagMarker)){
                        tags.add(createDescription(buffer.substring(
                                0, buffer.length()-tagMarker.length())));
                        buffer.setLength(0);
                        state = State.TAG;
                    }
                    buffer.append(ch);
                    break;
                case TAG:
                    if (StringUtils.endsWith(buffer, inlineTagStartsWith)){
                        state = State.INLINE_TAG;
                    }else if (StringUtils.endsWith(buffer, tagMarker)){
                        tags.add(createTag(buffer.substring(
                                0, buffer.length()-tagMarker.length())));
                        buffer.setLength(0);                                                
                    }
                    buffer.append(ch);
                    break;
                case INLINE_TAG:
                    if (StringUtils.endsWith(buffer, inlineTagEndsWith)){
                        state = State.TAG;
                    }
                    buffer.append(ch);
                    break;
            }
        }
        switch(state){
            case DESCRIPTION:
                tags.add(createDescription(buffer.toString()));
                break;
            case TAG:
                tags.add(createTag(buffer.toString()));
                break;
            case INLINE_TAG:
                log.warn(MessageFormat.format("Inline tag was started, " +
                        "but not finished : {0} \nin file {1}",
                        buffer, context.getCurrentFile().getName()));
                break;
        }
        return tags;
    }
}
