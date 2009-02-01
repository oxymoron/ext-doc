package org.extdoc;

import java.util.List;

/**
 * User: Andrey Zubkov
 * Date: 26.01.2009
 * Time: 18:17:48
 */
public interface TagReader {
    List<Tag> read(Context context);
}
