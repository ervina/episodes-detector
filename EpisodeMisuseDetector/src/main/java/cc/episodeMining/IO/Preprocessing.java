package cc.episodeMining.IO;

import cc.episodeMining.data.SequenceGenerator;

import com.google.inject.Inject;

public class Preprocessing {

	private SequenceGenerator generator;
	
	@Inject
	public Preprocessing(SequenceGenerator streamGenerator) {
		this.generator = streamGenerator;
	}
}
