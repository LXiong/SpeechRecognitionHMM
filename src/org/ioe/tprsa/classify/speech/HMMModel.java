package org.ioe.tprsa.classify.speech;

import java.io.Serializable;

import org.ioe.tprsa.db.Model;

public class HMMModel implements Serializable, Model {

	private static final long serialVersionUID = -8699362751441096092L;
	protected int num_obSeq;
	protected double transition[][];
	protected double output[][];
	protected double pi[];

	public int getNum_obSeq() {
		return num_obSeq;
	}

	public double[][] getTransition() {
		return transition;
	}

	public void setTransition(double[][] transition) {
		this.transition = transition;
	}

	public double[][] getOutput() {
		return output;
	}

	public void setOutput(double[][] output) {
		this.output = output;
	}

	public double[] getPi() {
		return pi;
	}

	public void setPi(double[] pi) {
		this.pi = pi;
	}
}