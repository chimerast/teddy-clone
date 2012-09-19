package st.chimera.graphics;

import java.awt.*;
import java.awt.geom.*;

import javax.vecmath.*;

public class ArrowStroke implements Stroke {
    Stroke stroke;

    public ArrowStroke(float w) {
        stroke = new BasicStroke(w);
    }

    public Shape createStrokedShape(Shape p) {
        Area ret = new Area();

        PathIterator itr = p.getPathIterator(null, 1.0);

        double[] point = new double[6];
        Line2D.Double line = new Line2D.Double();
        double sx = 0.0, sy = 0.0;

        while (!itr.isDone()) {
            int type = itr.currentSegment(point);
            if (type == PathIterator.SEG_MOVETO) {
                sx = line.x1 = point[0];
                sy = line.y1 = point[1];
            } else {
                if (type == PathIterator.SEG_LINETO) {
                    line.x2 = point[0];
                    line.y2 = point[1];
                } else {
                    line.x2 = sx;
                    line.y2 = sy;
                }

                Vector2f vec = new Vector2f((float)(line.x2 - line.x1), (float)(line.y2 - line.y1));
                vec.normalize();

                GeneralPath tri = new GeneralPath();
                tri.moveTo((float)(line.x2 - vec.x * 12.0 + vec.y * 4.0), (float)(line.y2 - vec.y
                        * 12.0 - vec.x * 4.0));
                tri.lineTo((float)line.x2, (float)line.y2);
                tri.lineTo((float)(line.x2 - vec.x * 12.0 - vec.y * 4.0), (float)(line.y2 - vec.y
                        * 12.0 + vec.x * 4.0));
                tri.lineTo((float)(line.x2 - vec.x * 8.0), (float)(line.y2 - vec.y * 8.0));
                tri.closePath();

                ret.add(new Area(tri));
                ret.add(new Area(stroke.createStrokedShape(line)));

                line.x1 = line.x2;
                line.y1 = line.y2;
            }
            itr.next();
        }
        return ret;
    }
}