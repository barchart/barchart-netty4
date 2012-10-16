package com.barchart.netty.matrix.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.barchart.netty.matrix.api.MatrixConfig;

/** TODO */
public class MatrixConfigEntry implements MatrixConfig {

	@Override
	public boolean isActive() {
		// return load(KEY_ACTIVE);
		return false;
	}

	@Override
	public CharSequence getSourceId() {
		// return load(KEY_SOURCE);
		return "";
	}

	@Override
	public CharSequence getTargetId() {
		// return load(KEY_TARGET);
		return "";
	}

	public void setActive(final boolean isActive) {
		// save(KEY_ACTIVE, isActive);
	}

	public void setSourceId(final CharSequence sourceId) {
		// save(KEY_SOURCE, sourceId);
	}

	public void setTargetId(final CharSequence targetId) {
		// save(KEY_SOURCE, targetId);
	}

	public static MatrixConfigEntry from(final Map<String, Object> map) {
		final MatrixConfigEntry entry = new MatrixConfigEntry();
		// entry.props().putAll(map);
		return entry;
	}

	public static MatrixConfigEntry form(final com.typesafe.config.Config conf) {
		return from(conf.root().unwrapped());
	}

	public static List<MatrixConfigEntry> form(
			final List<? extends com.typesafe.config.Config> confList) {

		final List<MatrixConfigEntry> entryList = new ArrayList<MatrixConfigEntry>(
				confList.size());

		for (final com.typesafe.config.Config conf : confList) {
			final MatrixConfigEntry entry = MatrixConfigEntry.form(conf);
			entryList.add(entry);
		}

		return entryList;

	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

}
