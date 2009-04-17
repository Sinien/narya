//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.util {

import flash.errors.IllegalOperationError;

import flash.utils.Dictionary;

/**
 * An implementation of a HashMap in actionscript. Any object (and null) may
 * be used as a key. Simple keys (Number, int, uint, Boolean, String) utilize
 * a Dictionary internally for storage; keys that implement Hashable are
 * stored efficiently, and any other key can also be used if the equalsFn
 * and hashFn are specified to the constructor.
 */
public class HashMap
    implements Map
{
    /**
     * Construct a HashMap
     *
     * @param loadFactor - A measure of how full the hashtable is allowed to
     *                     get before it is automatically resized. The default
     *                     value of 1.75 should be fine.
     * @param equalsFn   - (Optional) A function to use to compare object
     *                     equality for keys that are neither simple nor
     *                     implement Hashable. The signature should be
     *                     "function (o1, o2) :Boolean".
     * @param hashFn     - (Optional) A function to use to generate a hash
     *                     code for keys that are neither simple nor
     *                     implement Hashable. The signature should be
     *                     "function (obj) :*", where the return type is
     *                     numeric or String. Two objects that are equals
     *                     according to the specified equalsFn <b>must</b>
     *                     generate equal values when passed to the hashFn.
     */
    public function HashMap (
            loadFactor :Number = 1.75,
            equalsFn :Function = null,
            hashFn :Function = null)
    {
        if ((equalsFn != null) != (hashFn != null)) {
            throw new ArgumentError("Both the equals and hash functions " +
                "must be specified, or neither.");
        }
        _loadFactor = loadFactor;
        _equalsFn = equalsFn;
        _hashFn = hashFn;
    }

    /** @inheritDoc */
    public function clear () :void
    {
        _simpleData = null;
        _simpleSize = 0;
        _entries = null;
        _entriesSize = 0;
    }

    /** @inheritDoc */
    public function containsKey (key :Object) :Boolean
    {
        return (undefined !== get(key));
    }

    /** @inheritDoc */
    public function get (key :Object) :*
    {
        if (isSimple(key)) {
            return (_simpleData == null) ? undefined : _simpleData[key];
        }

        if (_entries == null) {
            return undefined;
        }

        var hkey :Hashable = keyFor(key);
        var hash :int = hkey.hashCode();
        var index :int = indexFor(hash);
        var e :HashMap_Entry = (_entries[index] as HashMap_Entry);
        while (e != null) {
            if (e.hash == hash && e.key.equals(hkey)) {
                return e.value;
            }
            e = e.next;
        }
        return undefined;
    }

    /** @inheritDoc */
    public function isEmpty () :Boolean
    {
        return (size() == 0);
    }

    /** @inheritDoc */
    public function put (key :Object, value :Object) :*
    {
        var oldValue :*;
        if (isSimple(key)) {
            if (_simpleData == null) {
                _simpleData = new Dictionary();
            }

            oldValue = _simpleData[key];
            _simpleData[key] = value;
            if (oldValue === undefined) {
                _simpleSize++;
            }
            return oldValue;
        }

        // lazy-create the array holding other hashables
        if (_entries == null) {
            _entries = [];
            _entries.length = DEFAULT_BUCKETS;
        }

        var hkey :Hashable = keyFor(key);
        var hash :int = hkey.hashCode();
        var index :int = indexFor(hash);
        var firstEntry :HashMap_Entry = (_entries[index] as HashMap_Entry);
        for (var e :HashMap_Entry = firstEntry; e != null; e = e.next) {
            if (e.hash == hash && e.key.equals(hkey)) {
                oldValue = e.value;
                e.value = value;
                return oldValue; // size did not change
            }
        }

        _entries[index] = new HashMap_Entry(hash, hkey, value, firstEntry);
        _entriesSize++;
        // check to see if we should grow the map
        if (_entriesSize > _entries.length * _loadFactor) {
            resize(2 * _entries.length);
        }
        // indicate that there was no value previously stored for the key
        return undefined;
    }

    /** @inheritDoc */
    public function remove (key :Object) :*
    {
        if (isSimple(key)) {
            if (_simpleData == null) {
                return undefined;
            }

            var oldValue :* = _simpleData[key];
            if (oldValue !== undefined) {
                _simpleSize--;
            }
            delete _simpleData[key];
            return oldValue;
        }

        if (_entries == null) {
            return undefined;
        }

        var hkey :Hashable = keyFor(key);
        var hash :int = hkey.hashCode();
        var index :int = indexFor(hash);
        var prev :HashMap_Entry = (_entries[index] as HashMap_Entry);
        var e :HashMap_Entry = prev;

        while (e != null) {
            var next :HashMap_Entry = e.next;
            if (e.hash == hash && e.key.equals(hkey)) {
                if (prev == e) {
                    _entries[index] = next;
                } else {
                    prev.next = next;
                }
                _entriesSize--;
                // check to see if we should shrink the map
                if ((_entries.length > DEFAULT_BUCKETS) &&
                        (_entriesSize < _entries.length * _loadFactor * .125)) {
                    resize(Math.max(DEFAULT_BUCKETS, _entries.length / 2));
                }
                return e.value;
            }
            prev = e;
            e = next;
        }

        return undefined; // never found
    }

    /** @inheritDoc */
    public function size () :int
    {
        return _simpleSize + _entriesSize;
    }

    /** @inheritDoc */
    public function keys () :Array
    {
        var keys :Array = [];
        forEach0(function (k :*, v :*) :void {
            keys.push(k);
        });
        return keys;
    }

    /** @inheritDoc */
    public function values () :Array
    {
        var vals :Array = [];
        forEach0(function (k :*, v :*) :void {
            vals.push(v);
        });
        return vals;
    }

    /** @inheritDoc */
    public function forEach (fn :Function) :void
    {
        forEach0(fn);
    }

    /**
     * Internal forEach.
     * @private
     */
    protected function forEach0 (fn :Function) :void
    {
        if (_simpleData != null) {
            for (var key :Object in _simpleData) {
                fn(key, _simpleData[key]);
            }
        }

        if (_entries != null) {
            for (var ii :int = _entries.length - 1; ii >= 0; ii--) {
                for (var e :HashMap_Entry = (_entries[ii] as HashMap_Entry); e != null;
                        e = e.next) {
                    fn(e.getOriginalKey(), e.value);
                }
            }
        }
    }

    /**
     * Return a Hashable that represents the key.
     * @private
     */
    protected function keyFor (key :Object) :Hashable
    {
        if (key is Hashable) {
            return (key as Hashable);

        } else if (_hashFn == null) {
            throw new IllegalOperationError("Illegal key specified " +
                "for HashMap created without hashing functions.");

        } else {
            return new HashMap_KeyWrapper(key, _equalsFn, _hashFn);
        }
    }

    /**
     * Return an index for the specified hashcode.
     * @private
     */
    protected function indexFor (hash :int) :int
    {
        // TODO: improve?
        return Math.abs(hash) % _entries.length;
    }

    /**
     * Return true if the specified key may be used to store values in a
     * Dictionary object.
     * @private
     */
    protected function isSimple (key :Object) :Boolean
    {
        return (key == null) || (key is String) || (key is Number) || (key is Boolean) ||
            (key is Enum);
    }

    /**
     * Resize the entries with Hashable keys to optimize
     * the memory/performance tradeoff.
     * @private
     */
    protected function resize (newSize :int) :void
    {
        var oldEntries :Array = _entries;
        _entries = [];
        _entries.length = newSize;

        // place all the old entries in the new map
        for (var ii :int = 0; ii < oldEntries.length; ii++) {
            var e :HashMap_Entry = (oldEntries[ii] as HashMap_Entry);
            while (e != null) {
                var next :HashMap_Entry = e.next;
                var index :int = indexFor(e.hash);
                e.next = (_entries[index] as HashMap_Entry);
                _entries[index] = e;
                e = next;
            }
        }
    }

    /** The current number of key/value pairs stored in the Dictionary. @private */
    protected var _simpleSize :int = 0;

    /** The current number of key/value pairs stored in the _entries. @private */
    protected var _entriesSize :int = 0;

    /** The load factor. @private */
    protected var _loadFactor :Number;

    /** If non-null, contains simple key/value pairs. @private */
    protected var _simpleData :Dictionary

    /** If non-null, contains Hashable keys and their values. @private */
    protected var _entries :Array;

    /** The hashing function to use for non-Hashable complex keys. @private */
    protected var _hashFn :Function;

    /** The equality function to use for non-Hashable complex keys. @private */
    protected var _equalsFn :Function;

    /** The default size for the bucketed hashmap. @private */
    protected static const DEFAULT_BUCKETS :int = 16;
}

} // end: package com.threerings.util

