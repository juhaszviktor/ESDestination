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

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.unit.TimeValue;

public class ESBulkMessageProcessor extends ESMessageProcessor {
	private BulkProcessor bulkProcessor;
	
	public ESBulkMessageProcessor(ElasticSearchOptions options, ESClient client) {
		super(options, client);
		bulkProcessor = BulkProcessor.builder(
				client.getClient(),
				new BulkProcessor.Listener() {
					public void beforeBulk(long executionId,
						BulkRequest request) {
					}

					public void afterBulk(long executionId,
						BulkRequest request,
						BulkResponse response) {
					}

					public void afterBulk(long executionId,
						BulkRequest request,
						Throwable failure) {
						System.out.println("After bulk failed: " + failure);
					}
				}
			)
			.setBulkActions(options.getOption(ElasticSearchOptions.FLUSH_LIMIT).getValueAsInterger())
			.setFlushInterval(new TimeValue(1000))
			.setConcurrentRequests(1)
			.build();
	}

	@Override
	public boolean send(IndexRequest req) {
		bulkProcessor.add(req);
		return true;
	}
	
	@Override
	public void flush() {
		bulkProcessor.flush();
	}
	
	@Override
	public void close() {
		bulkProcessor.close();
	}

}
