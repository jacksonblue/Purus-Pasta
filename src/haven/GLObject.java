/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven;

import static haven.GOut.checkerr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.media.opengl.GL2;

public abstract class GLObject {
    private static final Map<CurrentGL, Collection<GLObject>> disposed = new HashMap<CurrentGL, Collection<GLObject>>();
    private boolean del;
    public final CurrentGL cur;

    public GLObject(GOut g) {
        this.cur = g.curgl;
    }

    public void dispose() {
        Collection<GLObject> can;
        synchronized (disposed) {
            if (del)
                return;
            del = true;
            can = disposed.get(cur);
            if (can == null) {
                can = new LinkedList<GLObject>();
                disposed.put(cur, can);
            }
        }
        synchronized (can) {
            can.add(this);
        }
    }

    protected void finalize() {
        dispose();
    }

    public abstract void create(GL2 gl);

    protected abstract void delete(BGL gl);

    public static void disposeall(CurrentGL cur, BGL gl) {
        Collection<GLObject> can;
        synchronized (disposed) {
            can = disposed.get(cur);
            if (can == null)
                return;
        }
        Collection<GLObject> copy;
        synchronized (can) {
            copy = new ArrayList<GLObject>(can);
            can.clear();
        }
        for (GLObject obj : copy)
            obj.delete(gl);
        checkerr(gl);
    }
}
