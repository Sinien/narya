//
// $Id: ImageManager.java,v 1.19 2002/09/23 23:30:23 mdb Exp $

package com.threerings.media;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.imageio.ImageReader;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import com.samskivert.io.NestableIOException;

import com.threerings.resource.ResourceManager;
import com.threerings.media.Log;
import com.threerings.media.util.ImageUtil;

/**
 * Provides a single point of access for image retrieval and caching.
 */
public class ImageManager
{
    /**
     * Construct an image manager with the specified {@link
     * ResourceManager} from which it will obtain its data. A non-null
     * <code>context</code> must be provided if there is any expectation
     * that the image manager will not be able to load images via the
     * ImageIO services and will have to fallback to Toolkit-style
     * loading.
     */
    public ImageManager (ResourceManager rmgr, Component context)
    {
	_rmgr = rmgr;

        // try to figure out which image loader we'll be using
        try {
            _loader = (ImageLoader)Class.forName(IMAGEIO_LOADER).newInstance();
        } catch (Throwable t) {
            Log.info("Unable to use ImageIO to load images. " +
                     "Falling back to Toolkit [error=" + t + "].");
            _loader = new ToolkitLoader(context);
        }
    }

    /**
     * Loads the image via the resource manager using the specified path
     * and caches it for faster retrieval henceforth.
     */
    public Image getImage (String path)
        throws IOException
    {
	Image img = (Image)_imgs.get(path);
	if (img != null) {
	    // Log.info("Retrieved image from cache [path=" + path + "].");
	    return img;
	}

	// Log.info("Loading image into cache [path=" + path + "].");
        img = createImage(_rmgr.getResource(path));
        _imgs.put(path, img);
        return img;
    }

    /**
     * Loads the image via the resource manager using the specified
     * path. Does no caching and does not convert the image for optimized
     * display on the target graphics configuration. Instead the original
     * image as returned by the image loader is returned.
     */
    public Image loadImage (String path)
        throws IOException
    {
        try {
            InputStream imgin = _rmgr.getResource(path);
            BufferedInputStream bin = new BufferedInputStream(imgin);
            return _loader.loadImage(bin);
        } catch (IllegalArgumentException iae) {
            String errmsg = "Error loading image [path=" + path + "]";
            throw new NestableIOException(errmsg, iae);
        }
    }

    /**
     * Loads the image from the supplied input stream. Does no caching and
     * does not convert the image for optimized display on the target
     * graphics configuration. Instead the original image as returned by
     * the image loader is returned.
     */
    public Image loadImage (InputStream source)
        throws IOException
    {
        try {
            return _loader.loadImage(source);
        } catch (IllegalArgumentException iae) {
            String errmsg = "Error loading image";
            throw new NestableIOException(errmsg, iae);
        }
    }

    /**
     * Creates an image that is optimized for rendering in the client's
     * environment and decodes the image data from the specified source
     * image into that target image. The resulting image is not cached.
     */
    public Image createImage (InputStream source)
        throws IOException
    {
        return createImage(loadImage(source));
    }

    /**
     * Creates an image that is optimized for rendering in the client's
     * environment and renders the supplied image into the optimized
     * image.  The resulting image is not cached.
     */
    public Image createImage (Image src)
    {
        int swidth = src.getWidth(null);
        int sheight = src.getHeight(null);
        BufferedImage dest = null;

        // use the same kind of transparency as the source image if we can
        // when converting the image to a format optimized for display
        if (src instanceof BufferedImage) {
            int trans = ((BufferedImage)src).getColorModel().getTransparency();
            dest = ImageUtil.createImage(swidth, sheight, trans);
        } else {
            dest = ImageUtil.createImage(swidth, sheight);
        }

        Graphics2D gfx = dest.createGraphics();
        gfx.drawImage(src, 0, 0, null);
        gfx.dispose();

        return dest;
    }

    /** A reference to the resource manager via which we load image data
     * by default. */
    protected ResourceManager _rmgr;

    /** The image loader via which we convert an input stream into an
     * image. */
    protected ImageLoader _loader;

    /** A cache of loaded images. */
    protected HashMap _imgs = new HashMap();

    /** The classname of the ImageIO-based image loader which we attempt
     * to use but fallback from if we're not running a JVM that has
     * ImageIO support. */
    protected static final String IMAGEIO_LOADER =
        "com.threerings.media.ImageIOLoader";
}
