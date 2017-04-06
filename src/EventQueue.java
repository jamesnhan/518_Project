import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventQueue {
	List<Event> m_Events;
	
	public EventQueue(Vertex[] vertices)
	{
		this.m_Events = new ArrayList<Event>();

		for (int i = 0; i < vertices.length; ++i)
		{
			HalfEdge edge = vertices[i].GetEdge();
			HalfEdge prev = edge.GetPrev();
			
			Event e1 = new Event(edge, edge.GetOrigin());
			Event e2 = new Event(prev, edge.GetOrigin());
			
			float e1Side = e1.GetVertex().compareTo(edge.GetNext().GetOrigin());
			float e2Side = e2.GetVertex().compareTo(prev.GetOrigin());
			
			if (e1Side > 0.0f)
			{
				e1.SetType(Event.EventType.RIGHT);
			}
			else
			{
				e1.SetType(Event.EventType.LEFT);
			}
			
			if (e2Side > 0.0f)
			{
				e2.SetType(Event.EventType.RIGHT);
			}
			else
			{
				e2.SetType(Event.EventType.LEFT);
			}
			
			this.m_Events.add(e1);
			this.m_Events.add(e2);
		}
		
		Collections.sort(this.m_Events);
	}
}
