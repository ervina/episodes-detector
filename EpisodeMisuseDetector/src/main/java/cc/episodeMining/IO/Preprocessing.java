package cc.episodeMining.IO;

import cc.episodeMining.data.StreamGenerator;

import com.google.inject.Inject;

public class Preprocessing {

	private StreamGenerator generator;
	
	@Inject
	public Preprocessing(StreamGenerator streamGenerator) {
		this.generator = streamGenerator;
	}
}
