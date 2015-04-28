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
import org.syslog_ng.LogTemplate;

public class TemplateOptionDecorator extends OptionDecorator {
	private LogTemplate template;
	private String strTemplate;
	
	public TemplateOptionDecorator(Option decoratedOption) {
		super(decoratedOption);
	}
	
	@Override
	public void init() throws OptionException {
		decoratedOption.init();
		strTemplate = decoratedOption.getValue();
		if (strTemplate != null) {
			template = new LogTemplate(getConfigHandle());
			if(!template.compile(strTemplate)) {
				throw new OptionException("Can't compile template: '" + strTemplate + "'");
			}
		}
	}
	
	@Override
	public void deinit() {
		if (template != null)
			template.release();
		decoratedOption.deinit();
	}
	
	@Override
	public String getResolvedString(LogMessage msg) {
		if (template != null)
			return template.format(msg);
		return decoratedOption.getResolvedString(msg);
	}

}
