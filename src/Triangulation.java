import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

public class Triangulation {
	private static class HorizontalComparator implements Comparator<HalfEdge>
	{
		@Override
		public int compare(HalfEdge e1, HalfEdge e2)
		{
			return (int)Math.signum((e1.GetOrigin().GetPoint().GetX() - e2.GetOrigin().GetPoint().GetX()));
		}
	}
	
	private static class VerticalComparator implements Comparator<HalfEdge>
	{
		@Override
		public int compare(HalfEdge e1, HalfEdge e2)
		{
			return (int)Math.signum((e2.GetOrigin().GetPoint().GetY() - e1.GetOrigin().GetPoint().GetY()));
		}
	}
	
	private static class VerticalComparatorVertex implements Comparator<Vertex>
	{
		@Override
		public int compare(Vertex v1, Vertex v2)
		{
			return (int)Math.signum(v2.GetPoint().GetY() - v1.GetPoint().GetY());
		}
	}
	
	public static void TriangulateMonotonePolygon(DCEL polygon) throws Exception
	{
		if (!IsMonotonePolygon(polygon))
		{
			throw new Exception("TriangulateMonotonePolygon: Attempted to triangulate a non-monotone polygon!");
		}
		Iterator<Face> faces = polygon.GetFaces().iterator();
		List<Vertex> diagonals = new ArrayList<Vertex>();
		while (faces.hasNext())
		{
			Face f = faces.next();
			HalfEdge bound = f.GetOuter();
			if (bound != null)
			{
				TreeSet<Vertex> V = new TreeSet<Vertex>(new VerticalComparatorVertex());
				Stack<Vertex> S = new Stack<Vertex>();
				
				HalfEdge e = bound;
				do
				{
					V.add(e.GetOrigin());
					e = e.GetNext();
				} while (!e.equals(bound));
				
				if (V.size() == 3)
				{
					continue;
				}
				
				S.push(V.pollFirst());
				S.push(V.pollFirst());
				
				while (V.size() > 1)
				{
					Vertex uj = V.pollFirst();
					
					HalfEdge uje = uj.GetEdge();
					while (!uje.GetFace().equals(f))
					{
						uje = uje.GetTwin().GetNext();
					}
					
					Vertex x = S.peek();
					
					HalfEdge xe = x.GetEdge();
					while (!xe.GetFace().equals(f))
					{
						xe = xe.GetTwin().GetNext();
					}
					
					HalfEdge ujen = uje.GetNext();
					HalfEdge xen = xe.GetNext();
					
					float ujx = uj.GetPoint().GetX();
					float ujy = uj.GetPoint().GetY();
					float ujnx = ujen.GetOrigin().GetPoint().GetX();
					float ujny = ujen.GetOrigin().GetPoint().GetY();
					
					float xx = xe.GetOrigin().GetPoint().GetX();
					float xy = xe.GetOrigin().GetPoint().GetY();
					float xnx = xen.GetOrigin().GetPoint().GetX();
					float xny = xen.GetOrigin().GetPoint().GetY();
					
					float ujs = Math.signum(ujy - ujny);
					float xs = Math.signum(xy - xny);
					
					if (ujs != xs)
					{
						Vertex old = S.peek();
						while (S.size() > 1)
						{
							Vertex v = S.pop();
							diagonals.add(uj);
							diagonals.add(v);
						}
						S.clear();
						S.push(old);
						S.push(uj);
					}
					else
					{
						x = S.pop();
						Vertex vk = null;
						
						while (!S.isEmpty())
						{
							vk = S.peek();
							
							float vkx = vk.GetPoint().GetX();
							float vky = vk.GetPoint().GetY();
							xx = xe.GetOrigin().GetPoint().GetX();
							xy = xe.GetOrigin().GetPoint().GetY();
							
							float cross = ((vkx - xx) * (ujy - xy)) - ((vky - xy) * (ujx - xx));
							cross = Point.Cross(vk.GetPoint(), x.GetPoint(), uj.GetPoint());
							float dot = ((vkx - xx) * (ujx - xx)) + ((vky - xy) * (ujy - xy));
							dot = Point.Dot(vk.GetPoint(), x.GetPoint(), uj.GetPoint());
							double angle = Math.atan2(cross, dot);
							
							if (angle < 0.0)
							{
								angle += 2.0 * Math.PI;
							}
							
							if (angle < Math.PI)
							{
								diagonals.add(uj);
								diagonals.add(vk);
								x = vk;
								S.pop();
							}
							else
							{
								break;
							}
						}
						
						S.push(x);
						S.push(uj);
					}
				}
				
				Vertex un = V.pollFirst();
				S.pop();
				while (S.size() > 1)
				{
					Vertex v = S.pop();
					diagonals.add(un);
					diagonals.add(v);
				}
				S.clear();
			}
		}
		
		Iterator<Vertex> iter = diagonals.iterator();
		while (iter.hasNext())
		{
			Vertex v0 = iter.next();
			Vertex v1 = iter.next();
			
			polygon.AddEdge(v0, v1);
		}
	}
	
	public static boolean IsMonotonePolygon(DCEL polygon)
	{
		for (Face f : polygon.GetFaces())
		{
			if (f.IsConvex())
			{
				return true;
			}
		}
		
		HalfEdge e1 = (HalfEdge)polygon.GetEdges().toArray()[0];
		HalfEdge e2 = e1.GetTwin();
		Face innerFace;
		
		if (e1.GetDirection() < 0)
		{
			innerFace = e1.GetFace();
		}
		else
		{
			innerFace = e2.GetFace();
		}
		
		HalfEdge e = innerFace.GetOuter();
		TreeSet<HalfEdge> T = new TreeSet<HalfEdge>(new HorizontalComparator());
		TreeMap<HalfEdge, Vertex> helper = new TreeMap<HalfEdge, Vertex>(new HorizontalComparator());
		List<Vertex> diagonals = new ArrayList<Vertex>();
		ArrayList<HalfEdge> edges = new ArrayList<HalfEdge>();
		do
		{
			edges.add(e);
			e = e.GetNext();
		} while (!e.equals(innerFace.GetOuter()));
		edges.sort(new VerticalComparator());
		
		// Classify each vertex.
		Iterator<HalfEdge> iter = edges.iterator();
		while (iter.hasNext())
		{
			e = iter.next();
			Vertex v = e.GetOrigin();
			Vertex p = e.GetPrev().GetOrigin();
			Vertex n = e.GetNext().GetOrigin();

			float vx = v.GetPoint().GetX();	
			float vy = v.GetPoint().GetY();
			float nx = n.GetPoint().GetX();	
			float ny = n.GetPoint().GetY();
			float px = p.GetPoint().GetX();	
			float py = p.GetPoint().GetY();
			
			float cross = ((nx - vx) * (py - vy)) - ((ny - vy) * (px - vx));
			float dot = ((nx - vx) * (px - vx)) + ((ny - vy) * (py - vy));
			double angle = Math.atan2(cross, dot);
			
			if (angle < 0.0)
			{
				angle += 2 * Math.PI;
			}
			
			if (py < vy && ny < vy)
			{
				if (angle > Math.PI)
				{
					return false;
				}
			}
			else if (py > vy && ny > vy)
			{
				if (angle > Math.PI)
				{
					return false;
				}
			}
		}
		
		return true;
	}
	
	public static void MakeMonotone(DCEL polygon) throws Exception
	{
		if (!polygon.IsSimplePolygon(polygon.GetVertices()))
		{
			throw new Exception("MakeMonotone: Attempted to make a non-simple polygon monotone!");
		}
		
		for (Face f : polygon.GetFaces())
		{
			if (f.IsConvex())
			{
				return;
			}
		}
		
		// Assume simple polygons, two faces.
		HalfEdge e1 = (HalfEdge)polygon.GetEdges().toArray()[0];
		HalfEdge e2 = e1.GetTwin();
		Face innerFace;
		
		if (e1.GetDirection() < 0)
		{
			innerFace = e1.GetFace();
		}
		else
		{
			innerFace = e2.GetFace();
		}
		
		HalfEdge e = innerFace.GetOuter();
		TreeSet<HalfEdge> T = new TreeSet<HalfEdge>(new HorizontalComparator());
		TreeMap<HalfEdge, Vertex> helper = new TreeMap<HalfEdge, Vertex>(new HorizontalComparator());
		List<Vertex> diagonals = new ArrayList<Vertex>();
		ArrayList<HalfEdge> edges = new ArrayList<HalfEdge>();
		do
		{
			edges.add(e);
			e = e.GetNext();
		} while (!e.equals(innerFace.GetOuter()));
		edges.sort(new VerticalComparator());
		
		// Classify each vertex.
		Iterator<HalfEdge> iter = edges.iterator();
		while (iter.hasNext())
		{
			e = iter.next();
			Vertex v = e.GetOrigin();
			Vertex p = e.GetPrev().GetOrigin();
			Vertex n = e.GetNext().GetOrigin();

			float vx = v.GetPoint().GetX();	
			float vy = v.GetPoint().GetY();
			float nx = n.GetPoint().GetX();	
			float ny = n.GetPoint().GetY();
			float px = p.GetPoint().GetX();	
			float py = p.GetPoint().GetY();
			
			float cross = ((nx - vx) * (py - vy)) - ((ny - vy) * (px - vx));
			float dot = ((nx - vx) * (px - vx)) + ((ny - vy) * (py - vy));
			double angle = Math.atan2(cross, dot);
			
			if (angle < 0.0)
			{
				angle += 2 * Math.PI;
			}
			
			// Is Vertex start or split?
			if (py < vy && ny < vy)
			{
				if (angle < Math.PI)
				{
					v.SetType(Vertex.Type.START);
					T.add(v.GetEdge());
					helper.put(v.GetEdge(), v);
				}
				else if (angle > Math.PI)
				{
					v.SetType(Vertex.Type.SPLIT);
					HalfEdge h = T.lower(v.GetEdge());
					diagonals.add(v);
					diagonals.add(helper.get(h));
					helper.put(h, v);
					T.add(v.GetEdge());
					helper.put(v.GetEdge(), v);
				}
			}
			else if (py > vy && ny > vy)
			{
				if (angle < Math.PI)
				{
					v.SetType(Vertex.Type.END);
					if (helper.get(p.GetEdge()).GetType() == Vertex.Type.MERGE)
					{
						diagonals.add(v);
						diagonals.add(helper.get(p));
					}
					T.remove(p.GetEdge());
				}
				else if (angle > Math.PI)
				{
					v.SetType(Vertex.Type.MERGE);
					if (helper.get(p.GetEdge()).GetType() == Vertex.Type.MERGE)
					{
						diagonals.add(v);
						diagonals.add(helper.get(p.GetEdge()));
					}
					T.remove(p.GetEdge());
					HalfEdge h = T.lower(v.GetEdge());
					if (helper.get(h).GetType() == Vertex.Type.MERGE)
					{
						diagonals.add(v);
						diagonals.add(helper.get(h));
					}
					helper.put(h, v);
				}
			}
			else
			{
				// Vertex is on left if a HalfEdge is pointing downward
				if (ny < vy || vy < py)
				{
					v.SetType(Vertex.Type.REGULARL);
					if (helper.get(p).GetType() == Vertex.Type.MERGE)
					{
						diagonals.add(v);
						diagonals.add(helper.get(p));
					}
					T.remove(p.GetEdge());
					T.add(v.GetEdge());
					helper.put(v.GetEdge(), v);
				}
				else if (ny > vy || vy > py)
				{
					v.SetType(Vertex.Type.REGULARR);
					HalfEdge h = T.lower(v.GetEdge());
					if (helper.get(h).GetType() == Vertex.Type.MERGE)
					{
						diagonals.add(v);
						diagonals.add(helper.get(h));
					}
					helper.put(h, v);
				}
				else
				{
					// Break if they're collinear...
					throw new Exception("MakeMonotone: Detected collinear points!");
				}
			}
		}

		Iterator<Vertex> iter2 = diagonals.iterator();
		while (iter2.hasNext())
		{
			Vertex v0 = iter2.next();
			Vertex v1 = iter2.next();
			
			polygon.AddEdge(v0, v1);
		}
	}
}
