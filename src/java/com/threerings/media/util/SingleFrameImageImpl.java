//
// $Id: SingleFrameImageImpl.java,v 1.5 2003/01/13 22:49:47 mdb Exp $

package com.threerings.media.util;

import java.awt.Graphics2D;

import com.threerings.media.image.Mirage;

/**
 * The single frame image class is a basic implementation of the {@link
 * MultiFrameImage} interface intended to facilitate the creation of MFIs
 * whose display frames consist of only a single image.
 */
public class SingleFrameImageImpl implements MultiFrameImage
{
    /**
     * Constructs a single frame image object.
     */
    public SingleFrameImageImpl (Mirage mirage)
    {
        _mirage = mirage;
    }

    // documentation inherited
    public int getFrameCount ()
    {
        return 1;
    }

    // documentation inherited from interface
    public int getWidth (int index)
    {
        return _mirage.getWidth();
    }

    // documentation inherited from interface
    public int getHeight (int index)
    {
        return _mirage.getHeight();
    }

    // documentation inherited from interface
    public void paintFrame (Graphics2D g, int index, int x, int y)
    {
        _mirage.paint(g, x, y);
    }

    // documentation inherited from interface
    public boolean hitTest (int index, int x, int y)
    {
        return _mirage.hitTest(x, y);
    }

    /** The frame image. */
    protected Mirage _mirage;
}
