package GeDBIT.web;

import java.util.logging.Level;

import GeDBIT.app.BuildVPIndex;
import GeDBIT.index.algorithms.PartitionMethod;
import GeDBIT.index.algorithms.PartitionMethods;
import GeDBIT.index.algorithms.PivotSelectionMethod;
import GeDBIT.index.algorithms.PivotSelectionMethods;

import com.opensymphony.xwork2.ActionSupport;

public class BuildIndex extends ActionSupport {
	private static final long serialVersionUID = 1855192643060756269L;
	private String textfield; // 上传的文件
	private String data_type;
	private String build_dpm;
	private String build_f;
	private String build_frag;
	private String build_dim;
	private String build_v;
	private String build_m;
	private String build_psm;
	private String build_fft_scale;

	private int pNum = 2;
	private int sf = 3;
	private int mls = 100;
	private int initialSize = 1000;
	private int finalSize = 1000;
	private int stepSize = 1000;
	private Level debug = Level.OFF;
	private int pathLength = 0;
	private PartitionMethod dpm = PartitionMethods.valueOf("BALANCED");
	private PivotSelectionMethod psm = PivotSelectionMethods.valueOf("FFT");
	private int frag = 6;
	private int dim = 2;
	private boolean bucket = true;
	private double maxR = 0.02;

	private int setA = 10000;
	private int setN = 50;
	private int fftScale = 30;
	private String indexPrefix = "D:/data/index/"+"defaults";
	private String fileName = "1m.vector";
	private String dataType = "vector";
	private String psmName = "fft";
	private String forPrint = "";

	private String selectAlgorithm = "";
	private String testKind = "";
	private String yMethod = "";

	public String execute() throws Exception {
		fileName = textfield;
		if(data_type.equalsIgnoreCase("vector"))
		indexPrefix = "D:/data/index/" + data_type+ "-dim"+ build_dim + "-v" + build_v + "-f"
				+ build_f + "-m" + build_m + "-" + build_dpm + build_psm
				+ "-index";
		else
			indexPrefix = "D:/data/index/" + data_type+ "-frag"+ build_frag + "-v" + build_v + "-f"
					+ build_f + "-m" + build_m + "-" + build_dpm + build_psm
					+ "-index";

		dataType = data_type;
		if (!build_dim.equals(""))
			dim = Integer.parseInt(build_dim);
		frag = Integer.parseInt(build_frag);
		mls = Integer.parseInt(build_m);
		pNum = Integer.parseInt(build_v);
		sf = Integer.parseInt(build_f);
		if (build_dpm.equalsIgnoreCase("MVPT"))
			build_dpm = "CLUSTERINGKMEANS";
		dpm = PartitionMethods.valueOf(build_dpm.toUpperCase());
		psmName = build_psm;
		fftScale = Integer.valueOf(build_fft_scale);
		PartitionMethods.pm = build_dpm.toUpperCase();
		PartitionMethods.r = maxR;

		dpm.setMaxRadius(maxR);

		// hack, if cght, use clustering partition, and set maxr to -1 to denote
		// it
		if (dpm == PartitionMethods.CGHT) {
			dpm = PartitionMethods.CLUSTERINGKMEANS;
			dpm.setMaxRadius(-2);
		}
		if (dpm == PartitionMethods.GHT) {
			dpm = PartitionMethods.CLUSTERINGKMEANS;
			dpm.setMaxRadius(-1);
		}

		if (psmName.equalsIgnoreCase("incremental"))
			psm = new GeDBIT.index.algorithms.IncrementalSelection(setA, setN);
		else if (psmName.equalsIgnoreCase("pcaonfft"))
			psm = new GeDBIT.index.algorithms.PCAOnFFT(fftScale);
		else if (psmName.equalsIgnoreCase("selectiononfft"))
			psm = new GeDBIT.index.algorithms.SelectionOnFFT(fftScale,
					testKind, yMethod, selectAlgorithm);
		// psm = new GeDBIT.index.algorithms.SelectionOnFFT(fftScale);
		else if (psmName.equalsIgnoreCase("eigen"))
			psm = new GeDBIT.index.algorithms.EigenOnFFT(fftScale);
		else if (psmName.equalsIgnoreCase("gauss"))
			psm = new GeDBIT.index.algorithms.GaussOnFFT(fftScale);
		else
			psm = PivotSelectionMethods.valueOf(psmName.toUpperCase());

		BuildVPIndex.batchBulkLoad(fileName, indexPrefix, dataType, dim, frag,
				initialSize, finalSize, stepSize, mls, pNum, sf, debug,
				pathLength, psm, dpm, bucket, forPrint);
		System.out.println(indexPrefix);
		return "success";
	}

	public String getData_type() {
		return data_type;
	}

	public void setData_type(String data_type) {
		this.data_type = data_type;
	}

	public String getBuild_f() {
		return build_f;
	}

	public void setBuild_f(String build_f) {
		this.build_f = build_f;
	}

	public String getBuild_frag() {
		return build_frag;
	}

	public void setBuild_frag(String build_frag) {
		this.build_frag = build_frag;
	}

	public String getBuild_dim() {
		return build_dim;
	}

	public void setBuild_dim(String build_dim) {
		this.build_dim = build_dim;
	}

	public String getBuild_v() {
		return build_v;
	}

	public void setBuild_v(String build_v) {
		this.build_v = build_v;
	}

	public String getBuild_m() {
		return build_m;
	}

	public void setBuild_m(String build_m) {
		this.build_m = build_m;
	}

	public String getBuild_dpm() {
		return build_dpm;
	}

	public void setBuild_dpm(String build_dpm) {
		this.build_dpm = build_dpm;
	}

	public String getBuild_psm() {
		return build_psm;
	}

	public void setBuild_psm(String build_psm) {
		this.build_psm = build_psm;
	}

	public String getBuild_fft_scale() {
		return build_fft_scale;
	}

	public void setBuild_fft_scale(String build_fft_scale) {
		this.build_fft_scale = build_fft_scale;
	}

	public String getTextfield() {
		return textfield;
	}

	public void setTextfield(String textfield) {
		this.textfield = textfield;
	}

}
