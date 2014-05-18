package org.ioe.tprsa.classify.speech.vq;

import org.ioe.tprsa.classify.speech.CodeBookDictionary;
import org.ioe.tprsa.db.DataBase;
import org.ioe.tprsa.db.ObjectIODataBase;

/**
 * last updated on June 15, 2002<br>
 * <b>description:</b> Codebook for Vector Quantization component<br>
 * <b>calls:</b> Centroid, Points<br>
 * <b>called by:</b> volume, train<br>
 * <b>input:</b> speech signal<br>
 * <b>output:</b> set of centroids, set of indices
 */
public class Codebook {
	/**
	 * split factor (should be in the range of 0.01 <= SPLIT <= 0.05)
	 */
	protected final double SPLIT = 0.01;
	/**
	 * minimum distortion
	 */
	protected final double MIN_DISTORTION = 0.1;
	/**
	 * Codebook size - number of codewords (codevectors)<br>
	 * default is: 256
	 */
	protected int codebook_size = 256;
	/**
	 * centroids array
	 */
	protected Centroid centroids[];
	/**
	 * training points
	 */
	protected Points pt[];
	/**
	 * dimension /////no of features
	 */
	protected int dimension;

	/**
	 * constructor to train a Codebook with given training points and default
	 * Codebook size (256)<br>
	 * calls: none<br>
	 * called by: trainCodebook
	 * 
	 * @param tmpPt
	 *            training vectors
	 */
	public Codebook(Points tmpPt[]) {
		this.pt = tmpPt;

		// make sure there are enough training points to train the Codebook
		if (pt.length >= codebook_size) {
			dimension = pt[0].getDimension();
			initialize();
		} else {
			System.out.println("err: not enough training points");
		}
	}

	/**
	 * constructor to load a saved Codebook from external file<br>
	 * calls: Centroid<br>
	 * called by: volume
	 */
	public Codebook() {
		DataBase db = new ObjectIODataBase();
		db.setType("cbk");
		CodeBookDictionary cbd = (CodeBookDictionary) db.readModel(null);
		dimension = cbd.getDimension();
		centroids = cbd.getCent();
	}

	/**
	 * creates a Codebook using LBG algorithm which includes K-means<br>
	 * calls: Centroid<br>
	 * called by: Codebook
	 */
	protected void initialize() {
		double distortion_before_update = 0; // distortion measure before
												// updating centroids
		double distortion_after_update = 0; // distortion measure after update
											// centroids

		// design a 1-vector Codebook
		centroids = new Centroid[1];

		// then initialize it with (0, 0) coordinates
		double origin[] = new double[dimension];
		centroids[0] = new Centroid(origin);

		// initially, all training points will belong to 1 single cell
        for (Points aPt : pt) {
            centroids[0].add(aPt, 0);
        }

		// calls update to set the initial codevector as the average of all
		// points
		centroids[0].update();

		// Iteration 1: repeat splitting step and K-means until required number
		// of codewords is reached
		while (centroids.length < codebook_size) {
			// split codevectors by a binary splitting method
			split();

			// group training points to centroids closest to them
			groupPtoC();

			// Iteration 2: perform K-means algorithm
			do {
                for (Centroid centroid : centroids) {
                    distortion_before_update += centroid.getDistortion();
                    centroid.update();
                }

				// regroup
				groupPtoC();

                for (Centroid centroid : centroids) {
                    distortion_after_update += centroid.getDistortion();
                }

			} while (Math.abs(distortion_after_update - distortion_before_update) < MIN_DISTORTION);
		}
	}

	/**
	 * save Codebook to cbk object file<br>
	 * calls: none<br>
	 * called by: train
	 */
	public void saveToFile() {
		DataBase db = new ObjectIODataBase();
		db.setType("cbk");
		CodeBookDictionary cbd = new CodeBookDictionary();
		// no need to save all the points,
		// must be removed in objectIO, to reduce the size of file
        for (Centroid centroid : centroids) {
            centroid.pts.removeAllElements();
        }
		cbd.setDimension(dimension);
		cbd.setCent(centroids);
		db.saveModel(cbd, null);// filepath is not used
		// System.out.println("Showing parameters");
		// showParameters();

	}

	/**
	 * splitting algorithm to increase number of centroids by multiple of 2<br>
	 * calls: Centroid<br>
	 * called by: Codebook
	 */
	protected void split() {
		System.out.println("Centroids length now becomes " + centroids.length + 2);
		Centroid temp[] = new Centroid[centroids.length * 2];
		double tCo[][];
		for (int i = 0; i < temp.length; i += 2) {
			tCo = new double[2][dimension];
			for (int j = 0; j < dimension; j++) {
				tCo[0][j] = centroids[i / 2].getCo(j) * (1 + SPLIT);
			}
			temp[i] = new Centroid(tCo[0]);
			for (int j = 0; j < dimension; j++) {
				tCo[1][j] = centroids[i / 2].getCo(j) * (1 - SPLIT);
			}
			temp[i + 1] = new Centroid(tCo[1]);
		}

		// replace old centroids array with new one
		centroids = new Centroid[temp.length];
		centroids = temp;
	}

	/**
	 * quantize the input array of points in k-dimensional space<br>
	 * calls: none<br>
	 * called by: volume
	 * 
	 * @param pts
	 *            points to be quantized
	 * @return quantized index array
	 */
	public int[] quantize(Points pts[]) {
		int output[] = new int[pts.length];
		for (int i = 0; i < pts.length; i++) {
			output[i] = closestCentroidToPoint(pts[i]);
		}
		return output;
	}

	/**
	 * finds the closest Centroid to a specific Points<br>
	 * calls: none<br>
	 * called by: Codebook
	 * 
	 * @param pt
	 *            Points
	 * @return index number of the closest Centroid
	 */
	private int closestCentroidToPoint(Points pt) {
		double tmp_dist;
		double lowest_dist = 0; // = getDistance(pt, centroids[0]);
		int lowest_index = 0;

		for (int i = 0; i < centroids.length; i++) {
			tmp_dist = getDistance(pt, centroids[i]);
			if (tmp_dist < lowest_dist || i == 0) {
				lowest_dist = tmp_dist;
				lowest_index = i;
			}
		}
		return lowest_index;
	}

	/**
	 * finds the closest Centroid to a specific Centroid<br>
	 * calls: none<br>
	 * called by: Codebook
	 * 
	 * @param c Points
	 * @return index number of the closest Centroid
	 */
	private int closestCentroidToCentroid(Centroid c) {
		double tmp_dist;
		double lowest_dist = Double.MAX_VALUE;
		int lowest_index = 0;
		for (int i = 0; i < centroids.length; i++) {
			tmp_dist = getDistance(c, centroids[i]);
			if (tmp_dist < lowest_dist && centroids[i].getNumPts() > 1) {
				lowest_dist = tmp_dist;
				lowest_index = i;
			}
		}
		return lowest_index;
	}

	/**
	 * finds the closest Points in c2's cell to c1<br>
	 * calls: none<br>
	 * called by: Codebook
	 * 
	 * @param c1
	 *            first Centroid
	 * @param c2
	 *            second Centroid
	 * @return index of Points
	 */
	private int closestPoint(Centroid c1, Centroid c2) {
		double tmp_dist;
		double lowest_dist = getDistance(c2.getPoint(0), c1);
		int lowest_index = 0;
		for (int i = 1; i < c2.getNumPts(); i++) {
			tmp_dist = getDistance(c2.getPoint(i), c1);
			if (tmp_dist < lowest_dist) {
				lowest_dist = tmp_dist;
				lowest_index = i;
			}
		}
		return lowest_index;
	}

	/**
	 * grouping points to cells<br>
	 * calls: none<br>
	 * called by: Codebook
	 */
	private void groupPtoC() {
		// find closest Centroid and assign Points to it
        for (Points aPt : pt) {
            int index = closestCentroidToPoint(aPt);
            centroids[index].add(aPt, getDistance(aPt, centroids[index]));
        }
		// make sure that all centroids have at least one Points assigned to it
		// no cell should be empty or else NaN error will occur due to division
		// of 0 by 0
        for (Centroid centroid : centroids) {
            if (centroid.getNumPts() == 0) {
                // find the closest Centroid with more than one points assigned
                // to it
                int index = closestCentroidToCentroid(centroid);
                // find the closest Points in the closest Centroid's cell
                int closestIndex = closestPoint(centroid, centroids[index]);
                Points closestPt = centroids[index].getPoint(closestIndex);
                centroids[index].remove(closestPt, getDistance(closestPt, centroids[index]));
                centroid.add(closestPt, getDistance(closestPt, centroid));
            }
        }
	}

	/**
	 * calculates the distance of a Points to a Centroid<br>
	 * calls: none<br>
	 * called by: Codebook
	 * 
	 * @param tPt
	 *            points
	 * @param tC
	 *            Centroid
	 */
	private double getDistance(Points tPt, Centroid tC) {
		double distance = 0;
		double temp;
		for (int i = 0; i < dimension; i++) {
			temp = tPt.getCo(i) - tC.getCo(i);
			distance += temp * temp;
		}
		distance = Math.sqrt(distance);
		return distance;
	}
}