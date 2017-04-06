import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class Slab {
	private class VerticalEdgeComparator implements Comparator<HalfEdge>
	{
		@Override
		public int compare(HalfEdge e1, HalfEdge e2) {
			float y1 = e1.GetTwin().GetOrigin().GetPoint().GetY();
			float y2 = e2.GetTwin().GetOrigin().GetPoint().GetY();
			
			if (y1 < y2)
			{
				return -1;
			}
			else if (y1 > y2)
			{
				return 1;
			}
			else
			{
				y1 = e1.GetOrigin().GetPoint().GetY();
				y2 = e2.GetOrigin().GetPoint().GetY();
				
				if (y1 < y2)
				{
					return -1;
				}
				else if (y1 > y2)
				{
					return 1;
				}
			}
			
			return 0;
		}
	}
	
	private List<HalfEdge> m_Edges;
	private Point m_LeftBound;
	private Point m_RightBound;
	
	public Slab(Point leftBound, Point rightBound)
	{
		this.m_Edges = new ArrayList<HalfEdge>();
		this.m_LeftBound = leftBound;
		this.m_RightBound = rightBound;
	}
	
	public void AddEdge(HalfEdge e)
	{
		this.m_Edges.add(e);
		this.m_Edges.sort(new VerticalEdgeComparator());
	}
	
	public boolean IsInSlab(Point p)
	{
		return ((this.m_LeftBound.GetX() <= p.GetX()) && (this.m_RightBound.GetX() >= p.GetX()));
	}
	
	public HalfEdge GetEdgeBelow(Point p)
	{
		Iterator<HalfEdge> iter = this.m_Edges.iterator();
		HalfEdge below = null;
		
		while (iter.hasNext())
		{
			HalfEdge e = iter.next();
			
			if (p.IsLeft(e))
			{
				below = e;
			}
			else
			{
				break;
			}
		}
		
		return below;
	}
	
	public Point GetLeftBound()
	{
		return this.m_LeftBound;
	}
	
	public Point GetRightBound()
	{
		return this.m_RightBound;
	}
}
