package com.tencent.tsf.femas.common.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 计数器，从0开始，保证正数。
 *
 * @author zhixinzxliu
 */
public class PositiveAtomicCounter {

    private static final int MASK = 0x7FFFFFFF;
    private final AtomicInteger atom;

    public PositiveAtomicCounter() {
        atom = new AtomicInteger(0);
    }

    public final int incrementAndGet() {
        return atom.incrementAndGet() & MASK;
    }

    public final int getAndIncrement() {
        return atom.getAndIncrement() & MASK;
    }

    public int get() {
        return atom.get() & MASK;
    }

}
