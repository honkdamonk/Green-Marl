package common;

import java.util.HashSet;

public class gm_vocabulary {
	
	private HashSet<String> words = new HashSet<String>();

	public final void clear() {
		words.clear();
	}

	public final void add_word(String word) {
		assert word != null;
		words.add(word);
	}

	public final boolean has_word(String word) {
		return words.contains(word);
	}

}
