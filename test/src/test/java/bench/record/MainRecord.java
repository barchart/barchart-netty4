/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package bench.record;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.proto.buf.wrap.PacketWrapper;
import com.barchart.proto.buf.wrap.PacketWrapper.Builder;

public class MainRecord {

	final static Logger log = LoggerFactory.getLogger(MainRecord.class);

	public static void main(final String... args) throws Exception {

		log.info("init");

		final String folder = "./target";
		final String file = "record.reader-002.1348079755714.buf";

		final File path = new File(folder, file);

		final InputStream input = new FileInputStream(path);

		while (true) {

			final Builder wrapper = PacketWrapper.newBuilder();

			if (wrapper.mergeDelimitedFrom(input)) {
				log.info("message : \n{}", wrapper.build());
			} else {
				break;
			}

		}

		log.info("done");

	}

}
