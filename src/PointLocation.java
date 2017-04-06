import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

public class PointLocation {
	private PersistentRedBlackTreeSet<Slab> m_Tree;
	
	private class SlabComparator implements Comparator<Slab>
	{
		@Override
		public int compare(Slab s1, Slab s2) {
			if (s1.GetLeftBound().GetX() < s2.GetLeftBound().GetX())
			{
				return -1;
			}
			else if (s1.GetLeftBound().GetX() > s2.GetLeftBound().GetX())
			{
				return 1;
			}
			
			return 0;
		}
	}
	
	public PointLocation(DCEL polygon)
	{
		this.m_Tree = new PersistentRedBlackTreeSet<Slab>(new SlabComparator());
		
		List<Vertex> vertsSortedHorizontal = new ArrayList<Vertex>();
		HashSet<Vertex> verts = polygon.GetVertices();
		Iterator<Vertex> iter = verts.iterator();
		while (iter.hasNext())
		{
			vertsSortedHorizontal.add(iter.next());
		}
		Collections.sort(vertsSortedHorizontal);
		iter = vertsSortedHorizontal.iterator();
		Vertex v1;
		Vertex v2;
		Point p1;
		Point p2;
		
		List<HalfEdge> openEdges = new ArrayList<HalfEdge>();
		
		if (iter.hasNext())
		{
			
			v1 = iter.next();
			
			while (iter.hasNext())
			{
				v2 = iter.next();
				
				// p1 is left side
				// p2 is right side
				p1 = v1.GetPoint();
				p2 = v2.GetPoint();
				
				Slab s = new Slab(p1, p2);
				
				// Also check the ones that started earlier and go into/through this slab
				Iterator<HalfEdge> iter2 = openEdges.iterator();
				while (iter2.hasNext())
				{
					HalfEdge edge = iter2.next();
					if (edge.GetTwin().GetOrigin().GetPoint().GetX() <= p2.GetX())
					{
						iter2.remove();
					}
					
					if (edge.GetTwin().GetOrigin().GetPoint().GetX() > p1.GetX())
					{
						s.AddEdge(edge);
					}
				}
				
				// Find all HalfEdges in the slab
				HalfEdge e = v1.GetEdge();
				do
				{
					Point p3 = e.GetNext().GetOrigin().GetPoint();
					if (p1.GetX() <= p3.GetX())
					{
						s.AddEdge(e);
						openEdges.add(e);
					}
					e = e.GetTwin().GetNext();
				} while (!e.equals(v1.GetEdge()));
				
				v1 = v2;
				
				this.m_Tree = this.m_Tree.insert(s);
			}
		}
	}
	
	public HalfEdge Query(Point p)
	{
		PersistentRedBlackTreeSet<Slab>.RedBlackNode<Slab> n = this.m_Tree.GetRootNode();
		
		while (n.GetLeft() != null && n.GetRight() != null)
		{
			Slab s = n.GetElement();
			if (s.GetLeftBound().GetX() <= p.GetX() && s.GetRightBound().GetX() >= p.GetX())
			{
				return s.GetEdgeBelow(p);
			}
			else if (s.GetRightBound().GetX() < p.GetX())
			{
				n = n.GetRight();
			}
			else if (s.GetLeftBound().GetX() > p.GetX())
			{
				n = n.GetLeft();
			}
		}
		
		return null;
	}
	
	public List<Slab> GetSlabs()
	{
		return this.m_Tree.getElements();
	}
}
