package com.barchart.netty.util.arb;

import java.util.Collection;

/** arbiter : ordered sequence pool */
public interface Arbiter<V> {

	/** discard store; reset to new expected sequence number */
	void reset(long key);

	/** fill store with (sequence,instance) pair */
	void fill(long key, V value);

	/** do we have consistent (no holes, non empty) store? */
	boolean isReady();

	/** extract store, regardless if ready */
	void drainTo(Collection<V> list);

}
