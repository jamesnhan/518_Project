import java.util.HashSet;
import java.util.Iterator;

public class Face
{
	private HalfEdge m_OuterComponent;
	private HashSet<HalfEdge> m_InnerComponent;
	private boolean m_Convex;
	
	public Face()
	{
		this.m_OuterComponent = null;
		this.m_InnerComponent = new HashSet<HalfEdge>();
		this.m_Convex = false;
	}
	
	public Face(HalfEdge outer, HalfEdge inner)
	{
		this.m_OuterComponent = outer;
		this.m_InnerComponent = new HashSet<HalfEdge>();
		this.m_InnerComponent.add(inner);
		this.m_Convex = false;
	}
	
	public Face(HalfEdge outer, HashSet<HalfEdge> inner)
	{
		this.m_OuterComponent = outer;
		this.m_InnerComponent = new HashSet<HalfEdge>(inner);
		this.m_Convex = false;
	}
	
	public HalfEdge GetOuter()
	{
		return this.m_OuterComponent;
	}
	
	public void SetOuter(HalfEdge outer)
	{
		this.m_OuterComponent = outer;
	}
	
	public HashSet<HalfEdge> GetInner()
	{
		return this.m_InnerComponent;
	}
	
	public void SetInner(HashSet<HalfEdge> inner)
	{
		this.m_InnerComponent = inner;
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
		StringBuilder sb = new StringBuilder();
		
		sb.append("[");
		if (this.m_OuterComponent != null)
		{
			sb.append("Outer: <");
			HalfEdge iter = this.m_OuterComponent;
			do
			{
				sb.append(iter.toString())
					.append(", ");
				iter = iter.GetNext();
			} while(!iter.equals(this.m_OuterComponent));
			sb.delete(sb.lastIndexOf(","), sb.length())
				.append(">");
		}
		
		if (this.m_OuterComponent != null && this.m_InnerComponent.size() > 0)
		{
			sb.append(", ");
		}
		
		if (this.m_InnerComponent.size() > 0)
		{
			sb.append("Inner: <");
			Iterator<HalfEdge> innerIter = this.m_InnerComponent.iterator();
			while (innerIter.hasNext())
			{
				HalfEdge inner = innerIter.next();
				HalfEdge iter = inner;
				do
				{
					sb.append(iter.toString())
						.append(", ");
					iter = iter.GetNext();
				} while(!iter.equals(inner));
				sb.delete(sb.lastIndexOf(","), sb.length())
					.append(">, <");
			}
			sb.delete(sb.lastIndexOf(","), sb.length());
		}
		sb.append("]");
		
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		Face other = (Face)obj;
		
		if (this.m_OuterComponent != null && other.m_OuterComponent != null)
		{
			HalfEdge iter = this.m_OuterComponent;
			do
			{
				if (iter.equals(other.m_OuterComponent))
				{
					return true;
				}
				
				iter = iter.GetNext();
			} while (!iter.equals(this.m_OuterComponent));
		}
		
		return false;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int hash = 17;
		
		if (this.m_OuterComponent != null)
		{
			HalfEdge iter = this.m_OuterComponent;
			do
			{
				hash = prime * hash + iter.hashCode();
				iter = iter.GetNext();
			} while (!iter.equals(this.m_OuterComponent));
		}
		
		return hash;
	}
}