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

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.x10.X10BindingProvider;
import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.types.Command;
import org.openhab.core.types.TypeParser;
import org.openhab.model.item.binding.AbstractGenericBindingProvider;
import org.openhab.model.item.binding.BindingConfigParseException;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for parsing the binding configuration.
 * 
 * @author Guido Zockoll
 * @since 1.2.0
 */
public class X10GenericBindingProvider extends AbstractGenericBindingProvider
		implements X10BindingProvider, ManagedService {
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
//		if (!(item instanceof SwitchItem || item instanceof DimmerItem)) {
//			throw new BindingConfigParseException(
//					"item '"
//							+ item.getName()
//							+ "' is of type '"
//							+ item.getClass().getSimpleName()
//							+ "', only Switch- and DimmerItems are allowed - please check your *.items configuration");
//		}
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
		parseConfig(item, bindingConfig);

	}

	private X10BindingConfig parseConfig(Item item, String s) {
		X10BindingConfig config = new X10BindingConfig(item);
		String[] parts = s.split(",");
		for (String part : parts) {
			Matcher m = pattern.matcher(part);
			if (m.matches()) {
				String ohCmdStr = m.group(1);
				String x10CmdStr = m.group(2);
				String x10Unit = m.group(3);
				Command command = TypeParser.parseCommand(
						item.getAcceptedCommandTypes(), ohCmdStr);
				config.addCommand(command, x10Unit,
						X10Command.valueOf(x10CmdStr));
				addBindingConfig(item, config);
			} else {
				logger.error(part + " does not match " + pattern.pattern()
						+ "', please check your *.items configuration");
			}
		}
		return config;
	}

	class X10BindingConfig implements BindingConfig {

		private Map<Command, X10Command> ohX10 = new HashMap<Command, X10Command>();
		private Map<X10Command, Command> x10Oh = new HashMap<X10Command, Command>();
		private Item item;
		private String unit;

		public X10BindingConfig(Item item) {
			super();
			this.item = item;
		}

		public void setUnit(String unit) {
			this.unit = unit;
		}

		public Item getItem() {
			return item;
		}

		public String getUnit() {
			return unit;
		}

		public void addCommand(Command key, String x10Unit, X10Command value) {
			if(StringUtils.isEmpty(unit))
				this.unit=x10Unit;
			else if(!unit.equalsIgnoreCase(x10Unit))
				logger.warn("All command mappings should belong to the same Unit");
			ohX10.put(key, value);
			x10Oh.put(value, key);
		}

		public X10Command get(Command command) {
			return ohX10.get(command);
		}

		public Command get(X10Command command) {
			return x10Oh.get(command);
		}

		public x10.Command getX10Command(Command command) {
			X10Command x10Command = ohX10.get(command);
			if (x10Command!=null)
				return new x10.Command(unit, x10Command.byteValue());
			else return null;
		}

		public Command getOHCommand(X10Command command) {
			return x10Oh.get(command);
		}
	}

	@Override
	public boolean supports(String itemName, Command command) {
		return getX10Command(itemName, command) != null;
	}

	@Override
	public x10.Command getX10Command(String itemName, Command command) {
		X10BindingConfig config = (X10BindingConfig) bindingConfigs
				.get(itemName);
		return config.getX10Command(command);
	}

	@Override
	public boolean supports(String unit,X10Command command) {
		return getOHCommand(unit,command)!=null;
	}

	@Override
	public Command getOHCommand(String unit,X10Command command) {
		for (BindingConfig config : bindingConfigs.values()) {
			X10BindingConfig binding=(X10BindingConfig) config;
			if (binding.getUnit().equals(unit) )
				return binding.getOHCommand(command);
		}
		return null;
	}

	@Override
	public void updated(Dictionary<String, ?> properties)
			throws ConfigurationException {
		logger.debug("Update received: " + properties);

	}

	@Override
	public Item getItem(String unit, X10Command x10Commmand) {
		for (BindingConfig config : bindingConfigs.values()) {
			X10BindingConfig binding=(X10BindingConfig) config;
			if (binding.getUnit().equals(unit) )
				return binding.getItem();
		}
		return null;
	}
}
