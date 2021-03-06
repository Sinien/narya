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

package com.threerings.crowd.client {

import com.threerings.presents.client.InvocationDecoder;

/**
 * Dispatches calls to a {@link LocationReceiver} instance.
 */
public class LocationDecoder extends InvocationDecoder
{
    /** The generated hash code used to identify this receiver class. */
    public static const RECEIVER_CODE :String = "58f2830e027f4f3377e100ef12332497";

    /** The method id used to dispatch {@link LocationReceiver#forcedMove}
     * notifications. */
    public static const FORCED_MOVE :int = 1;

    /**
     * Creates a decoder that may be registered to dispatch invocation
     * service notifications to the specified receiver.
     */
    public function LocationDecoder (receiver :LocationReceiver)
    {
        this.receiver = receiver;
    }

    public override function getReceiverCode () :String
    {
        return RECEIVER_CODE;
    }

    public override function dispatchNotification (methodId :int, args :Array) :void
    {
        switch (methodId) {
        case FORCED_MOVE:
            LocationReceiver(receiver).forcedMove(
                (args[0] as int)
            );
            return;

        default:
            super.dispatchNotification(methodId, args);
        }
    }
}
}
