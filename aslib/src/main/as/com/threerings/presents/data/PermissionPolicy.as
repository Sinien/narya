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

package com.threerings.presents.data {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

/**
 * Encapsulates a fine-grained permissions policy. The default policy is to deny access to
 * everything, systems using fine-grained permissions should create a custom policy and provide it
 * at client resolution time via the ClientResolver. This would be an inner class of ClientObject,
 * but ActionScript does not support inner classes.
 */
public class PermissionPolicy implements Streamable
{
    /**
     * Returns null if the specified client has the specified permission, an error code explaining
     * the lack of access if they do not. {@link InvocationCodes#ACCESS_DENIED} should be returned
     * if no more specific explanation is available.
     */
    public function checkAccess (clobj :ClientObject, perm :Permission, context :Object) :String
    {
        // by default, you can't do it!
        return InvocationCodes.ACCESS_DENIED;
    }

    public function writeObject (out :ObjectOutputStream) :void
    {
        // nothing to write
    }

    public function readObject (ins :ObjectInputStream) :void
    {
        // nothing to read
    }
}
}
