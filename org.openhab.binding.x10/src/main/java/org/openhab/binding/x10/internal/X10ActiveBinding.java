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

import org.openhab.binding.x10.X10BindingProvider;

import org.apache.commons.lang.StringUtils;
import org.openhab.core.binding.AbstractActiveBinding;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.TypeParser;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import x10.Controller;
import x10.UnitEvent;
import x10.UnitListener;

/**
 * Implement this class if you are going create an actively polling service like
 * querying a Website/Device.
 * 
 * @author Guido Zockoll
 * @since 1.2.0
 */
public class X10ActiveBinding extends AbstractActiveBinding<X10BindingProvider>
		implements ManagedService, UnitListener,HasControllerReference {

	private static final Logger logger = LoggerFactory
			.getLogger(X10ActiveBinding.class);

	private boolean isProperlyConfigured = false;

	/**
	 * the refresh interval which is used to poll values from the X10 server
	 * (optional, defaults to 60000ms)
	 */
	private long refreshInterval = 60000;

	private Controller controller=null;

	private ItemRegistry itemRegistry;

	public X10ActiveBinding() {
	}

	public void setItemRegistry(ItemRegistry itemRegistry) {
		this.itemRegistry = itemRegistry;
	}
	
	public void unsetItemRegistry(ItemRegistry itemRegistry) {
		this.itemRegistry = null;
	}

	public void activate() {
		logger.info("Activate");
	}

	public void deactivate() {
		logger.info("Deactivete");
		// deallocate Resources here that are no longer needed and
		// should be reset when activating this binding again
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected long getRefreshInterval() {
		return refreshInterval;
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected String getName() {
		return "X10 Refresh Service";
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	public boolean isProperlyConfigured() {
		return isProperlyConfigured;
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected void execute() {
		logger.info("execute()");
		// the frequently executed code goes here ...
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	public void updated(Dictionary<String, ?> config)
			throws ConfigurationException {
		if (config != null) {
			String refreshIntervalString = (String) config.get("refresh");
			if (StringUtils.isNotBlank(refreshIntervalString)) {
				refreshInterval = Long.parseLong(refreshIntervalString);
			}

			// read further config parameters here ...

			isProperlyConfigured = true;
		}
	}

	private void processEvent(UnitEvent event) {
		logger.debug("Event occurred: " + event);
		String unit = "" + event.getCommand().getHouseCode() + event.getCommand().getUnitCode();
		for (X10BindingProvider provider:providers) {
			X10Command x10Commmand = X10Command.fromFunction(event.getCommand().getFunctionByte());
			if (provider.supports(unit,x10Commmand)) {
				Item item=provider.getItem(unit,x10Commmand);
				Command command=provider.getOHCommand(unit, x10Commmand);
				eventPublisher.postUpdate(item.getName(), (State) command);
			}
		}
	}

	@Override
	public void allLightsOff(UnitEvent event) {
		processEvent(event);
	}

	@Override
	public void allLightsOn(UnitEvent event) {
		processEvent(event);
	}

	@Override
	public void allUnitsOff(UnitEvent event) {
		processEvent(event);
	}

	@Override
	public void unitBright(UnitEvent event) {
		processEvent(event);
	}

	@Override
	public void unitDim(UnitEvent event) {
		processEvent(event);
	}

	@Override
	public void unitOff(UnitEvent event) {
		processEvent(event);
	}

	@Override
	public void unitOn(UnitEvent event) {
		processEvent(event);
	}

	@Override
	public synchronized void setController(Controller controller) {
		this.controller = controller;
		controller.addUnitListener(this);
	}

	@Override
	public synchronized void unsetController(Controller controller) {
		controller.removeUnitListener(this);
		this.controller = null;
	}
	
	/**
	 * Returns the {@link Item} for the given <code>itemName</code> or 
	 * <code>null</code> if there is no or to many corresponding Items
	 * 
	 * @param itemName
	 * 
	 * @return the {@link Item} for the given <code>itemName</code> or 
	 * <code>null</code> if there is no or to many corresponding Items
	 */
	private Item getItemFromItemName(String itemName) {
		try {
			return itemRegistry.getItem(itemName);
		} catch (ItemNotFoundException e) {
			logger.error("couldn't find item for itemName '" + itemName + "'");
		}	
		return null;
	}

}
