public class HalfEdge
{
	private Vertex m_Origin;
	private Face m_IncidentFace;
	private HalfEdge m_Next;
	private HalfEdge m_Prev;
	private HalfEdge m_Twin;
	private Vertex m_Helper;
	
	public HalfEdge() { }
	
	public HalfEdge(Vertex origin, Face incidentFace, HalfEdge next, HalfEdge prev, HalfEdge twin)
	{
		this.m_Origin = origin;
		this.m_IncidentFace = incidentFace;
		this.m_Next = next;
		this.m_Prev = prev;
		this.m_Twin = twin;
		this.m_Helper = null;
	}
	
	public HalfEdge(HalfEdge other)
	{
		this.m_Origin = other.m_Origin;
		this.m_IncidentFace = other.m_IncidentFace;
		this.m_Next = other.m_Next;
		this.m_Prev = other.m_Prev;
		this.m_Twin = other.m_Twin;
		this.m_Helper = other.m_Helper;
	}
	
	public Vertex GetOrigin()
	{
		return this.m_Origin;
	}
	
	public void SetOrigin(Vertex v)
	{
		this.m_Origin = v;
	}
	
	public Face GetFace()
	{
		return this.m_IncidentFace;
	}
	
	public void SetFace(Face f)
	{
		this.m_IncidentFace = f;
	}
	
	public HalfEdge GetNext()
	{
		return this.m_Next;
	}
	
	public void SetNext(HalfEdge e)
	{
		this.m_Next = e;
	}
	
	public HalfEdge GetPrev()
	{
		return this.m_Prev;
	}
	
	public void SetPrev(HalfEdge e)
	{
		this.m_Prev = e;
	}
	
	public HalfEdge GetTwin()
	{
		return this.m_Twin;
	}
	
	public void SetTwin(HalfEdge e)
	{
		this.m_Twin = e;
	}
	
	public Vertex GetHelper()
	{
		return this.m_Helper;
	}
	
	public void SetHelper(Vertex v)
	{
		this.m_Helper = v;
	}
	
	public int GetDirection()
	{
		float f = 0.0f;
		
		HalfEdge e = this;
		do
		{
			HalfEdge n = e.m_Next;
			Point ex = e.m_Origin.GetPoint();
			Point nx = n.m_Origin.GetPoint();
			f += (nx.GetX() - ex.GetX()) * (nx.GetY() + ex.GetY());
			e = n;
		} while(!e.equals(this));
		
		return (int)Math.signum(f);
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append("[")
			.append(this.m_Origin.toString())
			.append(", ")
			.append(this.m_Next.m_Origin.toString())
			.append("]");
		
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		HalfEdge other = (HalfEdge)obj;
		return ((this.m_Origin.equals(other.m_Origin)) && (this.m_Next.m_Origin.equals(other.m_Next.m_Origin)));
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int hash = 17;
		
		hash = prime * hash + this.m_Origin.hashCode();
		hash = prime * hash + this.m_Next.m_Origin.hashCode();
		
		return hash;
	}
}
