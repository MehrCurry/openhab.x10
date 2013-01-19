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
import org.openhab.core.events.AbstractEventSubscriberBinding;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import x10.Controller;

/**
 * Implement this class if you binding should react to openHAB events.
 * 
 * @author Guido Zockoll
 * @since 1.2.0
 */
public class X10EventSubscriberBinding extends
		AbstractEventSubscriberBinding<X10BindingProvider> implements
		HasControllerReference, ManagedService {

	private static final Logger logger = LoggerFactory
			.getLogger(X10EventSubscriberBinding.class);
	private Controller controller;

	public X10EventSubscriberBinding() {
	}

	public void activate() {
		logger.info("Activate");
	}

	public void deactivate() {
		logger.info("Deactivate");
		// deallocate Resources here that are no longer needed and
		// should be reset when activating this binding again
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected void internalReceiveCommand(String itemName, Command command) {
		logger.info("receive command: " + itemName + ":" + command);
		X10BindingProvider provider = findFirstMatchingBindingProvider(
				itemName, command);
		if (provider != null) {
			x10.Command x10 = provider.getX10Command(itemName, command);
			controller.addCommand(x10);
		}

	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected void internalReceiveUpdate(String itemName, State newState) {
		logger.info("receive update: " + itemName + ":" + newState);
		// No further action is taken since all state changes are comming from the X10 controller itself
	}

	/**
	 * Find the first matching {@link X10BindingProvider} according to
	 * <code>itemName</code> and <code>command</code>.
	 * 
	 * @param itemName
	 * @param command
	 * 
	 * @return the matching binding provider or <code>null</code> if no binding
	 *         provider could be found
	 */
	private X10BindingProvider findFirstMatchingBindingProvider(
			String itemName, Command command) {

		X10BindingProvider firstMatchingProvider = null;

		for (X10BindingProvider provider : this.providers) {

			if (provider.supports(itemName, command)) {
				firstMatchingProvider = provider;
				break;
			}
		}

		return firstMatchingProvider;
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	public synchronized void setController(Controller controller) {
		this.controller = controller;
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	public synchronized void unsetController(Controller controller) {
		this.controller = null;
	}

	/**
	 * @{inheritDoc
	 */

	@Override
	public void updated(Dictionary<String, ?> arg0)
			throws ConfigurationException {
		// Currently no configuration needed

	}
}
