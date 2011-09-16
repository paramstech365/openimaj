package org.openimaj.math.geometry.shape.util.polygon;

import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.util.PolygonUtils;

/**
    *
    */
public class TopPolygonNode
{
	PolygonNode top_node = null;

	public PolygonNode add_local_min( double x, double y )
	{
		PolygonNode existing_min = top_node;

		top_node = new PolygonNode( existing_min, x, y );

		return top_node;
	}

	public void merge_left( PolygonNode p, PolygonNode q )
	{
		/* Label contour as a hole */
		q.proxy.hole = true;

		if( p.proxy != q.proxy )
		{
			/* Assign p's vertex list to the left end of q's list */
			p.proxy.v[PolygonUtils.RIGHT].next = q.proxy.v[PolygonUtils.LEFT];
			q.proxy.v[PolygonUtils.LEFT] = p.proxy.v[PolygonUtils.LEFT];

			/* Redirect any p.proxy references to q.proxy */
			PolygonNode target = p.proxy;
			for( PolygonNode node = top_node; (node != null); node = node.next )
			{
				if( node.proxy == target )
				{
					node.active = 0;
					node.proxy = q.proxy;
				}
			}
		}
	}

	public void merge_right( PolygonNode p, PolygonNode q )
	{
		/* Label contour as external */
		q.proxy.hole = false;

		if( p.proxy != q.proxy )
		{
			/* Assign p's vertex list to the right end of q's list */
			q.proxy.v[PolygonUtils.RIGHT].next = p.proxy.v[PolygonUtils.LEFT];
			q.proxy.v[PolygonUtils.RIGHT] = p.proxy.v[PolygonUtils.RIGHT];

			/* Redirect any p->proxy references to q->proxy */
			PolygonNode target = p.proxy;
			for( PolygonNode node = top_node; (node != null); node = node.next )
			{
				if( node.proxy == target )
				{
					node.active = 0;
					node.proxy = q.proxy;
				}
			}
		}
	}

	public int count_contours()
	{
		int nc = 0;
		for( PolygonNode polygon = top_node; 
			(polygon != null); polygon = polygon.next )
		{
			if( polygon.active != 0 )
			{
				/* Count the vertices in the current contour */
				int nv = 0;
				for( VertexNode v = polygon.proxy.v[PolygonUtils.LEFT]; 
					(v != null); v = v.next )
				{
					nv++;
				}

				/* Record valid vertex counts in the active field */
				if( nv > 2 )
				{
					polygon.active = nv;
					nc++;
				}
				else
				{
					/* Invalid contour: just free the heap */
					// VertexNode nextv = null ;
					// for (VertexNode v= polygon.proxy.v[LEFT]; (v != null); v
					// = nextv)
					// {
					// nextv= v.next;
					// v = null ;
					// }
					polygon.active = 0;
				}
			}
		}
		return nc;
	}

	public Polygon getResult( Class<Polygon> polyClass )
	{
		Polygon result = new Polygon();
		int num_contours = count_contours();
		if( num_contours > 0 )
		{
			int c = 0;
			PolygonNode npoly_node = null;
			for( PolygonNode poly_node = top_node; 
			     (poly_node != null); poly_node = npoly_node )
			{
				npoly_node = poly_node.next;
				
				if( poly_node.active != 0 )
				{
					Polygon polygon = result;
					
					if( num_contours > 1 )
					{
						polygon = new Polygon();
					}
					
					if( poly_node.proxy.hole )
					{
						polygon.setIsHole( poly_node.proxy.hole );
					}

					// ------------------------------------------------------------------------
					// --- This algorithm puts the verticies into the Polygon in
					// reverse order ---
					// ------------------------------------------------------------------------
					for( VertexNode vtx = poly_node.proxy.v[PolygonUtils.LEFT]; 
					     (vtx != null); vtx = vtx.next )
					{
						polygon.addVertex( (float) vtx.x, (float) vtx.y );
					}
					
					if( num_contours > 1 )
					{
						result.addInnerPolygon( polygon );
					}
					
					c++;
				}
			}

			// -----------------------------------------
			// --- Sort holes to the end of the list ---
			// -----------------------------------------
			Polygon orig = result;
			result = new Polygon();
			for( int i = 0; i < orig.getNumInnerPoly(); i++ )
			{
				Polygon inner = orig.getInnerPoly( i );
				if( !inner.isHole() )
				{
					result.addInnerPolygon( inner );
				}
			}
			for( int i = 0; i < orig.getNumInnerPoly(); i++ )
			{
				Polygon inner = orig.getInnerPoly( i );
				if( inner.isHole() )
				{
					result.addInnerPolygon( inner );
				}
			}
		}
		return result;
	}

	public void print()
	{
		System.out.println( "---- out_poly ----" );
		int c = 0;
		PolygonNode npoly_node = null;
		for( PolygonNode poly_node = top_node; (poly_node != null); 
			poly_node = npoly_node )
		{
			System.out.println( "contour=" + c + "  active=" + 
					poly_node.active + "  hole=" + poly_node.proxy.hole );
			npoly_node = poly_node.next;
			if( poly_node.active != 0 )
			{
				int v = 0;
				for( VertexNode vtx = poly_node.proxy.v[PolygonUtils.LEFT]; (vtx != null); vtx = vtx.next )
				{
					System.out.println( "v=" + v + "  vtx.x=" + vtx.x + "  vtx.y=" + vtx.y );
				}
				c++;
			}
		}
	}
}
