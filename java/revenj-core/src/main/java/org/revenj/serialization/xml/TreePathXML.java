package org.revenj.serialization.xml;

import org.revenj.TreePath;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@XmlRootElement(name = "TreePath")
public class TreePathXML {
	@XmlValue
	public String value;

	static String create(TreePath value) {
		if (value == null) return null;
		return value.toString();
	}

	static TreePath create(String value) {
		if (value == null) return null;
		return TreePath.create(value);
	}

	public static final Function<TreePath, TreePathXML> convert = s -> {
		TreePathXML xml = new TreePathXML();
		xml.value = s.toString();
		return xml;
	};

	public static class Adapter extends XmlAdapter<String, TreePath> {

		@Override
		public TreePath unmarshal(String v) throws Exception {
			return create(v);
		}

		@Override
		public String marshal(TreePath v) throws Exception {
			return create(v);
		}
	}

	@XmlRootElement(name = "ArrayOfTreePath")
	static class ArrayXML {
		@XmlElement(name = "TreePath")
		public String[] value;

		public static final Function<TreePath[], ArrayXML> convert = s -> {
			ArrayXML xml = new ArrayXML();
			xml.value = new String[s.length];
			for (int i = 0; i < s.length; i++) {
				xml.value[i] = create(s[i]);
			}
			return xml;
		};

		public static final Function<ArrayXML, TreePath[]> parse = s -> {
			TreePath[] result = new TreePath[s.value.length];
			for (int i = 0; i < s.value.length; i++) {
				result[i] = create(s.value[i]);
			}
			return result;
		};
	}

	@XmlRootElement(name = "ArrayOfTreePath")
	static class ListXML {
		@XmlElement(name = "TreePath")
		public List<String> value;

		public static final Function<List<TreePath>, ListXML> convert = s -> {
			ListXML xml = new ListXML();
			xml.value = new ArrayList<>(s.size());
			for (TreePath it : s) {
				xml.value.add(create(it));
			}
			return xml;
		};

		public static final Function<ListXML, List<TreePath>> parse = s -> {
			List<TreePath> result = new ArrayList<>(s.value.size());
			for (int i = 0; i < s.value.size(); i++) {
				result.add(create(s.value.get(i)));
			}
			return result;
		};
	}
}