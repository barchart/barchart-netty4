package com.barchart.netty.guice.http;

import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.net.SocketAddress;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.netty.common.util.AddressUtil;
import com.barchart.netty.server.HandlerFactory;
import com.barchart.netty.server.Servers;
import com.barchart.netty.server.http.HttpServer;
import com.barchart.netty.server.http.error.DefaultErrorHandler;
import com.barchart.netty.server.http.error.ErrorHandler;
import com.barchart.netty.server.http.request.RequestHandler;
import com.barchart.netty.server.util.SingleHandlerFactory;
import com.barchart.util.guice.Activatable;
import com.barchart.util.guice.Component;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.typesafe.config.Config;

@Component("com.barchart.netty.guice.http")
public class HttpServiceProvider implements HttpService, Activatable {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final HttpServer server;

	// limit number of boss to 1 per cpu, number of workers 2 per cpu
	private final EventLoopGroup bossGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());
	private final EventLoopGroup workerGroup = new NioEventLoopGroup();

	@Inject
	@Named("#")
	private Config config;

	protected HttpServiceProvider() {
		server = Servers.createHttpServer()
				.group(bossGroup)
				.childGroup(workerGroup)
				.errorHandler(errorHandler());
	}

	@Inject(optional = true)
	protected void httpServices(final Set<HttpRequestHandler> services) {
		for (final HttpRequestHandler hs : services) {
			registerHandler(hs.path(), new SingleHandlerFactory<RequestHandler>(hs));
		}
	}

	@Inject(optional = true)
	protected void webSocketServices(final Set<WebSocketRequestHandler> services) {
		for (final WebSocketRequestHandler ws : services) {
			registerWebSocket(ws.path(), new SingleHandlerFactory<ChannelHandler>(ws));
		}
	}


	@Override
	public void activate() throws Exception {

		if (config.hasPath("max-request-size")) {
			server.maxRequestSize(config.getInt("max-request-size"));
		}

		if (config.hasPath("max-connections")) {
			server.maxConnections(config.getInt("max-connections"));
		}

		String requestLogger = null;
		if (config.hasPath("request-logger")) {
			requestLogger = config.getString("request-logger");
		}

		String errorLogger = null;
		if (config.hasPath("error-logger")) {
			errorLogger = config.getString("error-logger");
		}

		server.logger(new SLF4JRequestLogger(requestLogger, errorLogger));

		if (config.hasPath("local-address")) {

			final SocketAddress address = AddressUtil.parseAddress(config.getString("local-address"));

			log.debug("Starting HTTP server on " + address);
			server.listen(address).sync();

		} else {

			log.debug("Starting HTTP server on *:8080");
			server.listen(8080).sync();

		}

	}

	public void deactivate() {

		if (server.running()) {

			log.debug("Stopping HTTP server");

			try {
				server.kill().sync();
				bossGroup.shutdownGracefully().sync();
				workerGroup.shutdownGracefully().sync();
			} catch (final Throwable t) {
				log.error("Could not shutdown HTTP server", t);
			}

		} else {
			log.debug("HTTP server not running!");
		}

	}

	@Override
	public void registerHandler(final String path, final HandlerFactory<RequestHandler> factory) {
		server.requestHandler(path, factory);
		log.debug("Registered handler for " + path + ": " + factory);
	}

	@Override
	public void registerWebSocket(final String path, final HandlerFactory<? extends ChannelHandler> factory) {
		server.webSocketHandler(path, factory);
		log.debug("Registered websocket service for " + path + ": " + factory);
	}

	@Override
	public void unregisterHandler(final String path) {
		server.removeRequestHandler(path);
		server.removeWebSocketHandler(path);
		log.debug("Unregistered handler for " + path);
	}

	private ErrorHandler errorHandler() {
		return new DefaultErrorHandler();
	}

}
