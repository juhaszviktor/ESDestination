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

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequestBuilder;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.Client;
import org.syslog_ng.InternalMessageSender;

public abstract class ESClient {
	private Client client;
	protected ElasticSearchOptions options;
	
	public ESClient(ElasticSearchOptions options) {
		this.options = options;
	}
	
	public void initConnection() throws ElasticsearchException {
		InternalMessageSender.debug("initializing...");
		ClusterHealthRequestBuilder healthRequest = client.admin().cluster().prepareHealth();
		healthRequest.setTimeout("5");
		healthRequest.setWaitForGreenStatus();
		ClusterHealthResponse response = (ClusterHealthResponse) healthRequest
				.execute().actionGet();
		if (response.isTimedOut()) {
			InternalMessageSender.debug("Failed to wait for green");
			InternalMessageSender.debug("Wait for read yellow status...");
			healthRequest = client.admin().cluster().prepareHealth();
			healthRequest.setTimeout("5");
			healthRequest.setWaitForYellowStatus();
			response = (ClusterHealthResponse) healthRequest.execute()
					.actionGet();
			if (response.isTimedOut()) {
				InternalMessageSender.debug("Timedout");
				throw new ElasticsearchException(
						"Can't connect to cluster: " + options.getOption(ElasticSearchOptions.CLUSTER).getValue());
			}
		}
		InternalMessageSender.debug("Ok");
	}
	
	
	public final boolean open() {
		client = createClient();
		try {
			initConnection();
		}
		catch (ElasticsearchException e) {
			return false;
		}
		return true;
	}
	
	public abstract void close();
	
	public abstract Client createClient();
	
	public Client getClient() {
		return client;
	}
}
