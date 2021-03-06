//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.io.streamers {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamer;

/**
 * A Streamer for Number objects.
 */
public class NumberStreamer extends Streamer
{
    public function NumberStreamer ()
    {
        super(Number, "java.lang.Double");
    }

    override public function createObject (ins :ObjectInputStream) :Object
    {
        return ins.readDouble();
    }

    override public function writeObject (obj :Object, out :ObjectOutputStream) :void
    {
        var n :Number = (obj as Number);
        out.writeDouble(n);
    }

    override public function readObject (obj :Object, ins :ObjectInputStream) :void
    {
        // nothing here, the Number is fully read in createObject()
    }
}
}
