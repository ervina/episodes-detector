package cc.episodeMining.mubench.model;

import java.util.List;

import cc.kave.episodes.io.EventStreamIo;
import cc.kave.episodes.model.events.Event;
import cc.recommenders.io.Logger;

import com.google.inject.Inject;

public class EventGenerator {

	private static EventStreamIo eventStream;
	
	@Inject
	public EventGenerator(EventStreamIo streamIo) {
		this.eventStream = streamIo;
	}
	
	public static void main(String[] args) {
		List<Event> events = eventStream.readMapping(300);
		
		for (Event e : events) {
			Logger.log("%s", e.toString());
			Logger.log("%s", e.getType().toString());
			Logger.log("%s", e.getMethod().toString());
			break;
		}
	}
}
