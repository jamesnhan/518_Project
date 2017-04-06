public class Vertex implements Comparable<Vertex>
{
	public enum Type
	{
		START, END, MERGE, SPLIT, REGULARL, REGULARR, UNKNOWN;
	}
	
    private Point m_Point;
    private HalfEdge m_IncidentEdge;
    private Type m_Type;
    private boolean m_Convex;

    public Vertex()
    {
        this.m_Point = null;
        this.m_IncidentEdge = null;
        this.m_Type = Type.UNKNOWN;
        this.m_Convex = false;
    }

    public Vertex(Point p, HalfEdge e)
    {
        this.m_Point = p;
        this.m_IncidentEdge = e;
        this.m_Type = Type.UNKNOWN;
        this.m_Convex = false;
    }

    public Vertex(Point p)
    {
        this.m_Point = p;
        this.m_IncidentEdge = null;
        this.m_Type = Type.UNKNOWN;
        this.m_Convex = false;
    }

    public void SetPoint(Point p)
    {
        this.m_Point = p;
    }

    public Point GetPoint()
    {
        return this.m_Point;
    }

    public HalfEdge GetEdge()
    {
        return this.m_IncidentEdge;
    }

    public void SetEdge(HalfEdge e)
    {
        this.m_IncidentEdge = e;
    }
    
    public Type GetType()
    {
    	return this.m_Type;
    }
    
    public void SetType(Type t)
    {
    	this.m_Type = t;
    }
    
    public boolean IsConvex()
    {
    	return this.m_Convex;
    }
    
    public void SetConvex(boolean convex)
    {
    	this.m_Convex = convex;
    }

    @Override
    public String toString()
    {
        return this.m_Point.toString();
    }

    @Override
    public boolean equals(Object obj)
    {
    	Vertex other = (Vertex)obj;
        return (this.m_Point.equals(other.m_Point));
    }
    
    @Override
    public int compareTo(Vertex other)
    {
    	return this.m_Point.compareTo(other.m_Point);
    }
    
    @Override
    public int hashCode()
    {
    	return this.m_Point.hashCode();
    }
}

