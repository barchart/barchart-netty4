package com.barchart.netty.util.arb;

import java.util.Collection;

public interface Arbiter<V> {

	void reset(long key);

	void fill(long key, V value);

	boolean isReady();

	void drainTo(Collection<V> list);

}
