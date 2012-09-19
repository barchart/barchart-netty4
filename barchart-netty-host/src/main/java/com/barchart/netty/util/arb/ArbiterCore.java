package com.barchart.netty.util.arb;

import java.util.Collection;

/** not thread safe */
public class ArbiterCore<V> implements Arbiter<V> {

	/** lifetime maximum store size */
	protected int maxStoreSize;

	/** currently stored instance count */
	protected int sizeStored;

	/** total received */
	protected long countProcessed;
	/** total stored */
	protected long countFilled;
	/** total extracted */
	protected long countDrained;
	/** total duplicates */
	protected long countDuplicate;
	/** total holes */
	protected long countLost;

	/** number of gaps during fill */
	protected long numberGap;
	/** number of drains wile not ready */
	protected long numberForced;

	/** start sequence of current batch */
	protected long keyOriginal;

	/** next sequence expected to receive */
	protected long keyExpected;

	/** most advanced sequence received already */
	protected long keyAdvanced;

	/** maximum permitted sequence gap size */
	protected final int storeSize;

	protected final V[] storeArray;

	public static final int DEFAULT_STORE_SIZE = 64 * 1024;

	public ArbiterCore() {
		this(DEFAULT_STORE_SIZE);
	}

	@SuppressWarnings("unchecked")
	public ArbiterCore(final int storeSize) {
		this.storeSize = storeSize;
		this.storeArray = (V[]) new Object[storeSize];
	}

	@Override
	public void fill(final long keyCurrent, final V value) {

		countProcessed++;

		final int offset = (int) (keyCurrent - keyOriginal);

		/** key as expected */
		if (keyCurrent == keyExpected) {
			store(offset, value);
			updateKeyAdvanced(keyCurrent);
			updateKeyExpected();
			countFilled++;
			return;
		}

		/** sequence start or key gap */
		if (keyCurrent == 0L || Math.abs(offset) >= storeSize) {
			numberGap++;
			reset(keyCurrent);
			store(0, value);
			updateKeyAdvanced(keyCurrent);
			updateKeyExpected();
			countFilled++;
			return;
		}

		/** before current batch : key expired */
		if (keyCurrent < keyOriginal) {
			countDuplicate++;
			return;
		}

		/** inside current batch : key already stored */
		if (keyCurrent < keyExpected) {
			countDuplicate++;
			return;
		}

		/** ahead of expected : keyCurrent > keyExpected */
		if (isPresent(offset)) {
			/** already stored */
			countDuplicate++;
			return;
		} else {
			/** new future entry */
			store(offset, value);
			updateKeyAdvanced(keyCurrent);
			countFilled++;
			return;
		}

	}

	protected final boolean isPresent(final int offset) {
		assert 0 <= offset && offset < storeArray.length;
		return storeArray[offset] != null;
	}

	/** point to first hole */
	protected void updateKeyExpected() {
		int offset = (int) (keyExpected - keyOriginal);
		do {
			offset++;
		} while (storeArray[offset] != null);
		keyExpected = keyOriginal + offset;
	}

	/** move forward */
	protected void updateKeyAdvanced(final long keyCurrent) {
		if (keyCurrent > keyAdvanced) {
			keyAdvanced = keyCurrent;
		}
	}

	protected void store(final int offset, final V value) {
		assert 0 <= offset && offset < storeArray.length;
		assert storeArray[offset] == null;
		storeArray[offset] = value;
		sizeStored++;
	}

	@Override
	public void reset(final long keyExpected) {
		this.keyOriginal = keyExpected;
		this.keyExpected = keyExpected;
		this.keyAdvanced = 0;
		resetStore();
	}

	@Override
	public boolean isReady() {

		/** not ready when empty */
		if (sizeStored == 0) {
			return false;
		}

		/** most expected scenario */
		if (sizeStored == 1 && keyExpected == (keyOriginal + 1)) {
			return true;
		}

		/** last received should be last in batch */
		if (keyAdvanced + 1 != keyExpected) {
			return false;
		}

		/** batch should have no holes */
		if (keyExpected - keyOriginal != sizeStored) {
			return false;
		}

		return true;

	}

	@Override
	public void drainTo(final Collection<V> list) {

		final int sizeStored = this.sizeStored;

		if (sizeStored == 0) {
			return;
		}

		this.sizeStored = 0;

		if (sizeStored > maxStoreSize) {
			maxStoreSize = sizeStored;
		}

		final long keyExpected = keyAdvanced + 1;

		final int range = (int) (keyExpected - keyOriginal);

		int countLost = 0;
		int countDrained = 0;

		for (int index = 0; index < range; index++) {

			final V value = storeArray[index];

			if (value == null) {
				countLost++;
			} else {
				countDrained++;
				list.add(value);
				storeArray[index] = null;
			}

		}

		if (countLost > 0) {
			numberForced++;
		}

		this.countLost += countLost;
		this.countDrained += countDrained;

		this.keyOriginal = keyExpected;
		this.keyExpected = keyExpected;
		this.keyAdvanced = 0;

	}

	protected void resetStore() {
		if (sizeStored != 0) {
			countLost += sizeStored;
			sizeStored = 0;
		}
		final int length = storeArray.length;
		for (int k = 0; k < length; k++) {
			storeArray[k] = null;
		}
	}

}
