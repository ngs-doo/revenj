package org.revenj;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class TestCommon {

	@Test
	public void checkTreePathMethods() throws IOException {
		TreePath t1 = TreePath.create("a.b.c");
		TreePath t2 = TreePath.create("a.b");
		Assert.assertTrue(t1.isDescendant(t2));
		Assert.assertFalse(t2.isDescendant(t1));
		Assert.assertTrue(t2.isDescendant(t2));
		Assert.assertFalse(t1.isAncestor(t2));
		Assert.assertTrue(t2.isAncestor(t1));
		Assert.assertTrue(t2.isAncestor(t2));
	}
}
