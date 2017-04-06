public class Event implements Comparable<Event>
{
	public enum EventType
	{
		LEFT, RIGHT;
	}
	
	private HalfEdge m_Edge;
	private Vertex m_Vertex;
	private EventType m_Type;
	
	public Event(HalfEdge e, Vertex v)
	{
		this.m_Edge = e;
		this.m_Vertex = v;
	}
	
	public HalfEdge GetEdge()
	{
		return this.m_Edge;
	}
	
	public void SetEdge(HalfEdge e)
	{
		this.m_Edge = e;
	}
	
	public Vertex GetVertex()
	{
		return this.m_Vertex;
	}
	
	public void SetVertex(Vertex v)
	{
		this.m_Vertex = v;
	}
	
	public EventType GetType()
	{
		return this.m_Type;
	}
	
	public void SetType(EventType t)
	{
		this.m_Type = t;
	}
	
	@Override
	public int compareTo(Event other)
	{
		return this.m_Vertex.compareTo(other.m_Vertex);
	}
}
