/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.matrix.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.netty.matrix.api.Matrix;
import com.barchart.netty.matrix.api.MatrixConfig;
import com.barchart.netty.matrix.api.MatrixTarget;

/** one per bundle */
@Component(servicefactory = true)
public class MatrixProvider implements Matrix {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@SuppressWarnings("serial")
	static class ConfigList extends CopyOnWriteArrayList<CharSequence> {
	}

	@SuppressWarnings("serial")
	static class ConfigMap extends ConcurrentHashMap<CharSequence, ConfigList> {
	}

	@SuppressWarnings("serial")
	static class HandlerMap extends
			ConcurrentHashMap<CharSequence, MatrixTarget> {
	}

	/** bound handlers */
	private final HandlerMap handlerMap = new HandlerMap();

	/** handler configuration */
	private final ConfigMap configMap = new ConfigMap();

	@Override
	public synchronized void configCleanup(final String targetId) {

		for (final ConfigList list : configMap.values()) {
			list.remove(targetId);
		}

	}

	@Override
	public synchronized void configApply(final MatrixConfig config) {

		if (config == null || !config.isValid()) {
			return;
		}

		final CharSequence sourceId = config.getSourceId();
		final CharSequence targetId = config.getTargetId();

		ConfigList targetList = configMap.get(sourceId);

		if (config.isActive()) {

			if (targetList == null) {
				targetList = new ConfigList();
				configMap.put(sourceId, targetList);
			}

			targetList.addIfAbsent(targetId);

		} else {

			if (targetList == null) {
				return;
			}

			targetList.remove(targetId);

			if (targetList.isEmpty()) {
				configMap.remove(sourceId);
			}

		}

	}

	@Override
	public void process(final String sourceId, final Object message) {

		log.debug("sourceId : {} ", sourceId);

		if (sourceId == null || message == null) {
			return;
		}

		final ConfigList targetList = configMap.get(sourceId);

		if (targetList == null) {
			return;
		}

		for (final CharSequence targetId : targetList) {

			final MatrixTarget handler = handlerMap.get(targetId);

			if (handler == null) {
				continue;
			}

			handler.process(message);

		}

	}

	private String targetFilter;

	protected boolean isRecognized(final MatrixTarget handler) {

		if (targetFilter == null || handler == null) {
			return false;
		}

		return targetFilter.equals(handler.getFilter());

	}

	@Reference( //
	policy = ReferencePolicy.DYNAMIC, //
	cardinality = ReferenceCardinality.MULTIPLE //
	)
	protected void bind(final MatrixTarget handler) {

		if (!isRecognized(handler)) {
			return;
		}

		final String targetId = handler.getId();

		handlerMap.put(targetId, handler);

	}

	protected void unbind(final MatrixTarget handler) {

		if (!isRecognized(handler)) {
			return;
		}

		final String targetId = handler.getId();

		handlerMap.remove(targetId);

	}

	@Override
	public void setTargetFilter(final String targetFilter) {

		this.targetFilter = targetFilter;

	}

	@Activate
	protected void activate() {
		log.debug("activate");
	}

	@Deactivate
	protected void deactivate() {
		log.debug("deactivate");
	}

}
