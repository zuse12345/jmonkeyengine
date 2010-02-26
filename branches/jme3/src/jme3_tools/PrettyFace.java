
import com.jme3.math.FastMath;
import com.jme3.math.Triangle;
import com.jme3.math.Triangle;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import java.util.List;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import static com.jme3.math.FastMath.*;

public class PrettyFace {

    List uv;
    float width, height;
    List<PrettyFace> children;
    float xoff, yoff;
    boolean hasParent;
    boolean rot;

    private static final float angleBetweenVecs(Vector3f v1, Vector3f v2){
        return FastMath.acos( v1.dot(v2) / (v1.length() * v2.length()) );
    }

    public PrettyFace(List<PrettyFace> data){
        if (data.size() == 2){
            data.get(1).xoff = data.get(0).width;
            width = data.get(0).width * 2;
            height = data.get(0).height;
        }else if (data.size() == 4){
            float d = data.get(0).width;
            data.get(1).xoff += d;
            data.get(2).yoff += d;
            data.get(3).xoff += d;
            data.get(3).yoff += d;
            width = height = d * 2;
        }

        for (PrettyFace pf : data)
		  pf.hasParent = true;

        children = data;
    }

//    public PrettyFace(List data){
//        uv = data;
//
//        float lens1ord = data.x;
//        if (data.y != 0){
//            float lens2ord = data.y;
//            width = 0;
//        }

        /*
        if type(data) == tuple:
			# 2 blender faces
			# f, (len_min, len_mid, len_max)
			self.uv = data

			f1, lens1, lens1ord = data[0]
			if data[1]:
				f2, lens2, lens2ord = data[1]
				self.width  = (lens1[lens1ord[0]] + lens2[lens2ord[0]])/2
				self.height = (lens1[lens1ord[1]] + lens2[lens2ord[1]])/2
			else: # 1 tri :/
				self.width = lens1[0]
				self.height = lens1[1]

			self.children = []


		else: # blender face
			self.uv = data.uv

			cos = [v.co for v in data]
			self.width  = ((cos[0]-cos[1]).length + (cos[2]-cos[3]).length)/2
			self.height = ((cos[1]-cos[2]).length + (cos[0]-cos[3]).length)/2

			self.children = []
         */
//    }

    public void spin(){
        /*
         def spin(self):
		if self.uv and len(self.uv) == 4:
			self.uv = self.uv[1], self.uv[2], self.uv[3], self.uv[0]
         */

        float tmp = width;
        width = height;
        height = tmp;

        tmp = xoff;
        xoff = yoff;
        yoff = tmp;

        rot = !rot;
        if (children != null){
            for (PrettyFace pf : children)
                 pf.spin();
        }
    }

    private static class AngleIndex implements Comparable<AngleIndex> {

        int index;
        float angle;

        public AngleIndex(int index, float angle) {
            this.index = index;
            this.angle = angle;
        }

        public int compareTo(AngleIndex other){
            return angle < other.angle ? -1 : (angle > other.angle ? 1 : 0);
        }

    }

    private AngleIndex[] getTriAngles(Vector3f v1, Vector3f v2, Vector3f v3){
        float a1 = angleBetweenVecs(v2.subtract(v1), v3.subtract(v1));
        float a2 = angleBetweenVecs(v1.subtract(v2), v3.subtract(v2));
        float a3 = 180 - (a1 + a2); // AngleBetweenVecs(v2-v3,v1-v3)
        return new AngleIndex[]
            { new AngleIndex(0, a1),
              new AngleIndex(1, a2),
              new AngleIndex(2, a3) };
    }

    private void setUV(Triangle f, Vector3f p1, Vector3f p2, Vector3f p3){

        // cos =
        //v1 = cos[0]-cos[1]
        //v2 = cos[1]-cos[2]
        //v3 = cos[2]-cos[0]
        AngleIndex[] angles_co = getTriAngles(f.get1(), f.get2(), f.get3());
        Arrays.sort(angles_co);
        int[] I = new int[angles_co.length];
        //for (int i = 0; i < I.length; i++)
        //    I[i] = angles_co[i];

        //List fuv = f.uv;
        if (rot){

        }else{

        }
//        if self.rot:
//                fuv[I[2]][:] = p1
//                fuv[I[1]][:] = p2
//                fuv[I[0]][:] = p3
//        else:
//                fuv[I[2]][:] = p1
//                fuv[I[0]][:] = p2
//                fuv[I[1]][:] = p3
    }

    public void place(int xoff, int yoff, int xfac, int yfac, int margin_w, int margin_h){
        xoff += this.xoff;
        yoff += this.yoff;
        for (PrettyFace pf : children)
             pf.place(xoff, yoff, xfac, yfac, margin_w, margin_h);

        List uv = this.uv;
        if (uv == null)
            return;

        float x1 = xoff;
        float y1 = yoff;
	   float x2 = xoff + this.width;
	   float y2 = yoff + this.height;

	   // Scale the values
        x1 = x1 / xfac + margin_w;
        x2 = x2 / xfac - margin_w;
	   y1 = y1 / yfac + margin_h;
	   y2 = y2 / yfac - margin_h;

         if (uv.size() == 2){
             // match the order of angle sizes of the 3d verts with the UV angles and rotate.
//             Vector2f lensord = uv.get(0);
//             Triangle f = new Triangle();
//             setUV(new Triangle(), new Vector2f(x1, y1),

         }
    }



//		# 2 Tri pairs
//		if len(uv) == 2:
//			f, lens, lensord = uv[0]
//
//			set_uv(f,  (x1,y1),  (x1, y2-margin_h),  (x2-margin_w, y1))
//
//			if uv[1]:
//				f, lens, lensord = uv[1]
//				set_uv(f,  (x2,y2),  (x2, y1+margin_h),  (x1+margin_w, y2))
//
//		else: # 1 QUAD
//			uv[1][:] = x1,y1
//			uv[2][:] = x1,y2
//			uv[3][:] = x2,y2
//			uv[0][:] = x2,y1
//
//	def __hash__(self):
//		# None unique hash
//		return self.width, self.height


//def lightmap_uvpack(	meshes,\
//PREF_SEL_ONLY=			True,\
//PREF_NEW_UVLAYER=		False,\
//PREF_PACK_IN_ONE=		False,\
//PREF_APPLY_IMAGE=		False,\
//PREF_IMG_PX_SIZE=		512,\
//PREF_BOX_DIV= 			8,\
//PREF_MARGIN_DIV=		512):
	
//	BOX_DIV if the maximum division of the UV map that
//	a box may be consolidated into.
//	Basicly, a lower value will be slower but waist less space
//	and a higher value will have more clumpy boxes but more waisted space

    private static class TriLens {

        Triangle f;
        float[] lens;
        int[] lens_order;

        public TriLens(Triangle f, float[] lens, int[] lens_order) {
            this.f = f;
            this.lens = lens;
            this.lens_order = lens_order;
        }

    }

    private static TriLens trylens(Triangle f){
            // f must be a tri
            Vector3f v01 = f.get1().subtract(f.get2());
            Vector3f v12 = f.get2().subtract(f.get3());
            Vector3f v20 = f.get3().subtract(f.get1());

            float[] lens = new float[]{ v01.length(), v12.length(), v20.length() };

            int lens_min = -1;
            float min = Float.MAX_VALUE;
            int lens_max = -1;
            float max = Float.MIN_VALUE;
            for (int i = 0; i < lens.length; i++){
                if (lens[i] < min){
                    min = lens[i];
                    lens_min = i;
                }
                if (lens[i] > max){
                    max = lens[i];
                    lens_max = i;
                }
            }
            assert lens_min >= 0 && lens_min <= 2;
            assert lens_max >= 0 && lens_max <= 2;

            int lens_mid = -1;
            for (int i = 0; i < 3; i++){
                if (i != lens_min && i != lens_max){
                    lens_mid = i;
                    break;
                }
            }

            int[] lens_order = new int[]{ lens_min, lens_mid, lens_max };
//            lens_order = lens_min, lens_mid, lens_max

//            return f, lens, lens_order
            return new TriLens(f, lens, lens_order);
    }

    private static float trilensdiff(TriLens t1, TriLens t2){
        return abs(t1.lens[t1.lens_order[0]] - t2.lens[t2.lens_order[0]])
             + abs(t1.lens[t1.lens_order[1]] - t2.lens[t2.lens_order[1]])
             + abs(t1.lens[t1.lens_order[2]] - t2.lens[t2.lens_order[2]]);
    }

//    def trilensdiff(t1,t2):
//				return\
//				abs(t1[1][t1[2][0]]-t2[1][t2[2][0]])+\
//				abs(t1[1][t1[2][1]]-t2[1][t2[2][1]])+\
//				abs(t1[1][t1[2][2]]-t2[1][t2[2][2]])

    public static void lightmapPack(Mesh[] meshes){
        /*
	if (meshes == null)
		return;

        List<Triangle> faces = new ArrayList<Triangle>();
	for (Mesh me : meshes){
		faces.addAll(me.getTriangles());
        }

        List<TriLens> tri_lengths = new ArrayList<TriLens>(faces.size());
        for (int i = 0; i < tri_lengths.size()); i++){
            tri_lengths.add(trylens(faces.get(i)));
        }

        List<PrettyFace> pretty_faces = new ArrayList<PrettyFace>();
        while (tri_lengths.size() > 0){
            TriLens tri1 = tri_lengths.remove(tri_lengths.size()-1);
            if (tri_lengths.size() == 0){
                pretty_faces.add(new PrettyFace(tri1));
            }

            int best_tri_index = -1;
            float best_tri_diff = 100000000.0f;

            for (int i = 0; i < tri_lengths.size(); i++){
                TriLens tri2 = tri_lengths.get(i);
                float diff = trilensdiff(tri1, tri2);
                if (diff < best_tri_diff){
                    best_tri_index = i;
                    best_tri_diff = diff;
                }
            }

            pretty_faces.add(new PrettyFace(tri1, tri_lengths.remove(best_tri_index)));
        }

        float max_area = 0;
        float min_area = 100000000.0f;
        float tot_area = 0;
        for (Triangle f : faces){
            float area = computeArea(f);
            if (area > max_area) max_area = area;
            if (area < min_area) min_area = area;
            tot_area += area;
        }

        float max_len = sqrt(max_area);
        float min_len = sqrt(min_area);
        float side_len = sqrt(tot_area);

        float cur_len = max_len;

        List<Float> lengths = new ArrayList<Float>();
        while (cur_len > min_len){
            lengths.add(cur_len);
            cur_len = cur_len / 2;

            // Dont allow boxes smaller then the margin
            // since we contract on the margin, boxes that are smaller will create errors
            // print curr_len, side_len/MARGIN_DIV

            if (cur_len / 4 < side_len / PREF_MARGIN_DIV)
                break;
        }

        if (lengths.size() == 0)
            lengths.add(cur_len);

        SortedMap<Float, Integer> lengths_to_ints = new TreeMap<Float, Integer>();
        int l_int = 1;
        for (int i = lengths.size() - 1; i >= 0; i--){
            float l = lengths.get(i);
            lengths_to_ints.put(l, l_int);
            l_int *= 2;
        }

        // apply quantized values.
        for (PrettyFace pf : pretty_faces){
            float w = pf.width;
            float h = pf.height;
            float bestw_diff = 1000000000.0f;
            float besth_diff = 1000000000.0f;
            int new_w = 0;
            int new_h = 0;

            for (Map.Entry<Float, Integer> li : lengths_to_ints.entrySet()){
                float l = li.getKey();
                int i = li.getValue();
                float d = abs(l - w);
                if (d < bestw_diff){
                    bestw_diff = d;
                    new_w = i; // ASSIGN INT VERSION
                }

                d = abs(l - h);
                if (d < besth_diff){
                    besth_diff = d;
                    new_h = i;
                }
            }

            pf.width = new_w;
            pf.height = new_h;
            if (new_w > new_h)
                pf.spin();

        }


        // Since the boxes are sized in powers of 2, we can neatly group them into bigger squares
        // this is done hierarchily, so that we may avoid running the pack function
        // on many thousands of boxes, (under 1k is best) because it would get slow.
        // Using an off and even dict us usefull because they are packed differently
        // where w/h are the same, their packed in groups of 4
        // where they are different they are packed in pairs
        //
        // After this is done an external pack func is done that packs the whole group.
        Map<Float, PrettyFace> even_dict = new HashMap<Float, PrettyFace>();
        Map<Vector2f, PrettyFace> odd_dict = new HashMap<Vector2f, PrettyFace>();
        for (PrettyFace pf : pretty_faces){
            float w = pf.width;
            float h = pf.height;
            if (w == h)
                even_dict.put(w, pf);
            else
                odd_dict.put(new Vector2f(w,h), pf);
        }

	// Count the number of boxes consolidated, only used for stats.
	int c = 0;
        */
        // This is tricky. the total area of all packed boxes, then squt that to get an estimated size
        // this is used then converted into out INT space so we can compare it with
        // the ints assigned to the boxes size
        // and divided by BOX_DIV, basicly if BOX_DIV is 8
        // ...then the maximum box consolidataion (recursive grouping) will have a max width & height
        // ...1/8th of the UV size.
        // ...limiting this is needed or you end up with bug unused texture spaces
        // ...however if its too high, boxpacking is way too slow for high poly meshes.
        /*
        float_to_int_factor = lengths_to_ints[0][0]
        if (float_to_int_factor > 0){
                max_int_dimension = int(((side_len / float_to_int_factor)) / PREF_BOX_DIV)
                ok = True
        }else{
                max_int_dimension = 0.0 // wont be used
                ok = False
        }
		# RECURSIVE prettyface grouping
		while ok:
			ok = False

			# Tall boxes in groups of 2
			for d, boxes in odd_dict.items():
				if d[1] < max_int_dimension:
					#\boxes.sort(key = lambda a: len(a.children))
					while len(boxes) >= 2:
						# print "foo", len(boxes)
						ok = True
						c += 1
						pf_parent = prettyface([boxes.pop(), boxes.pop()])
						pretty_faces.append(pf_parent)

						w,h = pf_parent.width, pf_parent.height

						if w>h: raise "error"

						if w==h:
							even_dict.setdefault(w, []).append(pf_parent)
						else:
							odd_dict.setdefault((w,h), []).append(pf_parent)

			# Even boxes in groups of 4
			for d, boxes in even_dict.items():
				if d < max_int_dimension:
					# py 2.3 compat
					try:	boxes.sort(key = lambda a: len(a.children))
					except:	boxes.sort(lambda a, b: cmp(len(a.children), len(b.children)))

					while len(boxes) >= 4:
						# print "bar", len(boxes)
						ok = True
						c += 1

						pf_parent = prettyface([boxes.pop(), boxes.pop(), boxes.pop(), boxes.pop()])
						pretty_faces.append(pf_parent)
						w = pf_parent.width # width and weight are the same
						even_dict.setdefault(w, []).append(pf_parent)

		del even_dict
		del odd_dict

		orig = len(pretty_faces)

		pretty_faces = [pf for pf in pretty_faces if not pf.has_parent]

		# spin every second prettyface
		# if there all vertical you get less efficiently used texture space
		i = len(pretty_faces)
		d = 0
		while i:
			i -=1
			pf = pretty_faces[i]
			if pf.width != pf.height:
				d += 1
				if d % 2: # only pack every second
					pf.spin()
					# pass

		print 'Consolidated', c, 'boxes, done'
		# print 'done', orig, len(pretty_faces)


		# boxes2Pack.append([islandIdx, w,h])
		print '\tPacking Boxes', len(pretty_faces), '...',
		boxes2Pack = [ [0.0, 0.0, pf.width, pf.height, i] for i, pf in enumerate(pretty_faces)]
		packWidth, packHeight = Geometry.BoxPack2D(boxes2Pack)

		# print packWidth, packHeight

		packWidth = float(packWidth)
		packHeight = float(packHeight)

		margin_w = ((packWidth) / PREF_MARGIN_DIV)/ packWidth
		margin_h = ((packHeight) / PREF_MARGIN_DIV) / packHeight

		# print margin_w, margin_h
		print 'done'

		# Apply the boxes back to the UV coords.
		print '\twriting back UVs',
		for i, box in enumerate(boxes2Pack):
			pretty_faces[i].place(box[0], box[1], packWidth, packHeight, margin_w, margin_h)
			# pf.place(box[1][1], box[1][2], packWidth, packHeight, margin_w, margin_h)
		print 'done'
*/
//	if not Draw.PupBlock('Lightmap Pack', [\
//	'Context...',
//	('Active Object', PREF_ACT_ONLY, 'If disabled, include other selected objects for packing the lightmap.'),\
//	('Selected Faces', PREF_SEL_ONLY, 'Use only selected faces from all selected meshes.'),\
//	'Image & UVs...',
//	('Share Tex Space', PREF_PACK_IN_ONE, 'Objects Share texture space, map all objects into 1 uvmap'),\
//	('New UV Layer', PREF_NEW_UVLAYER, 'Create a new UV layer for every mesh packed'),\
//	('New Image', PREF_APPLY_IMAGE, 'Assign new images for every mesh (only one if shared tex space enabled)'),\
//	('Image Size', PREF_IMG_PX_SIZE, 64, 5000, 'Width and Height for the new image'),\
//	'UV Packing...',
//	('Pack Quality: ', PREF_BOX_DIV, 1, 48, 'Pre Packing before the complex boxpack'),\
//	('Margin: ', PREF_MARGIN_DIV, 0.001, 1.0, 'Size of the margin as a division of the UV')\
//	]):

//	lightmap_uvpack(meshes,\
//			PREF_SEL_ONLY.val,\
//			PREF_NEW_UVLAYER.val,\
//			PREF_PACK_IN_ONE.val,\
//			PREF_APPLY_IMAGE.val,\
//			PREF_IMG_PX_SIZE.val,\
//			PREF_BOX_DIV.val,\
//			int(1/(PREF_MARGIN_DIV.val/100)))
    }
}