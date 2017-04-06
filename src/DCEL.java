import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class DCEL
{
	private HashSet<Vertex> m_Vertices;
	private HashSet<HalfEdge> m_Edges;
	private HashSet<Face> m_Faces;
	
	public DCEL()
	{
		this.m_Vertices = new HashSet<Vertex>();
		this.m_Edges = new HashSet<HalfEdge>();
		this.m_Faces = new HashSet<Face>();
	}
	
	public HashSet<Vertex> GetVertices()
	{
		return this.m_Vertices;
	}
	
	public HashSet<HalfEdge> GetEdges()
	{
		return this.m_Edges;
	}
	
	public HashSet<Face> GetFaces()
	{
		return this.m_Faces;
	}
	
	public void AddVertex(Vertex v)
	{
		Point r = v.GetPoint();
		Iterator<HalfEdge> iter = this.m_Edges.iterator();
		
		while (iter.hasNext())
		{
			HalfEdge edge = iter.next();
			HalfEdge edgeNext = edge.GetNext();
			
			Vertex origin = edge.GetOrigin();
			
			Point p = origin.GetPoint();
			Point q = origin.GetPoint();
			
			if (r.IsBetween(p, q))
			{
				HalfEdge edgePrev = edge.GetPrev();
				HalfEdge twin = edge.GetTwin();
				HalfEdge twinNext = twin.GetNext();
				HalfEdge twinPrev = twin.GetPrev();
				
				Vertex twinOrigin = twin.GetOrigin();
				
				Face f1 = edge.GetFace();
				Face f2 = twin.GetFace();
				
				HalfEdge e1 = new HalfEdge(origin, f1, null, edge.GetPrev(), null);
				HalfEdge e2 = new HalfEdge(v, f1, edgeNext, e1, null);
				HalfEdge t1 = new HalfEdge(twinOrigin, f2, null, twinPrev, e2);
				HalfEdge t2 = new HalfEdge(v, f2, twinNext, t1, e1);
				
				e1.SetNext(e2);
				e1.SetTwin(t2);
				e2.SetTwin(t1);
				t1.SetNext(t2);
				
				edgeNext.SetPrev(e2);
				edgePrev.SetNext(e1);
				twinNext.SetPrev(t2);
				twinPrev.SetNext(t1);
				
				if (edge.equals(f1.GetOuter()))
				{
					f1.SetOuter(e1);
				}
				
				if (twin.equals(f2.GetOuter()))
				{
					f2.SetOuter(t1);
				}
				
				HashSet<HalfEdge> f1Holes = f1.GetInner();
				if (f1Holes.contains(edge))
				{
					f1Holes.add(e1);
					f1Holes.add(e2);
					f1Holes.remove(edge);
				}
				
				HashSet<HalfEdge> f2Holes = f2.GetInner();
				if (f2Holes.contains(twin))
				{
					f2Holes.add(t1);
					f2Holes.add(t2);
					f2Holes.remove(twin);
				}
				
				origin.SetEdge(e1);
				twinOrigin.SetEdge(t1);
				v.SetEdge(e2);
			}
		}
	}
	
	public void AddEdge(Vertex v1, Vertex v2)
	{
		// Fix this part and Triangulate works. It's getting the wrong face. Need to figure out how to get which face the edge between v1 and v2 would split.
		HalfEdge edge1 = v1.GetEdge();
		HalfEdge edge2 = v2.GetEdge();
		
		Face originalFace = edge1.GetFace();
		Face splitFace = new Face();
		
		// Go through each edge originating at v1
		HalfEdge rotE = edge1;
		boolean flag = false;
		do
		{
			originalFace = rotE.GetFace();
			// Only traverse the convex faces (i.e. do not traverse the face unbounded on the outside
			if (originalFace.GetOuter() == null)
			{
				rotE = rotE.GetTwin().GetNext();
				continue;
			}
			
			// Traverse the face to see if v2 is contained there
			HalfEdge iter = rotE.GetNext();
			while (!iter.equals(rotE))
			{
				if (iter.GetOrigin().equals(v2))
				{
					edge1 = rotE;
					edge2 = iter;
					flag = true;
					break;
				}
				iter = iter.GetNext();
			}
			rotE = rotE.GetTwin().GetNext();
		} while (!rotE.equals(edge1) && !flag);
		
		HalfEdge e1 = new HalfEdge(edge1.GetOrigin(), originalFace, edge2, edge1.GetPrev(), null);
		HalfEdge e2 = new HalfEdge(edge2.GetOrigin(), splitFace, edge1, edge2.GetPrev(), null);
		
		e1.SetTwin(e2);
		e2.SetTwin(e1);
		
		// Update all edges going into and out of v1 and v2
		// v1
		List<HalfEdge> v1UpdateBound = new ArrayList<HalfEdge>();
		List<HalfEdge> v1UpdateTwinBound = new ArrayList<HalfEdge>();
		HalfEdge iter = v1.GetEdge();
		do
		{
			if (iter.GetFace().equals(originalFace))
			{
				v1UpdateBound.add(iter);
			}
			else if(iter.GetTwin().GetFace().equals(originalFace))
			{
				v1UpdateTwinBound.add(iter);
			}
			iter = iter.GetTwin().GetNext();
		} while (!iter.equals(v1.GetEdge()));
		
		for (HalfEdge e : v1UpdateBound)
		{
			e.SetPrev(e2);
		}
		
		for (HalfEdge e : v1UpdateTwinBound)
		{
			e.GetTwin().SetNext(e1);
		}
		
		// v2
		List<HalfEdge> v2UpdateBound = new ArrayList<HalfEdge>();
		List<HalfEdge> v2UpdateTwinBound = new ArrayList<HalfEdge>();
		iter = v2.GetEdge();
		do
		{
			if (iter.GetFace().equals(originalFace))
			{
				v2UpdateBound.add(iter);
			}
			else if(iter.GetTwin().GetFace().equals(originalFace))
			{
				v2UpdateTwinBound.add(iter);
			}
			iter = iter.GetTwin().GetNext();
		} while (iter != v2.GetEdge());
		
		for (HalfEdge e : v2UpdateBound)
		{
			e.SetPrev(e1);
		}
		
		for (HalfEdge e : v2UpdateTwinBound)
		{
			e.GetTwin().SetNext(e2);
		}
		
		// Now update the edge list bounding originalFace and splitFace
		for (iter = e1.GetNext(); !iter.equals(e1); iter = iter.GetNext())
		{
			iter.SetFace(originalFace);
		}
		
		for (iter = e2.GetNext(); !iter.equals(e2); iter = iter.GetNext())
		{
			iter.SetFace(splitFace);
		}
		
		HashSet<HalfEdge> splitInner = new HashSet<HalfEdge>();
		HashSet<HalfEdge> originalInner = originalFace.GetInner();
		
		Iterator<HalfEdge> iter2 = originalInner.iterator();
		while (iter2.hasNext())
		{
			HalfEdge inner = iter2.next();
			Vertex origin = inner.GetOrigin();
			Point originPoint = origin.GetPoint();
			
			if (originPoint.IsLeft(e2))
			{
				splitInner.add(inner);
				originalInner.remove(inner);
			}
		}
		
		e1.GetOrigin().SetEdge(e1);
		e2.GetOrigin().SetEdge(e2);
		
		originalFace.SetOuter(e1);
		splitFace.SetOuter(e2);;
		
		this.m_Faces.add(splitFace);
		this.m_Edges.add(e1);
		this.m_Edges.add(e2);
	}
	
	public boolean IsSimplePolygon(Vertex[] vertices)
	{
		EventQueue eq = new EventQueue(vertices);
		SweepLine sl = new SweepLine();
		Iterator<Event> iter = eq.m_Events.iterator();
		SweepLine.Segment s;
		
		while(iter.hasNext())
		{
			Event e = iter.next();
			if (e.GetType() == Event.EventType.LEFT)
			{
				s = sl.AddEvent(e);
				
				if (sl.Intersect(s, s.GetAbove()))
				{
					return false;
				}
				
				if (sl.Intersect(s, s.GetBelow()))
				{
					return false;
				}
			}
			else
			{
				s = sl.FindEvent(e);
				
				if (sl.Intersect(s.GetAbove(), s.GetBelow()))
				{
					return false;
				}
				
				sl.RemoveSegment(s);
			}
		}
		
		return true;
	}
	
	public boolean IsSimplePolygon(HashSet<Vertex> vertices)
	{
		Vertex[] verts = new Vertex[vertices.size()];
		Iterator<Vertex> iter = vertices.iterator();
		
		int i = 0;
		while (iter.hasNext())
		{
			verts[i++] = iter.next();
		}
		
		return this.IsSimplePolygon(verts);
	}
	
	public void ConstructSimplePolygon(Point[] points) throws Exception
	{
		if (points.length < 2)
		{
			throw new Exception("ConstructSimplePolygon: points does not represent a simple polygon!");
		}
		
		HalfEdge lastEdge = new HalfEdge();
		HalfEdge lastEdgeTwin = new HalfEdge();
		
		HalfEdge prev = lastEdge;
		HalfEdge prevTwin = lastEdgeTwin;
		
		prev.SetTwin(prevTwin);
		prevTwin.SetTwin(prev);
		
		Vertex[] vertices = new Vertex[points.length];
		
		for (int i = 0; i < points.length - 1; ++i)
		{
			HalfEdge e = new HalfEdge();
			HalfEdge t = new HalfEdge();
			vertices[i] = new Vertex(points[i], null);
			this.m_Vertices.add(vertices[i]);
			
			e.SetPrev(prev);
			prev.SetNext(e);
			
			prevTwin.SetOrigin(vertices[i]);
			e.SetOrigin(vertices[i]);
			
			e.SetTwin(t);
			t.SetTwin(e);
			
			t.SetNext(prevTwin);
			prevTwin.SetPrev(t);
			
			prev = e;
			prevTwin = t;
		}
		
		vertices[points.length - 1] = new Vertex(points[points.length - 1], null);
		this.m_Vertices.add(vertices[points.length - 1]);
		
		lastEdge.SetPrev(prev);
		prev.SetNext(lastEdge);
		
		prevTwin.SetOrigin(vertices[points.length - 1]);
		lastEdge.SetOrigin(vertices[points.length - 1]);
		
		lastEdgeTwin.SetNext(prevTwin);
		prevTwin.SetPrev(lastEdgeTwin);
		
		Face innerFace = new Face();
		Face outerFace = new Face();
		
		innerFace.SetOuter(prev);
		HashSet<HalfEdge> inner = outerFace.GetInner();
		inner.add(lastEdgeTwin);
		outerFace.SetInner(inner);
		
		HalfEdge edge = prev;
		innerFace.SetConvex(true);
		do
		{
			edge.SetFace(innerFace);
			edge.GetTwin().SetFace(outerFace);
			
			this.m_Edges.add(edge);
			this.m_Edges.add(edge.GetTwin());
			
			edge.GetOrigin().SetEdge(edge);
			edge.GetOrigin().SetConvex((edge.GetNext().GetOrigin().GetPoint().IsLeft(edge.GetPrev())));
			innerFace.SetConvex(innerFace.IsConvex() && edge.GetOrigin().IsConvex());
			
			edge = edge.GetNext();
		} while (!edge.equals(prev));
		
		this.m_Faces.add(innerFace);
		this.m_Faces.add(outerFace);
	
		if (!IsSimplePolygon(vertices))
		{
			throw new Exception("ConstructSimplePolygon: points does not represent a simple polygon!");
		}
	}
}
