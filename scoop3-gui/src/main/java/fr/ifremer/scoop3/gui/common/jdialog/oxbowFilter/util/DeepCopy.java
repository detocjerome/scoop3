/*
 * Copyright (c) 2009-2011, EzWare
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.Redistributions
 * in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.Neither the name of the
 * EzWare nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */

package fr.ifremer.scoop3.gui.common.jdialog.oxbowFilter.util;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import fr.ifremer.scoop3.gui.common.jdialog.oxbowFilter.util.copy.FastByteArrayInputStream;
import fr.ifremer.scoop3.gui.common.jdialog.oxbowFilter.util.copy.FastByteArrayOutputStream;

/**
 * Utility for object deep copying (vs. clone()'s shallow copying)
 */
public class DeepCopy {

    /**
     * Creates deep copy of the object.
     *
     * @param originalObject
     *            object to copy
     * @return copy of originalObject
     * @throws DeepCopyException
     *             if operation cannot be performed
     */
    public static final <T extends Serializable> T copy(final T originalObject) {
	return DeepCopy.<T> restore((FastByteArrayInputStream) store(originalObject).getInputStream());
    }

    public static final <T extends Serializable> FastByteArrayOutputStream store(final T obj) {

	try {
	    final FastByteArrayOutputStream fbos = new FastByteArrayOutputStream();
	    final ObjectOutputStream out = new ObjectOutputStream(fbos);
	    out.writeObject(obj);
	    out.flush();
	    out.close();

	    return fbos;
	} catch (final Exception ex) {
	    throw new DeepCopyException("An " + obj.getClass().getSimpleName() + " cannot be serialized. The reason: "
		    + ex.getLocalizedMessage(), ex);
	}

    }

    @SuppressWarnings("unchecked")
    public static final <T extends Serializable> T restore(final FastByteArrayInputStream stream) {

	try {
	    final ObjectInputStream in = new ObjectInputStream(stream);
	    return (T) in.readObject();

	} catch (final Exception ex) {
	    throw new DeepCopyException("An object cannot be deserizalized. The reason: " + ex.getLocalizedMessage(),
		    ex);
	}

    }

}