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

import org.syslog_ng.LogMessage;

public abstract class OptionDecorator implements Option {
	
	protected Option decoratedOption;

	public OptionDecorator(Option decoratedOption) {
		this.decoratedOption = decoratedOption;
	}

	public String getValue() {
		return decoratedOption.getValue();
	}

	public void init() throws OptionException {
		decoratedOption.init();
	}

	public void deinit() {
		decoratedOption.deinit();
	}
	
	public String getName() {
		return decoratedOption.getName();
	}

	public String getResolvedString(LogMessage msg) {
		return decoratedOption.getResolvedString(msg);
	}
	
	public long getConfigHandle() {
		return decoratedOption.getConfigHandle();
	}
	
	public Integer getValueAsInterger() {
		return decoratedOption.getValueAsInterger();
	}

	public String[] getValueAsStringList(String seporator) {
		return decoratedOption.getValueAsStringList(seporator);
	}

}
