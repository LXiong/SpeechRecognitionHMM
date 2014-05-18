package org.ioe.tprsa.audio.feature;

public class MFCC {

	private int numMelFilters = 30;// how much
    private double lowerFilterFreq = 80.00;// FmelLow
	private double samplingRate;
	private double upperFilterFreq;
    private int samplePerFrame;
	FFT fft;
	DCT dct;

	public MFCC(int samplePerFrame, int samplingRate, int numCepstra) {
		this.samplePerFrame = samplePerFrame;
		this.samplingRate = samplingRate;
        upperFilterFreq = samplingRate / 2.0;
		fft = new FFT();
		dct = new DCT(numCepstra, numMelFilters);
	}

	public double[] doMFCC(float[] framedSignal) {
		// Magnitude Spectrum
        double[] bin = magnitudeSpectrum(framedSignal);

		// prepare filter for for melFilter
		int cbin[] = fftBinIndices();// same for all
		// process Mel Filterbank
		double fbank[] = melFilter(bin, cbin);
		// magnitudeSpectrum and bin filter indices

		// Non-linear transformation
		double f[] = nonLinearTransformation(fbank);

		// Cepstral coefficients, by DCT
		return dct.performDCT(f);
	}

	private double[] magnitudeSpectrum(float frame[]) {
		double magSpectrum[] = new double[frame.length];
		// calculate FFT for current frame
		fft.computeFFT(frame);
		// System.err.println("FFT SUCCEED");
		// calculate magnitude spectrum
		for (int k = 0; k < frame.length; k++) {
			magSpectrum[k] = Math.sqrt(fft.real[k] * fft.real[k] + fft.imag[k] * fft.imag[k]);
		}
		return magSpectrum;
	}

	private int[] fftBinIndices() {
		int cbin[] = new int[numMelFilters + 2];
		cbin[0] = (int) Math.round(lowerFilterFreq / samplingRate * samplePerFrame);// cbin0
		cbin[cbin.length - 1] = (samplePerFrame / 2);// cbin24
		for (int i = 1; i <= numMelFilters; i++) {// from cbin1 to cbin23
			double fc = centerFreq(i);// center freq for i th filter
			cbin[i] = (int) Math.round(fc / samplingRate * samplePerFrame);
		}
		return cbin;
	}

	/**
	 * performs mel filter operation
	 * 
	 * @param bin
	 *            magnitude spectrum (| |)^2 of fft
	 * @param cbin
	 *            mel filter coeffs
	 * @return mel filtered coeffs--> filter bank coefficients.
	 */
	private double[] melFilter(double bin[], int cbin[]) {
		double temp[] = new double[numMelFilters + 2];
		for (int k = 1; k <= numMelFilters; k++) {
			double num1 = 0.0, num2 = 0.0;
			for (int i = cbin[k - 1]; i <= cbin[k]; i++) {
				num1 += ((i - cbin[k - 1] + 1) / (cbin[k] - cbin[k - 1] + 1)) * bin[i];
			}
			for (int i = cbin[k] + 1; i <= cbin[k + 1]; i++) {
				num2 += (1 - ((i - cbin[k]) / (cbin[k + 1] - cbin[k] + 1))) * bin[i];
			}
			temp[k] = num1 + num2;
		}
		double fbank[] = new double[numMelFilters];
        System.arraycopy(temp, 1, fbank, 0, numMelFilters);
		return fbank;
	}

	/**
	 * performs nonlinear transformation
	 * @return f log of filter bac
	 */
	private double[] nonLinearTransformation(double fbank[]) {
		double f[] = new double[fbank.length];
		final double FLOOR = -50;
		for (int i = 0; i < fbank.length; i++) {
			f[i] = Math.log(fbank[i]);
			// check if ln() returns a value less than the floor
			if (f[i] < FLOOR) {
				f[i] = FLOOR;
			}
		}
		return f;
	}

	private double centerFreq(int i) {
		double melFLow, melFHigh;
		melFLow = freqToMel(lowerFilterFreq);
		melFHigh = freqToMel(upperFilterFreq);
		double temp = melFLow + ((melFHigh - melFLow) / (numMelFilters + 1)) * i;
		return inverseMel(temp);
	}

	private double inverseMel(double x) {
		double temp = Math.pow(10, x / 2595) - 1;
		return 700 * (temp);
	}

	protected double freqToMel(double freq) {
		return 2595 * log10(1 + freq / 700);
	}

	private double log10(double value) {
		return Math.log(value) / Math.log(10);
	}
}
