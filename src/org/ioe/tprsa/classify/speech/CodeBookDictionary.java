package org.ioe.tprsa.classify.speech;

import java.io.Serializable;

import org.ioe.tprsa.classify.speech.vq.Centroid;
import org.ioe.tprsa.db.Model;

public class CodeBookDictionary implements Serializable, Model {

	private static final long serialVersionUID = 2094442679375932181L;
	protected int dimension;
	protected Centroid[] cent;

	public int getDimension() {
		return dimension;
	}

	public void setDimension(int dimension) {
		this.dimension = dimension;
	}

	public Centroid[] getCent() {
		return cent;
	}

	public void setCent(Centroid[] cent) {
		this.cent = cent;
	}
}