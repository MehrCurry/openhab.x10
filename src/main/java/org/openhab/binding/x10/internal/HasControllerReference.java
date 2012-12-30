package org.openhab.binding.x10.internal;

import x10.Controller;


public interface HasControllerReference {
	void setController(Controller controller);
	void unsetController(Controller controller);
}
