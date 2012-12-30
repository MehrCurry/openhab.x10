/**
 * openHAB, the open Home Automation Bus.
 * Copyright (C) 2010-2012, openHAB.org <admin@openhab.org>
 *
 * See the contributors.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or
 * combining it with Eclipse (or a modified version of that library),
 * containing parts covered by the terms of the Eclipse Public License
 * (EPL), the licensors of this Program grant you additional permission
 * to convey the resulting work.
 */
package org.openhab.binding.x10.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.x10.X10BindingProvider;
import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.types.Command;
import org.openhab.core.types.TypeParser;
import org.openhab.model.item.binding.AbstractGenericBindingProvider;
import org.openhab.model.item.binding.BindingConfigParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for parsing the binding configuration.
 * 
 * @author Guido Zockoll
 * @since 1.2.0
 */
public class X10GenericBindingProvider extends AbstractGenericBindingProvider
		implements X10BindingProvider {
	private static final Pattern basePattern = Pattern
			.compile("([<|>])((.*),?)+");
	private static final Pattern pattern = Pattern
			.compile("([a-zA-Z]*):([a-zA-Z]*)@([a-zA-Z]\\d+)");

	private static final Logger logger = LoggerFactory
			.getLogger(X10GenericBindingProvider.class);

	/**
	 * {@inheritDoc}
	 */
	public String getBindingType() {
		return "x10";
	}

	/**
	 * @{inheritDoc
	 */
	public void validateItemType(Item item, String bindingConfig)
			throws BindingConfigParseException {
		logger.info("validate item: " + item + ":" + bindingConfig);
		if (!(item instanceof SwitchItem || item instanceof DimmerItem)) {
			throw new BindingConfigParseException(
					"item '"
							+ item.getName()
							+ "' is of type '"
							+ item.getClass().getSimpleName()
							+ "', only Switch- and DimmerItems are allowed - please check your *.items configuration");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processBindingConfiguration(String context, Item item,
			String bindingConfig) throws BindingConfigParseException {
		super.processBindingConfiguration(context, item, bindingConfig);
		logger.info("process binding configuration: " + context + ":" + item
				+ ":" + bindingConfig);
		Matcher m = basePattern.matcher(bindingConfig);
		X10BindingConfig config;
		if (m.matches()) {
			String direction = m.group(1);
			if (">".equals(direction))
				config = parseOutConfig(item, bindingConfig.substring(1));
			else if ("<".equals(direction))
				config = parseInConfig(item, bindingConfig.substring(1));
			else
				throw new BindingConfigParseException(
						"Unknown direction qualifier in " + bindingConfig);
		}
	}

	private X10BindingConfig parseInConfig(Item item, String substring) {
		// TODO Auto-generated method stub
		return null;
	}

	private X10BindingConfig parseOutConfig(Item item, String s) {
		X10BindingConfig config = new X10BindingConfig();
		String[] parts = s.split(",");
		for (String part : parts) {
			Matcher m = pattern.matcher(part);
			if (m.matches()) {
				String ohCmdStr = m.group(1);
				String x10CmdStr = m.group(2);
				String x10Unit = m.group(3);
				Command command = TypeParser.parseCommand(
						item.getAcceptedCommandTypes(), ohCmdStr);
				config.put(command, new x10.Command(x10Unit,
						Commands.valueOf(x10CmdStr).byteValue()));
				addBindingConfig(item, config);
			} else {
				logger.error(part + " does not match " + pattern.pattern()
						+ "', please check your *.items configuration");
			}
		}
		return config;
	}

	public static enum Commands {
		ON(x10.Command.ON), OFF(x10.Command.OFF), DIM(x10.Command.DIM), BRIGHT(
				x10.Command.BRIGHT);

		private byte value;

		private Commands(byte b) {
			this.value = b;
		}

		public byte byteValue() {
			return value;
		}
	}

	class X10BindingConfig extends HashMap<Command, x10.Command> implements
			BindingConfig {

		/**
		 * 
		 */
		private static final long serialVersionUID = 7465718376448328932L;
	}

	@Override
	public boolean supports(String itemName, Command command) {
		return getX10Command(itemName, command)!=null;
	}

	@Override
	public x10.Command getX10Command(String itemName, Command command) {
		X10BindingConfig config = (X10BindingConfig) bindingConfigs
				.get(itemName);
		return config.get(command);
	}
}
