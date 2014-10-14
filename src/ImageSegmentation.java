/*
 * Aaron Gutierrez
 * CECS 451: Artificial Intelligence
 * Image Segmentation using a max-flow/min-cut method
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import org.jgrapht.alg.MinSourceSinkCut;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;

public class ImageSegmentation {

	static int numFore = 0;
	static int numBack = 0;

	public static void main(String[] args) throws IOException {
		// Declares a multigraph for computing the max flow and min cut
		DirectedWeightedMultigraph<ArrayList<Integer>, DefaultWeightedEdge> flow = new DirectedWeightedMultigraph<ArrayList<Integer>, DefaultWeightedEdge>(
				DefaultWeightedEdge.class);

		BitmapImage image = new BitmapImage("moon.bmp");
		BitmapImage bg = new BitmapImage("moon.bmp");

		Hashtable<ArrayList<Integer>, ArrayList<Integer>> penalties = new Hashtable<ArrayList<Integer>, ArrayList<Integer>>();

		int width = image.getWidth();
		int height = image.getHeight();

		//center pixel
		int center = image.getPixel((width + 1) / 2, (height + 1) / 2);
		// max distance
		int MD = distance(((width + 1) / 2), ((height + 1) / 2), width + 1,
				height + 1);

		// System.out.println(MD);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				ArrayList<Integer> pixel = new ArrayList<Integer>();
				pixel.add(i);
				pixel.add(j);
				flow.addVertex(pixel);
			}
		}
		// Add the source and sink to the graph
		ArrayList<Integer> source = new ArrayList<Integer>();
		source.add(-1);
		source.add(0);

		ArrayList<Integer> sink = new ArrayList<Integer>();
		sink.add(0);
		sink.add(-1);

		flow.addVertex(source);
		flow.addVertex(sink);

		// System.out.println(flow);
		// loop over all pixels by column
		// Computes necessary values for each pixel, and then adds pixel to the graph
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				// The pixel
				ArrayList<Integer> pixel = new ArrayList<Integer>();
				ArrayList<Integer> edges = new ArrayList<Integer>();
				pixel.add(i);
				pixel.add(j);
				// Weight: The sum of all penalty edges leaving the pixel
				int w;
				// a and b are for foreground and background labeling,
				// respectively
				double a;
				double b;
				// Special cases
				// Edge Pixels only have 2 edges
				if (i == 0 && j == 0) {
					int b1 = image.getPixel(i, j) & 0xFF;
					int g1 = image.getPixel(i, j) >> 8 & 0xFF;
					int r1 = image.getPixel(i, j) >> 16 & 0xFF;

					int b2 = image.getPixel(i + 1, j) & 0xFF;
					int g2 = image.getPixel(i + 1, j) >> 8 & 0xFF;
					int r2 = image.getPixel(i + 1, j) >> 16 & 0xFF;

					ArrayList<Integer> n1 = new ArrayList<Integer>();
					n1.add(i + 1);
					n1.add(j);

					int b3 = image.getPixel(i, j + 1) & 0xFF;
					int g3 = image.getPixel(i, j + 1) >> 8 & 0xFF;
					int r3 = image.getPixel(i, j + 1) >> 16 & 0xFF;

					ArrayList<Integer> n2 = new ArrayList<Integer>();
					n2.add(i);
					n2.add(j + 1);

					edges.add(rgbDistance(r1, g1, b1, r2, g2, b2));
					edges.add(rgbDistance(r1, g1, b1, r3, g3, b3));

					w = sumArrayList(edges);
					b = ((distance(i, j, ((width + 1) / 2), ((height + 1) / 2)))
							/ MD * (w / 100));
					a = (w / 100) - b;

					DefaultWeightedEdge edge1 = flow.addEdge(source, pixel);
					flow.setEdgeWeight(edge1, a);

					DefaultWeightedEdge edge2 = flow.addEdge(pixel, sink);
					flow.setEdgeWeight(edge2, b);

					DefaultWeightedEdge edge3 = flow.addEdge(pixel, n1);
					flow.setEdgeWeight(edge3, edges.get(0));

					DefaultWeightedEdge edge4 = flow.addEdge(pixel, n2);
					flow.setEdgeWeight(edge4, edges.get(1));

					penalties.put(pixel, edges);
				} else if (i == width - 1 && j == 0) {
					int b1 = image.getPixel(i, j) & 0xFF;
					int g1 = image.getPixel(i, j) >> 8 & 0xFF;
					int r1 = image.getPixel(i, j) >> 16 & 0xFF;

					int b2 = image.getPixel(i - 1, j) & 0xFF;
					int g2 = image.getPixel(i - 1, j) >> 8 & 0xFF;
					int r2 = image.getPixel(i - 1, j) >> 16 & 0xFF;

					ArrayList<Integer> n1 = new ArrayList<Integer>();
					n1.add(i - 1);
					n1.add(j);

					int b3 = image.getPixel(i, j + 1) & 0xFF;
					int g3 = image.getPixel(i, j + 1) >> 8 & 0xFF;
					int r3 = image.getPixel(i, j + 1) >> 16 & 0xFF;

					ArrayList<Integer> n2 = new ArrayList<Integer>();
					n2.add(i);
					n2.add(j + 1);

					edges.add(rgbDistance(r1, g1, b1, r2, g2, b2));
					edges.add(rgbDistance(r1, g1, b1, r3, g3, b3));

					w = sumArrayList(edges);
					b = ((distance(i, j, ((width + 1) / 2), ((height + 1) / 2)))
							/ MD * (w / 100));
					a = (w / 100) - b;

					DefaultWeightedEdge edge1 = flow.addEdge(source, pixel);
					flow.setEdgeWeight(edge1, a);

					DefaultWeightedEdge edge2 = flow.addEdge(pixel, sink);
					flow.setEdgeWeight(edge2, b);

					DefaultWeightedEdge edge3 = flow.addEdge(pixel, n1);
					flow.setEdgeWeight(edge3, edges.get(0));

					DefaultWeightedEdge edge4 = flow.addEdge(pixel, n2);
					flow.setEdgeWeight(edge4, edges.get(1));

					penalties.put(pixel, edges);
				} else if (i == 0 && j == height - 1) {
					int b1 = image.getPixel(i, j) & 0xFF;
					int g1 = image.getPixel(i, j) >> 8 & 0xFF;
					int r1 = image.getPixel(i, j) >> 16 & 0xFF;

					int b2 = image.getPixel(i, j - 1) & 0xFF;
					int g2 = image.getPixel(i, j - 1) >> 8 & 0xFF;
					int r2 = image.getPixel(i, j - 1) >> 16 & 0xFF;

					ArrayList<Integer> n1 = new ArrayList<Integer>();
					n1.add(i);
					n1.add(j - 1);

					int b3 = image.getPixel(i + 1, j) & 0xFF;
					int g3 = image.getPixel(i + 1, j) >> 8 & 0xFF;
					int r3 = image.getPixel(i + 1, j) >> 16 & 0xFF;

					ArrayList<Integer> n2 = new ArrayList<Integer>();
					n2.add(i + 1);
					n2.add(j);

					edges.add(rgbDistance(r1, g1, b1, r2, g2, b2));
					edges.add(rgbDistance(r1, g1, b1, r3, g3, b3));

					w = sumArrayList(edges);
					b = ((distance(i, j, ((width + 1) / 2), ((height + 1) / 2)))
							/ MD * (w / 100));
					a = (w / 100) - b;

					DefaultWeightedEdge edge1 = flow.addEdge(source, pixel);
					flow.setEdgeWeight(edge1, a);

					DefaultWeightedEdge edge2 = flow.addEdge(pixel, sink);
					flow.setEdgeWeight(edge2, b);

					DefaultWeightedEdge edge3 = flow.addEdge(pixel, n1);
					flow.setEdgeWeight(edge3, edges.get(0));

					DefaultWeightedEdge edge4 = flow.addEdge(pixel, n2);
					flow.setEdgeWeight(edge4, edges.get(1));

					penalties.put(pixel, edges);
				} else if (i == width - 1 && j == height - 1) {
					int b1 = image.getPixel(i, j) & 0xFF;
					int g1 = image.getPixel(i, j) >> 8 & 0xFF;
					int r1 = image.getPixel(i, j) >> 16 & 0xFF;

					int b2 = image.getPixel(i, j - 1) & 0xFF;
					int g2 = image.getPixel(i, j - 1) >> 8 & 0xFF;
					int r2 = image.getPixel(i, j - 1) >> 16 & 0xFF;

					ArrayList<Integer> n1 = new ArrayList<Integer>();
					n1.add(i);
					n1.add(j - 1);

					int b3 = image.getPixel(i - 1, j) & 0xFF;
					int g3 = image.getPixel(i - 1, j) >> 8 & 0xFF;
					int r3 = image.getPixel(i - 1, j) >> 16 & 0xFF;

					ArrayList<Integer> n2 = new ArrayList<Integer>();
					n2.add(i - 1);
					n2.add(j);

					edges.add(rgbDistance(r1, g1, b1, r2, g2, b2));
					edges.add(rgbDistance(r1, g1, b1, r3, g3, b3));

					w = sumArrayList(edges);
					b = ((distance(i, j, ((width + 1) / 2), ((height + 1) / 2)))
							/ MD * (w / 100));
					a = (w / 100) - b;

					DefaultWeightedEdge edge1 = flow.addEdge(source, pixel);
					flow.setEdgeWeight(edge1, a);

					DefaultWeightedEdge edge2 = flow.addEdge(pixel, sink);
					flow.setEdgeWeight(edge2, b);

					DefaultWeightedEdge edge3 = flow.addEdge(pixel, n1);
					flow.setEdgeWeight(edge3, edges.get(0));

					DefaultWeightedEdge edge4 = flow.addEdge(pixel, n2);
					flow.setEdgeWeight(edge4, edges.get(1));

					penalties.put(pixel, edges);
				} else if (i == 0) {
					int b1 = image.getPixel(i, j) & 0xFF;
					int g1 = image.getPixel(i, j) >> 8 & 0xFF;
					int r1 = image.getPixel(i, j) >> 16 & 0xFF;

					// Left edge
					// Up
					// Down
					// Right
					int b2 = image.getPixel(i, j - 1) & 0xFF;
					int g2 = image.getPixel(i, j - 1) >> 8 & 0xFF;
					int r2 = image.getPixel(i, j - 1) >> 16 & 0xFF;

					ArrayList<Integer> n1 = new ArrayList<Integer>();
					n1.add(i);
					n1.add(j - 1);

					int b3 = image.getPixel(i + 1, j) & 0xFF;
					int g3 = image.getPixel(i + 1, j) >> 8 & 0xFF;
					int r3 = image.getPixel(i + 1, j) >> 16 & 0xFF;

					ArrayList<Integer> n2 = new ArrayList<Integer>();
					n2.add(i + 1);
					n2.add(j);

					int b4 = image.getPixel(i, j + 1) & 0xFF;
					int g4 = image.getPixel(i, j + 1) >> 8 & 0xFF;
					int r4 = image.getPixel(i, j + 1) >> 16 & 0xFF;

					ArrayList<Integer> n3 = new ArrayList<Integer>();
					n3.add(i);
					n3.add(j + 1);

					edges.add(rgbDistance(r1, g1, b1, r2, g2, b2));
					edges.add(rgbDistance(r1, g1, b1, r3, g3, b3));
					edges.add(rgbDistance(r1, g1, b1, r4, g4, b4));

					w = sumArrayList(edges);
					b = ((distance(i, j, ((width + 1) / 2), ((height + 1) / 2)))
							/ MD * (w / 100));
					a = (w / 100) - b;

					DefaultWeightedEdge edge1 = flow.addEdge(source, pixel);
					flow.setEdgeWeight(edge1, a);

					DefaultWeightedEdge edge2 = flow.addEdge(pixel, sink);
					flow.setEdgeWeight(edge2, b);

					DefaultWeightedEdge edge3 = flow.addEdge(pixel, n1);
					flow.setEdgeWeight(edge3, edges.get(0));

					DefaultWeightedEdge edge4 = flow.addEdge(pixel, n2);
					flow.setEdgeWeight(edge4, edges.get(1));

					DefaultWeightedEdge edge5 = flow.addEdge(pixel, n3);
					flow.setEdgeWeight(edge5, edges.get(2));

					penalties.put(pixel, edges);
				} else if (j == 0) {
					int b1 = image.getPixel(i, j) & 0xFF;
					int g1 = image.getPixel(i, j) >> 8 & 0xFF;
					int r1 = image.getPixel(i, j) >> 16 & 0xFF;

					// Top Edge
					// Down
					// Right
					// Left
					int b2 = image.getPixel(i, j + 1) & 0xFF;
					int g2 = image.getPixel(i, j + 1) >> 8 & 0xFF;
					int r2 = image.getPixel(i, j + 1) >> 16 & 0xFF;

					ArrayList<Integer> n1 = new ArrayList<Integer>();
					n1.add(i);
					n1.add(j + 1);

					int b3 = image.getPixel(i + 1, j) & 0xFF;
					int g3 = image.getPixel(i + 1, j) >> 8 & 0xFF;
					int r3 = image.getPixel(i + 1, j) >> 16 & 0xFF;

					ArrayList<Integer> n2 = new ArrayList<Integer>();
					n2.add(i + 1);
					n2.add(j);

					int b4 = image.getPixel(i - 1, j) & 0xFF;
					int g4 = image.getPixel(i - 1, j) >> 8 & 0xFF;
					int r4 = image.getPixel(i - 1, j) >> 16 & 0xFF;

					ArrayList<Integer> n3 = new ArrayList<Integer>();
					n3.add(i - 1);
					n3.add(j);

					edges.add(rgbDistance(r1, g1, b1, r2, g2, b2));
					edges.add(rgbDistance(r1, g1, b1, r3, g3, b3));
					edges.add(rgbDistance(r1, g1, b1, r4, g4, b4));

					w = sumArrayList(edges);
					b = ((distance(i, j, ((width + 1) / 2), ((height + 1) / 2)))
							/ MD * (w / 100));
					a = (w / 100) - b;

					DefaultWeightedEdge edge1 = flow.addEdge(source, pixel);
					flow.setEdgeWeight(edge1, a);

					DefaultWeightedEdge edge2 = flow.addEdge(pixel, sink);
					flow.setEdgeWeight(edge2, b);

					DefaultWeightedEdge edge3 = flow.addEdge(pixel, n1);
					flow.setEdgeWeight(edge3, edges.get(0));

					DefaultWeightedEdge edge4 = flow.addEdge(pixel, n2);
					flow.setEdgeWeight(edge4, edges.get(1));

					DefaultWeightedEdge edge5 = flow.addEdge(pixel, n3);
					flow.setEdgeWeight(edge5, edges.get(2));

					penalties.put(pixel, edges);
				} else if (i == width - 1) {
					int b1 = image.getPixel(i, j) & 0xFF;
					int g1 = image.getPixel(i, j) >> 8 & 0xFF;
					int r1 = image.getPixel(i, j) >> 16 & 0xFF;

					// Right edge
					// Down
					// Up
					// Left
					int b2 = image.getPixel(i, j + 1) & 0xFF;
					int g2 = image.getPixel(i, j + 1) >> 8 & 0xFF;
					int r2 = image.getPixel(i, j + 1) >> 16 & 0xFF;

					ArrayList<Integer> n1 = new ArrayList<Integer>();
					n1.add(i);
					n1.add(j + 1);

					int b3 = image.getPixel(i, j - 1) & 0xFF;
					int g3 = image.getPixel(i, j - 1) >> 8 & 0xFF;
					int r3 = image.getPixel(i, j - 1) >> 16 & 0xFF;

					ArrayList<Integer> n2 = new ArrayList<Integer>();
					n2.add(i);
					n2.add(j - 1);

					int b4 = image.getPixel(i - 1, j) & 0xFF;
					int g4 = image.getPixel(i - 1, j) >> 8 & 0xFF;
					int r4 = image.getPixel(i - 1, j) >> 16 & 0xFF;

					ArrayList<Integer> n3 = new ArrayList<Integer>();
					n3.add(i - 1);
					n3.add(j);

					edges.add(rgbDistance(r1, g1, b1, r2, g2, b2));
					edges.add(rgbDistance(r1, g1, b1, r3, g3, b3));
					edges.add(rgbDistance(r1, g1, b1, r4, g4, b4));

					w = sumArrayList(edges);
					b = ((distance(i, j, ((width + 1) / 2), ((height + 1) / 2)))
							/ MD * (w / 100));
					a = (w / 100) - b;

					DefaultWeightedEdge edge1 = flow.addEdge(source, pixel);
					flow.setEdgeWeight(edge1, a);

					DefaultWeightedEdge edge2 = flow.addEdge(pixel, sink);
					flow.setEdgeWeight(edge2, b);

					DefaultWeightedEdge edge3 = flow.addEdge(pixel, n1);
					flow.setEdgeWeight(edge3, edges.get(0));

					DefaultWeightedEdge edge4 = flow.addEdge(pixel, n2);
					flow.setEdgeWeight(edge4, edges.get(1));

					DefaultWeightedEdge edge5 = flow.addEdge(pixel, n3);
					flow.setEdgeWeight(edge5, edges.get(2));

					penalties.put(pixel, edges);
				} else if (j == height - 1) {
					int r1 = image.getPixel(i, j) & 0xFF;
					int g1 = image.getPixel(i, j) >> 8 & 0xFF;
					int b1 = image.getPixel(i, j) >> 16 & 0xFF;

					// Bottom edge
					// Right
					// Up
					// Left
					int b2 = image.getPixel(i + 1, j) & 0xFF;
					int g2 = image.getPixel(i + 1, j) >> 8 & 0xFF;
					int r2 = image.getPixel(i + 1, j) >> 16 & 0xFF;

					ArrayList<Integer> n1 = new ArrayList<Integer>();
					n1.add(i + 1);
					n1.add(j);

					int b3 = image.getPixel(i, j - 1) & 0xFF;
					int g3 = image.getPixel(i, j - 1) >> 8 & 0xFF;
					int r3 = image.getPixel(i, j - 1) >> 16 & 0xFF;

					ArrayList<Integer> n2 = new ArrayList<Integer>();
					n2.add(i);
					n2.add(j - 1);

					int b4 = image.getPixel(i - 1, j) & 0xFF;
					int g4 = image.getPixel(i - 1, j) >> 8 & 0xFF;
					int r4 = image.getPixel(i - 1, j) >> 16 & 0xFF;

					ArrayList<Integer> n3 = new ArrayList<Integer>();
					n3.add(i - 1);
					n3.add(j);

					edges.add(rgbDistance(r1, g1, b1, r2, g2, b2));
					edges.add(rgbDistance(r1, g1, b1, r3, g3, b3));
					edges.add(rgbDistance(r1, g1, b1, r4, g4, b4));

					w = sumArrayList(edges);
					b = ((distance(i, j, ((width + 1) / 2), ((height + 1) / 2)))
							/ MD * (w / 100));
					a = (w / 100) - b;

					DefaultWeightedEdge edge1 = flow.addEdge(source, pixel);
					flow.setEdgeWeight(edge1, a);

					DefaultWeightedEdge edge2 = flow.addEdge(pixel, sink);
					flow.setEdgeWeight(edge2, b);

					DefaultWeightedEdge edge3 = flow.addEdge(pixel, n1);
					flow.setEdgeWeight(edge3, edges.get(0));

					DefaultWeightedEdge edge4 = flow.addEdge(pixel, n2);
					flow.setEdgeWeight(edge4, edges.get(1));

					DefaultWeightedEdge edge5 = flow.addEdge(pixel, n3);
					flow.setEdgeWeight(edge5, edges.get(2));

					penalties.put(pixel, edges);
				}
				// All pixels in the center
				else {
					int b1 = image.getPixel(i, j) & 0xFF;
					int g1 = image.getPixel(i, j) >> 8 & 0xFF;
					int r1 = image.getPixel(i, j) >> 16 & 0xFF;

					int b2 = image.getPixel(i, j + 1) & 0xFF;
					int g2 = image.getPixel(i, j + 1) >> 8 & 0xFF;
					int r2 = image.getPixel(i, j + 1) >> 16 & 0xFF;

					ArrayList<Integer> n1 = new ArrayList<Integer>();
					n1.add(i);
					n1.add(j + 1);

					int b3 = image.getPixel(i, j - 1) & 0xFF;
					int g3 = image.getPixel(i, j - 1) >> 8 & 0xFF;
					int r3 = image.getPixel(i, j - 1) >> 16 & 0xFF;

					ArrayList<Integer> n2 = new ArrayList<Integer>();
					n2.add(i);
					n2.add(j - 1);

					int b4 = image.getPixel(i - 1, j) & 0xFF;
					int g4 = image.getPixel(i - 1, j) >> 8 & 0xFF;
					int r4 = image.getPixel(i - 1, j) >> 16 & 0xFF;

					ArrayList<Integer> n3 = new ArrayList<Integer>();
					n3.add(i - 1);
					n3.add(j);

					int b5 = image.getPixel(i + 1, j) & 0xFF;
					int g5 = image.getPixel(i + 1, j) >> 8 & 0xFF;
					int r5 = image.getPixel(i + 1, j) >> 16 & 0xFF;

					ArrayList<Integer> n4 = new ArrayList<Integer>();
					n4.add(i + 1);
					n4.add(j);

					// System.out.println(r1 + " " + g1 + " " + b1 + " \n " +
					// image.getPixelAsColor(i, j));

					edges.add(rgbDistance(r1, g1, b1, r2, g2, b2));
					edges.add(rgbDistance(r1, g1, b1, r3, g3, b3));
					edges.add(rgbDistance(r1, g1, b1, r4, g4, b4));
					edges.add(rgbDistance(r1, g1, b1, r5, g5, b5));

					w = sumArrayList(edges);
					b = ((distance(i, j, ((width + 1) / 2), ((height + 1) / 2)))
							/ MD * (w / 100));
					a = (w / 100) - b;

					DefaultWeightedEdge edge1 = flow.addEdge(source, pixel);
					flow.setEdgeWeight(edge1, a);

					DefaultWeightedEdge edge2 = flow.addEdge(pixel, sink);
					flow.setEdgeWeight(edge2, b);

					DefaultWeightedEdge edge3 = flow.addEdge(pixel, n1);
					flow.setEdgeWeight(edge3, edges.get(0));

					DefaultWeightedEdge edge4 = flow.addEdge(pixel, n2);
					flow.setEdgeWeight(edge4, edges.get(1));

					DefaultWeightedEdge edge5 = flow.addEdge(pixel, n3);
					flow.setEdgeWeight(edge5, edges.get(2));

					DefaultWeightedEdge edge6 = flow.addEdge(pixel, n4);
					flow.setEdgeWeight(edge6, edges.get(3));

					penalties.put(pixel, edges);
				}

			}
		}

		// compute the min cut of the graph
		MinSourceSinkCut<ArrayList<Integer>, DefaultWeightedEdge> cut = new MinSourceSinkCut<ArrayList<Integer>, DefaultWeightedEdge>(
				flow);
		cut.computeMinCut(source, sink);

		int fg = cut.getSourcePartition().size() - 1;
		System.out.println("Foreground: " + fg);
		System.out
				.println("Background: " + (cut.getSinkPartition().size() - 1));
		System.out.println("Total: "
				+ (cut.getSourcePartition().size()
						+ cut.getSinkPartition().size() - 2));
		int tolerance = (int) (0.05 * fg);
		System.out.println("Tolerance: " + (fg - tolerance) + " : "
				+ (fg + tolerance));

		for (ArrayList<Integer> i : cut.getSourcePartition()) {
			bg.setPixel(i.get(0), i.get(1), bg.getColourForRGB(255, 255, 255));
		}
		for (ArrayList<Integer> i : cut.getSinkPartition()) {
			bg.setPixel(i.get(0), i.get(1), bg.getColourForRGB(0, 0, 0));
		}
		bg.display();
		image.display();

	}

	/*
	 * Helper functions
	 */
	
	// Distance between two pixels is defined as |a - x| + |b - y|
	public static int distance(int a, int b, int x, int y) {
		return Math.abs(a - x) + Math.abs(b - y);
	}

	// Color distance between two pixels is defined as the difference between
	// the red, blue, and green components of the two pixels, summed together
	public static int rgbDistance(int r1, int g1, int b1, int r2, int g2, int b2) {
		return Math.abs(r1 - r2) + Math.abs(b1 - b2) + Math.abs(g1 - g2);
	}

	// Returns the sum of the given arraylist 
	public static int sumArrayList(ArrayList<Integer> a) {
		int sum = 0;
		for (Integer i : a) {
			sum += i;
		}
		return sum;
	}

}
