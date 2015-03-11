/*
 * Copyright (c) 2015 BalaBit IT Ltd, Budapest, Hungary
 * Copyright (c) 2015 Viktor Juhasz <viktor.juhasz@balabit.com>
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * As an additional exemption you are allowed to compile & link against the
 * OpenSSL libraries as published by the OpenSSL project. See the file
 * COPYING for details.
 *
 */

package org.syslog_ng.elasticsearch;

import org.syslog_ng.InternalMessageSender;
import org.syslog_ng.LogMessage;
import org.syslog_ng.LogTemplate;
import org.syslog_ng.StructuredLogDestination;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequestBuilder;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;

import static org.elasticsearch.node.NodeBuilder.*;

public class ElasticSearchDestination extends StructuredLogDestination {

	Client client;
	String cluster;
	String type;
	int port;
	String server;

	Settings settings;
	int msg_count;
	int flush_limit;
	BulkRequestBuilder bulkRequest;
	boolean opened;
	String clientMode;
	Node node;

	String messageTemplateString;
	LogTemplate messageTemplate;
	String indexTemplateString;
	LogTemplate indexTemplate;
	String customIdTemplateString;
	LogTemplate customIdTemplate;

	public ElasticSearchDestination(long handle) {
		super(handle);
		Logger.getRootLogger().setLevel(Level.OFF);
		port = 9300;
		server = "localhost";
		clientMode = "transport";
		flush_limit = 1;
		opened = false;
		messageTemplateString = "$(format-json --scope rfc5424 --exclude DATE --key ISODATE)";
	}

	@Override
	protected boolean init() {
		readOptions();
		createTemplates();
		return checkRequiredOptions() && compileTemplates() && initCustomId();
	}

	@Override
	protected boolean isOpened() {
		return opened;
	}

	@Override
	protected boolean open() {
		opened = initConnection();
		return opened;
	}

	@Override
	protected boolean send(LogMessage msg) {
		String formattedMessage = messageTemplate.format(msg);
		if (customIdTemplate != null) {
			bulkRequest.add(client.prepareIndex(indexTemplate.format(msg),
					type, customIdTemplate.format(msg)).setSource(
					formattedMessage));
		} else {
			bulkRequest.add(client
					.prepareIndex(indexTemplate.format(msg), type).setSource(
							formattedMessage));
		}
		msg_count++;

		if (msg_count >= flush_limit) {
			return flush();
		} else {
			return true;
		}
	}

	@Override
	protected void close() {
		if (opened) {
			flush();
			client.close();
			if (node != null) {
				node.close();
			}
			opened = false;
		}
	}

	@Override
	protected void deinit() {
		flush();
		messageTemplate.release();
		indexTemplate.release();
		if (customIdTemplate != null) {
			customIdTemplate.release();
		}
	}

	private boolean flush() {
		boolean result = true;
		if ((bulkRequest != null) && (bulkRequest.numberOfActions() > 0)) {
			BulkResponse bulkResponse = (BulkResponse) bulkRequest.execute()
					.actionGet();
			if (bulkResponse.hasFailures()) {
				InternalMessageSender.error("Bulk insert failed: "
						+ bulkResponse.buildFailureMessage());
				result = false;
			}
			bulkRequest = client.prepareBulk();
			msg_count = 0;
		}
		return result;

	}

	private boolean initConnection() {
		try {
			client = createClient();
			InternalMessageSender.debug("initializing...");
			ClusterHealthRequestBuilder healthRequest = client.admin().cluster().prepareHealth(null);
			healthRequest.setTimeout("5");
			healthRequest.setWaitForGreenStatus();
			ClusterHealthResponse response = (ClusterHealthResponse) healthRequest
					.execute().actionGet();
			if (response.isTimedOut()) {
				InternalMessageSender.debug("Failed to wait for green");
				InternalMessageSender.debug("Wait for read yellow status...");
				healthRequest = client.admin().cluster().prepareHealth(null);
				healthRequest.setTimeout("5");
				healthRequest.setWaitForYellowStatus();
				response = (ClusterHealthResponse) healthRequest.execute()
						.actionGet();
				if (response.isTimedOut()) {
					InternalMessageSender.debug("Timedout");
					throw new ElasticsearchException(
							"Can't connect to cluster: " + cluster);
				}
			}
			InternalMessageSender.debug("Ok");
			bulkRequest = client.prepareBulk();
		} catch (ElasticsearchException e) {
			System.out.println("Something evil happened: " + e.getMessage());
			e.printStackTrace(System.out);
			return false;
		}
		return true;
	}

	private void readOptions() {
		indexTemplateString = getOption("index");
		customIdTemplateString = getOption("custom_id");
		type = getOption("type");
		cluster = getOption("cluster");
		String localserver = getOption("server");
		String localport = getOption("port");
		String localflush_limit = getOption("flush_limit");
		String localMessageTemplateString = getOption("message_template");
		String localClientMode = getOption("client_mode");
		if (localserver != null) {
			server = localserver;
		}
		if (localport != null) {
			port = Integer.parseInt(localport);
		}
		if (localflush_limit != null) {
			flush_limit = Integer.parseInt(localflush_limit);
		}
		if (localMessageTemplateString != null) {
			messageTemplateString = localMessageTemplateString;
		}
		if (localClientMode != null) {
			clientMode = localClientMode;
		}
	}

	private boolean checkRequiredOptions() {
		boolean result = true;
		if (cluster == null) {
			InternalMessageSender.error("Required option is missing: cluster");
			result = false;
		}
		if (indexTemplateString == null) {
			InternalMessageSender.error("Required option is missing: index");
			result = false;
		}

		if (type == null) {
			InternalMessageSender.error("Required option is missing: type");
			result = false;
		}

		return result;
	}

	private boolean compileTemplates() {
		return messageTemplate.compile(messageTemplateString)
				&& indexTemplate.compile(indexTemplateString);
	}

	private void createTemplates() {
		messageTemplate = new LogTemplate(getConfigHandle());
		indexTemplate = new LogTemplate(getConfigHandle());
	}

	private boolean initCustomId() {
		if (customIdTemplateString != null) {
			customIdTemplate = new LogTemplate(getConfigHandle());
			return customIdTemplate.compile(customIdTemplateString);
		}
		return true;
	}

	private Client createClient() {
		if (clientMode == "transport") {
			return createTransportClient();
		} else if (clientMode == "node") {
			return createNodeClient();
		}
		throw new ElasticsearchException("Invalid client mode: " + clientMode);
	}

	private Client createTransportClient() {
		settings = ImmutableSettings.settingsBuilder()
				.put("client.transport.sniff", true)
				.put("cluster.name", cluster)
				.classLoader(Settings.class.getClassLoader()).build();

		TransportClient transport = new TransportClient(settings);

		transport.addTransportAddress(new InetSocketTransportAddress(server,
				port));

		return transport;
	}

	private Client createNodeClient() {
		node = nodeBuilder().node();
		return node.client();
	}
}
