package org.revenj;

import java.util.Arrays;

public final class TreePath {
	private final String value;
	private final String[] parts;

	public static final TreePath EMPTY = new TreePath("", new String[0]);

	public TreePath(String value) {
		if (value == null || value.length() == 0) {
			this.value = "";
			this.parts = new String[0];
		} else {
			this.value = value;
			this.parts = value.split("\\.");
			checkParts(parts);
		}
	}

	public static TreePath create(String value) {
		if (value == null || value.length() == 0) {
			return EMPTY;
		}
		String[] parts = value.split("\\.");
		checkParts(parts);
		return new TreePath(value, parts);
	}

	private TreePath(String value, String[] parts) {
		this.value = value;
		this.parts = parts;
	}

	private static void checkParts(String[] parts) {
		for (String p : parts) {
			for (int j = 0; j < p.length(); j++) {
				if (!Character.isLetterOrDigit(p.charAt(j))) {
					throw new IllegalArgumentException("Invalid value for part: " + p + ". Only letters and digits allowed for labels");
				}
			}
		}
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof TreePath && ((TreePath) obj).value.equals(this.value)
				|| obj instanceof String && obj.equals(this.value);
	}

	@Override
	public String toString() {
		return value;
	}

	public TreePath plus(TreePath other) {
		if (other == null) return this;
		if (this.value.length() == 0) {
			return other;
		} else if (other.value.length() == 0) {
			return this;
		}
		String[] values = Arrays.copyOf(this.parts, this.parts.length + other.parts.length);
		for (int i = 0; i < other.parts.length; i++) {
			values[this.parts.length + i] = other.parts[i];
		}
		return new TreePath(this.value + "." + other.value, values);
	}

	private static boolean compare(String[] left, String[] right, int count) {
		for (int i = 0; i < count; i++) {
			if (!left[i].equals(right[i])) return false;
		}
		return true;
	}

	public boolean isAncestor(TreePath other) {
		if (other == null) return false;
		if (this.value.equals(other.value)) return true;
		return this.parts.length < other.parts.length && compare(this.parts, other.parts, this.parts.length);
	}

	public boolean isDescendant(TreePath other) {
		if (other == null) return false;
		if (this.value.equals(other.value)) return true;
		return this.parts.length > other.parts.length && compare(this.parts, other.parts, other.parts.length);
	}
}
