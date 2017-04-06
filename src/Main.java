import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.*;

public class Main {
	private static Point[] s_RegularPentagon = new Point[5];
	private static Point[] s_IrregularPolygon = new Point[5];
	private static Point[] s_IntersectingPolygon = new Point[4];
	
	private static DCEL s_RegularPentagonDCEL;
	private static DCEL s_IrregularPolygonDCEL;
	private static DCEL s_IntersectingPolygonDCEL;
	
	private static Frame s_PentagonFrame;
	private static Frame s_PolygonFrame;
	
	private static class Frame extends JFrame
	{
		private JPanel m_Panel;
		private DCEL m_Polygon;
		private List<Point> m_Points;
		private PointLocation m_PointLocation;
		
		public Frame(String title, DCEL p)
		{
			this.setTitle(title);
			this.setSize(300, 300);
			this.setDefaultCloseOperation(EXIT_ON_CLOSE);
			
			this.m_Panel = new Panel();
			this.add(this.m_Panel);
			this.setVisible(true);
			
			this.m_Polygon = p;
			this.m_Points = new ArrayList<Point>();
		}
		
		public Frame(DCEL p)
		{
			this("Demo", p);
		}
		
		private class Panel extends JPanel
		{
			public Panel()
			{
				this.setPreferredSize(new Dimension(300, 300));
			}
			
			@Override
			public void paintComponent(Graphics g)
			{
				final float scale = 50.0f;
				final Point center = new Point(2.5f, 2.5f);
				// Draw the polygon
				Iterator<HalfEdge> iter = Frame.this.m_Polygon.GetEdges().iterator();
				while (iter.hasNext())
				{
					HalfEdge e = iter.next();
					Point p = e.GetOrigin().GetPoint();
					Point q = e.GetTwin().GetOrigin().GetPoint();
					g.drawLine((int)((p.GetX() + center.GetX()) * scale), (int)((-p.GetY() + center.GetY()) * scale),
							(int)((q.GetX() + center.GetX()) * scale), (int)((-q.GetY() + center.GetY()) * scale));
				}
				
				for (Point p : Frame.this.m_Points)
				{
					g.drawRect((int)((p.GetX() + center.GetX()) * scale), (int)((-p.GetY() + center.GetY()) * scale), 1, 1);
				}
				
				if (Frame.this.m_PointLocation != null)
				{
					for (Slab s : Frame.this.m_PointLocation.GetSlabs())
					{
						float l = s.GetLeftBound().GetX();
						float r = s.GetRightBound().GetX();

						g.drawLine((int)((l + center.GetX()) * scale), 0, (int)((l + center.GetX()) * scale), 300);
						g.drawLine((int)((r + center.GetX()) * scale), 0, (int)((r + center.GetX()) * scale), 300);
					}
				}
			}
		}
		
		public void AddPoint(Point p)
		{
			this.m_Points.add(p);
			this.m_Panel.repaint();
		}
		
		public void AddPointLocation(PointLocation p)
		{
			this.m_PointLocation = p;
			this.m_Panel.repaint();
		}
	}
	
	private static void TestConstructSimplePolygon()
	{
		System.out.println("Testing: ConstructSimplePolygon");
		System.out.println("\n-------------------------------\n");

		Main.s_RegularPentagonDCEL = new DCEL();
		Main.s_IrregularPolygonDCEL = new DCEL();
		Main.s_IntersectingPolygonDCEL = new DCEL();
		
		System.out.println("Testing a Regular Pentagon:");
		
		float angle = (float)Math.toRadians(180.0 - ((Main.s_RegularPentagon.length - 2) * 180.0) / Main.s_RegularPentagon.length);
		float angleOffset = (float)Math.toRadians(15.0);
		float radius = 1.0f;
		for (int i = 0; i < Main.s_RegularPentagon.length; ++i)
		{
			Main.s_RegularPentagon[i] = new Point((float)(radius * Math.cos(i * angle + angleOffset)),
					(float)(radius * Math.sin(i * angle + angleOffset)));
			System.out.println("\tVertex " + (i + 1) + ": " + Main.s_RegularPentagon[i]);
		}
		
		try {
			Main.s_RegularPentagonDCEL.ConstructSimplePolygon(Main.s_RegularPentagon);
			
			Iterator<Face> iter = Main.s_RegularPentagonDCEL.GetFaces().iterator();
			
			while (iter.hasNext())
			{
				System.out.println(iter.next().toString());
			}
			
			System.out.println("Regular Pentagon Passed!");
		}
		catch (Exception e)
		{
			System.out.println("Regular Pentagon Failed!");
			e.printStackTrace();
		}
		
		System.out.println("\n-------------------------------\n");
		System.out.println("Testing an Irregular, Simple Polygon:");
		
		Main.s_IrregularPolygon[0] = new Point(0.25f, 0.0f);
		System.out.println("\tVertex 0: " + Main.s_IrregularPolygon[0]);
		Main.s_IrregularPolygon[1] = new Point(2.0f, 0.25f);
		System.out.println("\tVertex 1: " + Main.s_IrregularPolygon[1]);
		Main.s_IrregularPolygon[2] = new Point(1.0f, 1.0f);
		System.out.println("\tVertex 2: " + Main.s_IrregularPolygon[2]);
		Main.s_IrregularPolygon[3] = new Point(0.5f, 0.5f);
		System.out.println("\tVertex 3: " + Main.s_IrregularPolygon[3]);
		Main.s_IrregularPolygon[4] = new Point(0.0f, 1.5f);
		System.out.println("\tVertex 4: " + Main.s_IrregularPolygon[4]);
		
		try {
			Main.s_IrregularPolygonDCEL.ConstructSimplePolygon(Main.s_IrregularPolygon);
			
			Iterator<Face> iter = Main.s_IrregularPolygonDCEL.GetFaces().iterator();
			
			while (iter.hasNext())
			{
				System.out.println(iter.next().toString());
			}
			
			System.out.println("Irregular, Simple Polygon Passed!");
		}
		catch (Exception e)
		{
			System.out.println("Irregular, Simple Polygon Failed!");
			e.printStackTrace();
		}
		
		System.out.println("\n-------------------------------\n");
		System.out.println("Testing an Intersecting Polygon:");
		
		Main.s_IntersectingPolygon[0] = new Point(0.0f, 0.0f);
		System.out.println("\tVertex 0: " + Main.s_IntersectingPolygon[0]);
		Main.s_IntersectingPolygon[1] = new Point(1.0f, 1.0f);
		System.out.println("\tVertex 1: " + Main.s_IntersectingPolygon[1]);
		Main.s_IntersectingPolygon[2] = new Point(-1.0f, 1.0f);
		System.out.println("\tVertex 2: " + Main.s_IntersectingPolygon[2]);
		Main.s_IntersectingPolygon[3] = new Point(0.0f, 2.0f);
		System.out.println("\tVertex 3: " + Main.s_IntersectingPolygon[3]);
		
		try {
			Main.s_IntersectingPolygonDCEL.ConstructSimplePolygon(Main.s_IntersectingPolygon);
			
			Iterator<Face> iter = Main.s_IntersectingPolygonDCEL.GetFaces().iterator();
			
			while (iter.hasNext())
			{
				System.out.println(iter.next().toString());
			}

			System.out.println("Intersecting Polygon Failed!");
		}
		catch (Exception e)
		{
			System.out.println("Intersecting Polygon Passed!");
			//e.printStackTrace();
		}
		System.out.println("\n-------------------------------\n");
	}
	
	public static void TestMakeMonotone()
	{
		System.out.println("Testing: MakeMonotone");
		System.out.println("\n-------------------------------\n");
		
		System.out.println("Testing a Regular Pentagon:");
		
		for (int i = 0; i < Main.s_RegularPentagon.length; ++i)
		{
			System.out.println("\tVertex " + (i + 1) + ": " + Main.s_RegularPentagon[i]);
		}

		try {
			DCEL regularPentagon = new DCEL();
			
			regularPentagon.ConstructSimplePolygon(Main.s_RegularPentagon);
			Triangulation.MakeMonotone(regularPentagon);
			//Triangulation.TriangulateMonotonePolygon(regularPentagon);
			
			Iterator<Face> iter = regularPentagon.GetFaces().iterator();
			
			while (iter.hasNext())
			{
				System.out.println(iter.next().toString());
			}
			
			System.out.println("Regular Pentagon Passed!");
		}
		catch (Exception e)
		{
			System.out.println("Regular Pentagon Failed!");
			e.printStackTrace();
		}
		
		System.out.println("\n-------------------------------\n");
		System.out.println("Testing an Irregular, Simple Polygon:");
		
		try {
			Triangulation.MakeMonotone(Main.s_IrregularPolygonDCEL);
			
			Iterator<Face> iter = Main.s_IrregularPolygonDCEL.GetFaces().iterator();
			
			while (iter.hasNext())
			{
				System.out.println(iter.next().toString());
			}
			
			System.out.println("Irregular, Simple Polygon Passed!");
		}
		catch (Exception e)
		{
			System.out.println("Irregular, Simple Polygon Failed!");
			e.printStackTrace();
		}
		
		System.out.println("\n-------------------------------\n");
		System.out.println("Testing an Intersecting Polygon:");
		
		System.out.println("\tVertex 0: " + Main.s_IntersectingPolygon[0]);
		System.out.println("\tVertex 1: " + Main.s_IntersectingPolygon[1]);
		System.out.println("\tVertex 2: " + Main.s_IntersectingPolygon[2]);
		System.out.println("\tVertex 3: " + Main.s_IntersectingPolygon[3]);
		
		try {
			Triangulation.MakeMonotone(Main.s_IntersectingPolygonDCEL);
			
			Iterator<Face> iter = Main.s_IntersectingPolygonDCEL.GetFaces().iterator();
			
			while (iter.hasNext())
			{
				System.out.println(iter.next().toString());
			}

			System.out.println("Intersecting Polygon Failed!");
		}
		catch (Exception e)
		{
			System.out.println("Intersecting Polygon Passed!");
			//e.printStackTrace();
		}
		System.out.println("\n-------------------------------\n");
	}
	
	public static void TestTriangulation()
	{
		System.out.println("Testing: TriangulateMonotonePolygon");
		System.out.println("\n-------------------------------\n");
		
		System.out.println("Testing a Regular Pentagon:");
		
		for (int i = 0; i < Main.s_RegularPentagon.length; ++i)
		{
			System.out.println("\tVertex " + (i + 1) + ": " + Main.s_RegularPentagon[i]);
		}

		try {
			// For some reason the triangulation is messing up one of the faces.
			Triangulation.TriangulateMonotonePolygon(Main.s_RegularPentagonDCEL);
			
			Iterator<Face> iter = Main.s_RegularPentagonDCEL.GetFaces().iterator();
			
			while (iter.hasNext())
			{
				System.out.println(iter.next().toString());
			}
			
			System.out.println("Regular Pentagon Passed!");
		}
		catch (Exception e)
		{
			System.out.println("Regular Pentagon Failed!");
			e.printStackTrace();
		}
		
		System.out.println("\n-------------------------------\n");
		System.out.println("Testing an Irregular, Simple Polygon:");
		
		try {
			Triangulation.TriangulateMonotonePolygon(Main.s_IrregularPolygonDCEL);
			
			Iterator<Face> iter = Main.s_IrregularPolygonDCEL.GetFaces().iterator();
			
			while (iter.hasNext())
			{
				System.out.println(iter.next().toString());
			}
			
			System.out.println("Irregular, Simple Polygon Passed!");
		}
		catch (Exception e)
		{
			System.out.println("Irregular, Simple Polygon Failed!");
			e.printStackTrace();
		}
		
		System.out.println("\n-------------------------------\n");
		System.out.println("Testing an Intersecting Polygon:");
		
		System.out.println("\tVertex 0: " + Main.s_IntersectingPolygon[0]);
		System.out.println("\tVertex 1: " + Main.s_IntersectingPolygon[1]);
		System.out.println("\tVertex 2: " + Main.s_IntersectingPolygon[2]);
		System.out.println("\tVertex 3: " + Main.s_IntersectingPolygon[3]);
		
		try {
			Triangulation.TriangulateMonotonePolygon(Main.s_IntersectingPolygonDCEL);
			
			Iterator<Face> iter = Main.s_IntersectingPolygonDCEL.GetFaces().iterator();
			
			while (iter.hasNext())
			{
				System.out.println(iter.next().toString());
			}

			System.out.println("Intersecting Polygon Failed!");
		}
		catch (Exception e)
		{
			System.out.println("Intersecting Polygon Passed!");
			//e.printStackTrace();
		}
		System.out.println("\n-------------------------------\n");
	}
	
	private static void TestPointLocation()
	{
		Point p;
		HalfEdge h;
		
		System.out.println("Testing: TestPointLocation");
		System.out.println("\n-------------------------------\n");
		
		System.out.println("Testing a Regular Pentagon:");
		
		try
		{
			PointLocation pentagon = new PointLocation(Main.s_RegularPentagonDCEL);
			Main.s_PentagonFrame.AddPointLocation(pentagon);
			p = new Point(0.25f, -0.5f);
			Main.s_PentagonFrame.AddPoint(p);
			h = pentagon.Query(p);
			System.out.println("Point " + p + " is in the face bounded by " + h);
			p = new Point(0.0f, 0.0f);
			Main.s_PentagonFrame.AddPoint(p);
			h = pentagon.Query(p);
			System.out.println("Point " + p + " is in the face bounded by " + h);
			p = new Point(0.5f, 0.4f);
			Main.s_PentagonFrame.AddPoint(p);
			h = pentagon.Query(p);
			System.out.println("Point " + p + " is in the face bounded by " + h);
			
			System.out.println("Regular Pentagon Passed!");
		}
		catch (Exception e)
		{
			System.out.println("Regular Pentagon Failed!");
			e.printStackTrace();
		}

		System.out.println("\n-------------------------------\n");
		System.out.println("Testing an Irregular, Simple Polygon:");
		
		try
		{
			PointLocation irregular = new PointLocation(Main.s_IrregularPolygonDCEL);
			Main.s_PolygonFrame.AddPointLocation(irregular);
			p = new Point(0.5f, 0.25f);
			Main.s_PolygonFrame.AddPoint(p);
			h = irregular.Query(p);
			System.out.println("Point " + p + " is in the face bounded by " + h);
			p = new Point(1.0f, 0.5f);
			Main.s_PolygonFrame.AddPoint(p);
			h = irregular.Query(p);
			System.out.println("Point " + p + " is in the face bounded by " + h);
			p = new Point(0.25f, 0.5f);
			Main.s_PolygonFrame.AddPoint(p);
			h = irregular.Query(p);
			System.out.println("Point " + p + " is in the face bounded by " + h);
			
			System.out.println("Irregular, Simple Polygon Passed!");
		}
		catch (Exception e)
		{
			System.out.println("Irregular, Simple Polygon Failed!");
			e.printStackTrace();
		}
		System.out.println("\n-------------------------------\n");
	}

	
	public static void main(String[] args) {
		// Should really update these to use asserts, but... laziness...
		Main.TestConstructSimplePolygon();
		Main.s_PentagonFrame = new Frame("Pentagon", Main.s_RegularPentagonDCEL);
		Main.s_PolygonFrame = new Frame("Irregular, Simple Polygon", Main.s_IrregularPolygonDCEL);
		Main.TestMakeMonotone();
		Main.TestTriangulation();
		Main.TestPointLocation();
		Main.s_PentagonFrame.revalidate();
		Main.s_PolygonFrame.revalidate();
	}
}
