public class Point implements Comparable<Point>
{
	private static int s_Hash = 0;
	private int m_Hash;
    private float m_X;
    private float m_Y;

    public Point(float x, float y)
    {
    	this.m_Hash = Point.s_Hash++;
        this.m_X = x;
        this.m_Y = y;
    }

    public float GetX()
    {
        return this.m_X;
    }
    
    public void SetX(float x)
    {
    	this.m_X = x;
    }

    public float GetY()
    {
        return this.m_Y;
    }
    
    public void SetY(float y)
    {
    	this.m_Y = y;
    }
    
    private static float Max(float a, float b)
    {
    	return (a > b) ? a : b;
    }
    
    private static float Min(float a, float b)
    {
    	return (a < b) ? a : b;
    }
    
    public static float Dot(Point p, Point q)
    {
    	return ((p.m_X * q.m_X) + (p.m_Y * q.m_Y));
    }
    
    public static float Dot(Point p, Point q, Point r)
    {
    	return (((p.m_X - q.m_X) * (r.m_X - q.m_X)) + ((p.m_Y - q.m_Y) * (r.m_Y - q.m_Y)));
    }
    
    public static float Cross(Point p, Point q, Point r)
    {
    	return ((r.m_X - p.m_X) * (q.m_Y - p.m_Y) - (r.m_Y - p.m_Y) * (q.m_X - p.m_X));
    }
    
    public boolean IsBetween(Point p, Point q)
    {
    	if (p.m_X == q.m_X)
    	{
    		float upperY = Point.Max(p.m_Y, q.m_Y);
    		float lowerY = Point.Min(p.m_Y, q.m_Y);
    		
    		return ((this.m_X == p.m_X) && (this.m_Y <= upperY) && (this.m_Y >= lowerY));
    	}
    	else if (p.m_Y == q.m_Y)
    	{
    		float upperX = Point.Max(p.m_X, q.m_X);
    		float lowerX = Point.Min(p.m_X, q.m_X);
    		
    		return ((this.m_Y == p.m_Y) && (this.m_X <= upperX) && (this.m_X >= lowerX));
    	}
    	
    	return Point.Cross(p, this, q) == 0.0f;
    }
    
    public boolean IsLeft(HalfEdge edge)
    {
    	Point p = edge.GetOrigin().GetPoint();
    	Point q = edge.GetNext().GetOrigin().GetPoint();
    	
    	return Point.Cross(p, this, q) > 0.0f;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("(")
            .append(this.m_X)
            .append(", ")
            .append(this.m_Y)
            .append(")");

        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj)
    {
    	Point other = (Point)obj;
    	return  ((this.m_X == other.m_X) && (this.m_Y == other.m_Y));
    }

    @Override
    public int compareTo(Point other)
    {
        if (this.m_X < other.m_X)
        {
            return -1;
        }
        else if (this.m_X > other.m_X)
        {
            return 1;
        }
        
        if (this.m_Y < other.m_Y)
        {
            return -1;
        }
        else if (this.m_Y > other.m_Y)
        {
            return 1;
        }
        
        return 0;
    }
    
    @Override
    public int hashCode()
    {
    	return this.m_Hash;
    }
}
