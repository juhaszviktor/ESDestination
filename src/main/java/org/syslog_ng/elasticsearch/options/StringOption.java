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

package org.syslog_ng.elasticsearch.options;

import org.syslog_ng.LogDestination;
import org.syslog_ng.LogMessage;

public class StringOption implements Option {
	private LogDestination owner;
	private String name;
	private String defaultValue;

	public StringOption(LogDestination owner, String name, String defaultValue) {
		this.owner = owner;
		this.name = name;
		this.defaultValue = defaultValue;
	}

	public StringOption(LogDestination owner, String name) {
		this(owner, name, null);
	}

	public String getValue() {
		String result = defaultValue;
		String value = owner.getOption(name);
		if (value != null) {
			result = value;
		}
		return result;
	}

	public void validate() throws InvalidOptionException {
	}

	public void deinit() {
	}

	public String getResolvedString(LogMessage msg) {
		return getValue();
	}

	public String getName() {
		return name;
	}

	public Integer getValueAsInterger() {
		try {
			return Integer.parseInt(getValue());
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	public String[] getValueAsStringList(String seporator) {
		String value = getValue();
		return value == null ? null : value.split(seporator);
	}
}
