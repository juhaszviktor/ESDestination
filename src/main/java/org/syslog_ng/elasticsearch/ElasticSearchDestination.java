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
import org.syslog_ng.StructuredLogDestination;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.elasticsearch.action.index.IndexRequest;

public class ElasticSearchDestination extends StructuredLogDestination {

	ESClient client;
	ESMessageProcessor msgProcessor;
	
	boolean opened;
	

	ElasticSearchOptions options;
	
	public ElasticSearchDestination(long handle) {
		super(handle);
		Logger.getRootLogger().setLevel(Level.OFF);		
		opened = false;
		options = new ElasticSearchOptions(this);
		client = ESClientFactory.getESClient(options);
		msgProcessor = ESMessageProcessorFactory.getMessageProcessor(options, client);
	}

	@Override
	protected boolean init() {
		boolean result = false;
		try {
			options.init();
			result = true;
		}
		catch (OptionException e){
			InternalMessageSender.error(e.getMessage());
		}
		open();
		InternalMessageSender.debug("Init done");
		return result;
	}

	@Override
	protected boolean isOpened() {
		return opened;
	}

	@Override
	protected boolean open() {
		opened = client.open();
		return opened;
	}
	
    protected IndexRequest createIndexRequest(LogMessage msg) {
    	String formattedMessage = options.getOption(ElasticSearchOptions.MESSAGE_TEMPLATE).getResolvedString(msg);
		String customId = options.getOption(ElasticSearchOptions.CUSTOM_ID).getResolvedString(msg);
		String index = options.getOption(ElasticSearchOptions.INDEX).getResolvedString(msg);
		String type = options.getOption(ElasticSearchOptions.TYPE).getResolvedString(msg);
	    return new IndexRequest(index, type, customId).source(formattedMessage);
    }

	@Override
	protected boolean send(LogMessage msg) {
		return msgProcessor.process(createIndexRequest(msg));
	}

	@Override
	protected void close() {
		if (opened) {
			msgProcessor.flush();
			msgProcessor.close();
			client.close();
			opened = false;
		}
	}

	@Override
	protected void deinit() {
		options.deinit();
	}
}
