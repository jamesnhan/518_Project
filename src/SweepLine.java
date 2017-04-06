import java.util.TreeSet;

public class SweepLine {
	public class Segment implements Comparable<Segment>
	{
		private HalfEdge m_Edge;
		private Vertex m_LeftVertex;
		private Vertex m_RightVertex;
		private Segment m_Above;
		private Segment m_Below;
		
		public HalfEdge GetEdge()
		{
			return this.m_Edge;
		}
		
		public void SetEdge(HalfEdge e)
		{
			this.m_Edge = e;
		}
		
		public Vertex GetLeftVertex()
		{
			return this.m_LeftVertex;
		}
		
		public void SetLeftVertex(Vertex v)
		{
			this.m_LeftVertex = v;
		}
		
		public Vertex GetRightVertex()
		{
			return this.m_RightVertex;
		}
		
		public void SetRightVertex(Vertex v)
		{
			this.m_RightVertex = v;
		}
		
		public Segment GetAbove()
		{
			return this.m_Above;
		}
		
		public void SetAbove(Segment s)
		{
			this.m_Above = s;
		}
		
		public Segment GetBelow()
		{
			return this.m_Below;
		}
		
		public void SetBelow(Segment s)
		{
			this.m_Below = s;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			Segment other = (Segment)obj;
			return this.m_Edge == other.m_Edge;
		}
		
		@Override
		public int hashCode()
		{
			return this.m_Edge.hashCode();
		}
		
		@Override
		public int compareTo(Segment other)
		{
			Point p = this.m_LeftVertex.GetPoint();
			Point q = this.m_RightVertex.GetPoint();
			Point r = other.m_LeftVertex.GetPoint();
			Point s = other.m_RightVertex.GetPoint();
			
			if (this.equals(other))
			{
				return 0;
			}
			
			if (p.GetX() <= r.GetX())
			{
				float left = Point.Cross(p, r, q);
				
				if (left != 0.0f)
				{
					return (int)Math.signum(left);
				}
				else
				{
					if (p.GetX() == s.GetX())
					{
						if (p.GetY() > s.GetY())
						{
							return 1;
						}
						else
						{
							return -1;
						}
					}
					else
					{
						return (int)Math.signum(Point.Cross(p, s, q));
					}
				}
			}
			else
			{
				float left = Point.Cross(r, p, s);
				
				if (left != 0.0f)
				{
					return -(int)Math.signum(left);
				}
				else
				{
					return -(int)Math.signum(Point.Cross(r, q, s));
				}
			}
		}
	}
	
	private TreeSet<Segment> m_Segments;
	
	public SweepLine()
	{
		this.m_Segments = new TreeSet<Segment>();
	}
	
	public Segment AddEvent(Event e)
	{
		Segment s = new Segment();
		s.m_Edge = e.GetEdge();
		Vertex v0 = s.m_Edge.GetOrigin();
		Vertex v1 = s.m_Edge.GetTwin().GetOrigin();
		
		if (v0.compareTo(v1) > 0)
		{
			s.m_RightVertex = v0;
			s.m_LeftVertex = v1;
		}
		else
		{
			s.m_LeftVertex = v0;
			s.m_RightVertex = v1;
		}
		
		s.m_Above = null;
		s.m_Below = null;

		Segment next = this.m_Segments.ceiling(s);
		Segment prev = this.m_Segments.floor(s);
		this.m_Segments.add(s);
		
		if (next != null)
		{
			s.m_Above = next;
			next.m_Below = s;
		}
		
		if (prev != null)
		{
			s.m_Below = prev;
			prev.m_Above = s;
		}
		
		return s;
	}
	
	public Segment FindEvent(Event e)
	{
		Segment s = new Segment();
		
		s.m_Edge = e.GetEdge();
		float left = s.m_Edge.GetOrigin().compareTo(s.m_Edge.GetTwin().GetOrigin());
		if (left <= 0.0f)
		{
			s.m_LeftVertex = s.m_Edge.GetOrigin();
			s.m_RightVertex = s.m_Edge.GetTwin().GetOrigin();
		}
		else
		{
			s.m_RightVertex = s.m_Edge.GetOrigin();
			s.m_LeftVertex = s.m_Edge.GetTwin().GetOrigin();
		}
		s.m_Above = null;
		s.m_Below = null;
		
		if (this.m_Segments.contains(s))
		{
			return s;
		}
		
		return null;
	}
	
	public void RemoveSegment(Segment s)
	{
		if (this.m_Segments.contains(s))
		{
			if (s.m_Above != null)
			{
				s.m_Above.m_Below = s.m_Below;
			}
			
			if (s.m_Below != null)
			{
				s.m_Below.m_Above = s.m_Above;
			}
			
			this.m_Segments.remove(s);
		}
	}
	
	public boolean Intersect(Segment s1, Segment s2)
	{
		if (s1 == null || s2 == null)
		{
			return false;
		}
		
		HalfEdge e1 = s1.m_Edge;
		HalfEdge e2 = s2.m_Edge;
		
		if (e1.GetNext().equals(e2) || e2.GetNext().equals(e1))
		{
			return false;
		}
		
		Point p = s1.m_LeftVertex.GetPoint();
		Point q = s1.m_RightVertex.GetPoint();
		Point r = s2.m_LeftVertex.GetPoint();
		Point s = s2.m_RightVertex.GetPoint();
		
		float left = Point.Cross(p, r, q);
		float right = Point.Cross(p, s, q);
		
		if (left * right > 0)
		{
			return false;
		}
		
		left = Point.Cross(r, p, s);
		right = Point.Cross(r, q, s);
		
		if (left * right > 0)
		{
			return false;
		}
		
		return true;
	}
}
