/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.tools.ant.types.resources;

import java.io.File;
import java.util.List;
import java.util.Stack;
import java.util.Vector;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.AbstractCollection;
import java.util.NoSuchElementException;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.util.CollectionUtils;

/**
 * Generic ResourceCollection: Either stores nested ResourceCollections,
 * making no attempt to remove duplicates, or references another ResourceCollection.
 * @since Ant 1.7
 */
public class Resources extends DataType implements ResourceCollection {
    /** static empty ResourceCollection */
    public static final ResourceCollection NONE = new ResourceCollection() {
        public boolean isFilesystemOnly() {
            return true;
        }
        public Iterator<Resource> iterator() {
            return EMPTY_ITERATOR;
        }
        public int size() {
            return 0;
        }
    };

    /** static empty Iterator */
    public static final Iterator<Resource> EMPTY_ITERATOR = new Iterator<Resource>() {
        public Resource next() {
            throw new NoSuchElementException();
        }
        public boolean hasNext() {
            return false;
        }
        public void remove() {
            throw new UnsupportedOperationException();
        }
    };

    private class MyCollection extends AbstractCollection<Resource> {
        private Collection<Resource> cached;

        MyCollection() {
        }
        public int size() {
            return getCache().size();
        }
        public Iterator<Resource> iterator() {
            return getCache().iterator();
        }
        private synchronized Collection<Resource> getCache() {
            Collection<Resource> coll = cached;
            if (coll == null) {
                coll = CollectionUtils.asCollection(new MyIterator());
                if (cache) {
                    cached = coll;
                }
            }
            return coll;
        }
        private class MyIterator implements Iterator<Resource> {
            private Iterator<ResourceCollection> rci = getNested().iterator();
            private Iterator<Resource> ri = null;

            public boolean hasNext() {
                boolean result = ri != null && ri.hasNext();
                while (!result && rci.hasNext()) {
                    ri = ((ResourceCollection) rci.next()).iterator();
                    result = ri.hasNext();
                }
                return result;
            }
            public Resource next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return ri.next();
            }
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }
    }

    private Vector<ResourceCollection> rc;
    private Collection<Resource> coll;
    private boolean cache = false;

    /**
     * Create a new Resources.
     */
    public Resources() {
    }

    /**
     * Create a new Resources.
     * @since Ant 1.8
     */
    public Resources(Project project) {
        setProject(project);
    }

    /**
     * Set whether to cache collections.
     * @param b boolean cache flag.
     * @since Ant 1.8.0
     */
    public synchronized void setCache(boolean b) {
        cache = b;
    }

    /**
     * Add a ResourceCollection.
     * @param c the ResourceCollection to add.
     */
    public synchronized void add(ResourceCollection c) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        if (c == null) {
            return;
        }
        if (rc == null) {
            rc = new Vector<ResourceCollection>();
        }
        rc.add(c);
        invalidateExistingIterators();
        coll = null;
        setChecked(false);
    }

    /**
     * Fulfill the ResourceCollection contract.
     * @return an Iterator of Resources.
     */
    public synchronized Iterator<Resource> iterator() {
        if (isReference()) {
            return getRef().iterator();
        }
        validate();
        return new FailFast(this, coll.iterator());
    }

    /**
     * Fulfill the ResourceCollection contract.
     * @return number of elements as int.
     */
    public synchronized int size() {
        if (isReference()) {
            return getRef().size();
        }
        validate();
        return coll.size();
    }

    /**
     * Fulfill the ResourceCollection contract.
     * @return true if all Resources represent files.
     */
    public boolean isFilesystemOnly() {
        if (isReference()) {
            return getRef().isFilesystemOnly();
        }
        validate();

        for (Iterator<ResourceCollection> i = getNested().iterator(); i.hasNext();) {
            if (!i.next().isFilesystemOnly()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Format this <code>Resources</code> as a String.
     * @return a descriptive <code>String</code>.
     */
    public synchronized String toString() {
        if (isReference()) {
            return getCheckedRef().toString();
        }
        validate();
        if (coll == null || coll.isEmpty()) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (Resource r : coll) {
            if (sb.length() > 0) {
                sb.append(File.pathSeparatorChar);
            }
            sb.append(r);
        }
        return sb.toString();
    }

    /**
     * Overrides the version of DataType to recurse on all DataType
     * child elements that may have been added.
     * @param stk the stack of data types to use (recursively).
     * @param p   the project to use to dereference the references.
     * @throws BuildException on error.
     */
    protected void dieOnCircularReference(Stack<Object> stk, Project p)
        throws BuildException {
        if (isChecked()) {
            return;
        }
        if (isReference()) {
            super.dieOnCircularReference(stk, p);
        } else {
            for (ResourceCollection resourceCollection : getNested()) {
                if (resourceCollection instanceof DataType) {
                    pushAndInvokeCircularReferenceCheck((DataType) resourceCollection, stk, p);
                }
            }
            setChecked(true);
        }
    }

    /**
     * Allow subclasses to notify existing Iterators they have experienced concurrent modification.
     */
    protected void invalidateExistingIterators() {
        FailFast.invalidate(this);
    }

    /**
     * Resolves references, allowing any ResourceCollection.
     * @return the referenced ResourceCollection.
     */
    private ResourceCollection getRef() {
        return (ResourceCollection) getCheckedRef(
            ResourceCollection.class, "ResourceCollection");
    }

    private synchronized void validate() {
        dieOnCircularReference();
        coll = (coll == null) ? new MyCollection() : coll;
    }

    private synchronized List<ResourceCollection> getNested() {
        return rc == null ? Collections.<ResourceCollection> emptyList() : rc;
    }
}
