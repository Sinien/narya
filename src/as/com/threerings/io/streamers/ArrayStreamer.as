//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2009 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
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

import com.threerings.util.ClassUtil;
import com.threerings.util.Enum;
import com.threerings.util.Log;
import com.threerings.util.env.Environment;

import com.threerings.io.ArrayMask;
import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamer;
import com.threerings.io.Translations;
import com.threerings.io.TypedArray;

/**
 * A Streamer for Array objects.
 */
public class ArrayStreamer extends Streamer
{
    public function ArrayStreamer (jname :String = "[Ljava.lang.Object;")
    {
        super(TypedArray, jname);

        var secondChar :String = jname.charAt(1);
        if (secondChar === "[") {
            // if we're a multi-dimensional array then we need a delegate
            _delegate = Streamer.getStreamerByJavaName(jname.substring(1));
            _isFinal = true; // it just is

        } else if (secondChar === "L") {
            // form is "[L<class>;"
            var baseJClass :String = jname.substring(2, jname.length - 1);
            _delegate = Streamer.getStreamerByJavaName(baseJClass);
            _elementType = ClassUtil.getClassByName(Translations.getFromServer(baseJClass));
            _isFinal = isFinal(_elementType);

        } else if (secondChar === "I") {
            _elementType = int;

        } else if (secondChar === "Z") {
            _elementType = Boolean;

        } else {
            Log.getLog(this).warning("Other array types are not yet handled", "jname", jname);
            throw new Error("Don't know how to stream '" + jname + "' instances.");
        }
    }

    override public function createObject (ins :ObjectInputStream) :Object
    {
        var ta :TypedArray = new TypedArray(_jname);
        ta.length = ins.readInt();
        return ta;
    }

    override public function writeObject (obj :Object, out :ObjectOutputStream) :void
    {
        var arr :Array = (obj as Array);
        var ii :int;
        out.writeInt(arr.length);
        if (_elementType == int) {
            for (ii = 0; ii < arr.length; ii++) {
                out.writeInt(int(arr[ii]));
            }

        } else if (_elementType == Boolean) {
            for (ii = 0; ii < arr.length; ii++) {
                out.writeBoolean(Boolean(arr[ii]));
            }

        } else if (_isFinal) {
            var mask :ArrayMask = new ArrayMask(arr.length);
            for (ii = 0; ii < arr.length; ii++) {
                if (arr[ii] != null) {
                    mask.setBit(ii);
                }
            }
            mask.writeTo(out);
            // now write the populated elements
            for (ii = 0; ii < arr.length; ii++) {
                var element :Object = arr[ii];
                if (element != null) {
                    out.writeBareObjectImpl(element, _delegate);
                }
            }

        } else {
            for (ii = 0; ii < arr.length; ii++) {
                out.writeObject(arr[ii]);
            }
        }
    }

    override public function readObject (obj :Object, ins :ObjectInputStream) :void
    {
        var arr :Array = (obj as Array);
        var ii :int;
        if (_elementType == int) {
            for (ii = 0; ii < arr.length; ii++) {
                arr[ii] = ins.readInt();
            }

        } else if (_elementType == Boolean) {
            for (ii = 0; ii < arr.length; ii++) {
                arr[ii] = ins.readBoolean();
            }

        } else if (_isFinal) {
            var mask :ArrayMask = new ArrayMask();
            mask.readFrom(ins);
            for (ii = 0; ii < arr.length; ii++) {
                if (mask.isSet(ii)) {
                    var target :Object;
                    if (_delegate == null) {
                        target = new _elementType();
                    } else {
                        target = _delegate.createObject(ins);
                    }
                    ins.readBareObjectImpl(target, _delegate);
                    arr[ii] = target;
                }
            } 

        } else {
            for (ii = 0; ii < arr.length; ii++) {
                arr[ii] = ins.readObject();
            } 
        }
    }

    protected static function isFinal (type :Class) :Boolean
    {
        if (type === String) {
            return true;
        }

        // all enums are final, even if you forget to make your enum class final, you punk
        if (Environment.isAssignableAs(Enum, type)) {
            return true;
        }

        // TODO: there's currently no way to determine final from the class
        // I thought examining the prototype might do it, but no dice.
        // Fuckers!
        return false;
    }

    /** A streamer for our elements. */
    protected var _delegate :Streamer;

    /** If this is the final dimension of the array, the element type. */
    protected var _elementType :Class;

    /** Whether we're final or not: true if we're not the final dimension
     * or if the element type is final. */
    protected var _isFinal :Boolean;
}
}
