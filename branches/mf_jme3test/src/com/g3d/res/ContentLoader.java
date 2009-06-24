package com.g3d.res;

import java.io.IOException;
import java.io.InputStream;

/**
 * An interface for content loaders.
 */
public interface ContentLoader {

    /**
     * Loads content from the given input stream, parsing it into
     * an application-usable object.
     *
     * @param owner The <code>ContentManager</code> to which this loader is assigned.
     * @param stream Input stream from which to load the data
     * @param extension The extension of the file from which the stream is retrieved.
     * @return An object representing the resource.
     * @throws java.io.IOException If an I/O error occurs while loading
     */
    public Object load(ContentManager owner, InputStream stream, String extension, ContentKey key) throws IOException;
}
