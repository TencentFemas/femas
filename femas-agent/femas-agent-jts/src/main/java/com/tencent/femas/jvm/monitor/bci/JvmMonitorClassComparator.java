/**
 * Copyright 2010-2021 the original author or authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tencent.femas.jvm.monitor.bci;

import java.util.Comparator;

public class JvmMonitorClassComparator implements Comparator<Class<?>> {

    @Override
    public int compare(Class<?> cl1, Class<?> cl2) {
        if (cl1.equals(cl2)) {
            return 0;
        }
        ClassLoader loader1 = cl1.getClassLoader();
        ClassLoader loader2 = cl2.getClassLoader();
        if ((loader1 == null && loader2 == null)
                || ((loader1 != null && loader2 != null) && loader1.equals(loader2))) {
            if (cl1.getName().equals(cl2.getName())) {
                return 0;
            }
        }
        return cl1.hashCode() > cl2.hashCode() ? 1 : -1;

    }
}
