package com.barchart.netty.util.entry;

import java.util.HashMap;
import java.util.Map;

public abstract class Entry {

	public static final String KEY_ID = "id";

	protected final Map<String, Object> props = new HashMap<String, Object>();

	public Map<String, Object> props() {
		return props;
	}

	//

	public boolean isValidId() {
		return isValid(getId());
	}

	public boolean isValid(final CharSequence text) {
		return text != null && text.length() > 0;
	}

	public String getId() {
		return load(KEY_ID);
	}

	public void setId(final String id) {
		save(KEY_ID, id);
	}

	//

	@SuppressWarnings("unchecked")
	public <T> T load(final String key) {
		if (key == null) {
			return null;
		}
		return (T) props.get(key);
	}

	public <T> void save(final String key, final T value) {
		props.put(key, value);
	}

	public boolean hasInt(final String key) {
		if (key == null) {
			return false;
		}
		final Object value = load(key);
		if (value instanceof Number) {
			return true;
		}
		return false;
	}

	public int getInt(final String key) {
		final Object value = load(key);
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		return 0;
	}

	@Override
	public String toString() {
		return props.toString();
	}

}
