package cc.episodeMining.data;

import java.util.List;

import com.google.common.collect.Lists;

//class TestClass extends StrBuilder {
//
//	String pattern(Object obj) {
//
//		String str = (obj == null ? this.getNullText() : obj.toString());
//		if (str == null) {
//			str = "";
//		}
//		return str;
//	}
//}
//
//abstract class StrBuilder {
//	String pattern(Object obj) {
//		return null;
//	}
//
//	String getNullText() {
//		return "";
//	}
//}

//class TestClass implements StrBuilder {
//
//	public String pattern(Object obj) {
//
//		String str = (obj == null ? this.getNullText() : obj.toString());
//		if (str == null) {
//			List<Integer> someList = Lists.newLinkedList();
//			str = "";
//		}
//		return str;
//	}
//
//	public String getNullText() {
//		return "";
//	}
//}
//
//interface StrBuilder {
//	String pattern(Object obj);
//
//	String getNullText();
//}

class TestClass {
	static {
		Object o = new Object();
		o.hashCode();
	}

	void n(Object o) {
		o.hashCode();
	}
}
