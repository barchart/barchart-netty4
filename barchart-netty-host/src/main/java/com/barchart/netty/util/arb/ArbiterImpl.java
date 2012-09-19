package com.barchart.netty.util.arb;

import java.util.Collection;

public class ArbiterImpl<V> implements Arbiter<V> {

	/** lifetime maximum */
	protected int maxPoolSize;

	protected int sizeSaved;

	protected long countProcessed;
	protected long countFilled;
	protected long countDrained;
	protected long countDuplicate;
	protected long countLost;

	/** max gap size */
	protected long keyThreshold = 100 * 1024;

	/** origin of batch */
	protected long keyOriginal;

	/** most advanced received */
	protected long keyAdvanced;

	/** next expected to receive */
	protected long keyExpected;

	@SuppressWarnings("unchecked")
	protected final V[] array = (V[]) new Object[(int) keyThreshold];

	@Override
	public void fill(final long keyCurrent, final V value) {

		countProcessed++;

		final long keyExpected = this.keyExpected;
		final long keyOriginal = this.keyOriginal;

		final int offset = (int) (keyCurrent - keyOriginal);

		/** new or gap */
		if (keyCurrent == 0 || Math.abs(offset) > keyThreshold) {
			resetInternal(keyCurrent);
			store(0, value);
			countFilled++;
			return;
		}

		/** present expected */
		if (keyCurrent == keyExpected) {
			store(offset, value);
			updateKeyAdvanced(keyCurrent);
			updateKeyExpected();
			countFilled++;
			return;
		}

		/** past expired */
		if (keyCurrent < keyOriginal) {
			countDuplicate++;
			return;
		}

		/** past stored */
		if (keyCurrent < keyExpected) {
			countDuplicate++;
			return;
		}

		/** future : keyCurrent > keyExpected */
		if (containsInternal(offset)) {
			countDuplicate++;
			return;
		} else {
			store(offset, value);
			updateKeyAdvanced(keyCurrent);
			countFilled++;
			return;
		}

	}

	protected final boolean containsInternal(final int offset) {
		assert 0 <= offset && offset < array.length;
		return array[offset] != null;
	}

	protected final void updateKeyExpected() {
		int offset = (int) (keyExpected - keyOriginal);
		do {
			offset++;
		} while (array[offset] != null);
		keyExpected = keyOriginal + offset;
	}

	protected final void updateKeyAdvanced(final long keyCurrent) {
		if (keyCurrent > keyAdvanced) {
			keyAdvanced = keyCurrent;
		}
	}

	protected final void store(final int offset, final V value) {
		assert 0 <= offset && offset < array.length;
		assert array[offset] == null;
		array[offset] = value;
		sizeSaved++;
	}

	@Override
	public void reset(final long keyExpected) {
		this.keyOriginal = keyExpected;
		this.keyAdvanced = 0;
		this.keyExpected = keyExpected;
	}

	protected void resetInternal(final long keyCurrent) {
		this.keyOriginal = keyCurrent;
		this.keyAdvanced = keyCurrent;
		this.keyExpected = keyCurrent + 1;
	}

	@Override
	public boolean isReady() {
		/** most expected scenario */
		if (sizeSaved == 1 && keyExpected == (keyOriginal + 1)) {
			return true;
		}
		/** last received is last in batch */
		if (keyAdvanced + 1 != keyExpected) {
			return false;
		}
		/** batch has no holes */
		if (keyExpected - keyOriginal != sizeSaved) {
			return false;
		}
		return true;
	}

	@Override
	public void drainTo(final Collection<V> list) {

		final int sizeSaved = this.sizeSaved;

		if (sizeSaved == 0) {
			return;
		}

		this.sizeSaved = 0;

		countDrained += sizeSaved;

		if (sizeSaved > maxPoolSize) {
			maxPoolSize = sizeSaved;
		}

		final long keyExpected = keyAdvanced + 1;
		final int range = (int) (keyExpected - keyOriginal);

		this.keyOriginal = keyExpected;
		this.keyExpected = keyExpected;

		for (int index = 0; index < range; index++) {
			final V value = array[index];
			if (value != null) {
				array[index] = null;
				list.add(value);
			}
		}

	}

}
