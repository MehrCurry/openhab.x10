package org.openhab.binding.x10.internal;

public enum X10Command {
	ON(x10.Command.ON), OFF(x10.Command.OFF), DIM(x10.Command.DIM), BRIGHT(
			x10.Command.BRIGHT);

	private byte value;

	private X10Command(byte b) {
		this.value = b;
	}

	public byte byteValue() {
		return value;
	}

	public static X10Command fromFunction (byte functionByte) {
		for (X10Command c : values()) {
			if (c.value == functionByte)
				return c;
		}
		return null;
	}
}