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
package org.openhab.binding.x10;

import org.openhab.binding.x10.internal.X10Command;
import org.openhab.core.binding.BindingProvider;
import org.openhab.core.items.Item;
import org.openhab.core.types.Command;

/**
 * Provides mapping from openHAB commands to X10 commands and vice versa
 * 
 * @author Guido Zockoll
 * @since 1.2.0
 */
public interface X10BindingProvider extends BindingProvider {

	public abstract boolean supports(String itemName,Command command);

	public abstract Command getOHCommand(String unitName,X10Command command);

	boolean supports(String unitName, X10Command x10Commmand);

	public abstract x10.Command getX10Command(String itemName, Command command);

	public abstract Item getItem(String unit, X10Command x10Commmand);
}
