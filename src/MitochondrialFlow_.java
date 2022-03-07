
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.RowFilter;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.measure.Calibration;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.ChannelSplitter;
import ij.plugin.PlugIn;
import ij.plugin.RoiEnlarger;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.MaximumFinder;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.filter.UnsharpMask;
import ij.plugin.frame.RoiManager;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import sc.fiji.analyzeSkeleton.AnalyzeSkeleton_;
import sc.fiji.analyzeSkeleton.Edge;
import sc.fiji.analyzeSkeleton.Graph;
import sc.fiji.analyzeSkeleton.Point;
import sc.fiji.analyzeSkeleton.SkeletonResult;
import sc.fiji.analyzeSkeleton.Vertex;

public class MitochondrialFlow_ implements PlugIn, Measurements {

	public String MITOCHONDRIALFLOW_IMAGES_PATH = "images_path";
	public String MITOCHONDRIALFLOW_SAVE_PATH = "save_path";
	public Preferences prefImages = Preferences.userRoot();
	public Preferences prefSave = Preferences.userRoot();
	static File[] listOfFiles;
	private JRadioButton csvFileB, excelFileB;
	static int l;
	static JTextArea taskOutput;
	public List<String> tableHeading1 = new ArrayList<String>();
	private RoiManager rm;
	private int sumNumberOfBranches, sumNumberOfJunctions, sumNumberOfEndPoints, sumNumberOfJunctionVoxels,
			sumNumberOfSlabs, sumNumberOfTriplePoints, sumNumberOfQuadruplePoints;
	private double sumAverageBranchLength, sumMaximumBranchLength;

	@Override
	public void run(String arg0) {
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			// If Nimbus is not available, you can set the GUI to another look and feel.
		}
		BatchModeGUI();

	}

	public void BatchModeGUI() {

		JFrame frameInitial = new JFrame("Mitochondrial-Flow:  Batch-Mode");
		JRadioButton batchAnalysisButton = new JRadioButton(" Batch-Mode for Mitochondrial-Flow Analysis");
		batchAnalysisButton.setSelected(true);
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(batchAnalysisButton);
		JPanel firstPanel = new JPanel();
		firstPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		firstPanel.add(batchAnalysisButton);
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
		buttonsPanel.add(Box.createVerticalStrut(8));
		buttonsPanel.add(Box.createHorizontalStrut(8));
		buttonsPanel.add(firstPanel);
		TextField textImages = (TextField) new TextField(20);
		TextField textSave = (TextField) new TextField(20);
		textImages.setText(prefImages.get(MITOCHONDRIALFLOW_IMAGES_PATH, ""));
		textSave.setText(prefSave.get(MITOCHONDRIALFLOW_SAVE_PATH, ""));
		ImageIcon iconImages = createImageIcon("images/browse.png");
		JButton buttonImages = new JButton("");
		JButton buttonSave = new JButton("");
		Icon iconImagesCell = new ImageIcon(iconImages.getImage().getScaledInstance(20, 22, Image.SCALE_SMOOTH));
		buttonImages.setIcon(iconImagesCell);
		buttonSave.setIcon(iconImagesCell);
		DirectoryListener_ listenerImages = new DirectoryListener_("Browse for images to analyze", textImages,
				JFileChooser.FILES_AND_DIRECTORIES);
		DirectoryListener_ listenerSave = new DirectoryListener_("Browse for images to analyze", textSave,
				JFileChooser.FILES_AND_DIRECTORIES);
		buttonImages.addActionListener(listenerImages);
		buttonSave.addActionListener(listenerSave);
		JPanel panelImages = new JPanel();
		JPanel panelSave = new JPanel();
		JPanel panelExport = new JPanel();
		panelImages.setLayout(new FlowLayout(FlowLayout.LEFT));
		panelSave.setLayout(new FlowLayout(FlowLayout.LEFT));
		panelExport.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel directImages = new JLabel("     ⊳   Directoy where Images to be analyzed are :   ");
		JLabel directSave = new JLabel("     ⊳   Directoy to Export Results :   ");
		JLabel fileToExport = new JLabel(" File Format to Export Results :   ");
		directImages.setFont(new Font("Helvetica", Font.BOLD, 12));
		directSave.setFont(new Font("Helvetica", Font.BOLD, 12));
		fileToExport.setFont(new Font("Helvetica", Font.BOLD, 12));
		panelImages.add(directImages);
		panelImages.add(textImages);
		panelImages.add(buttonImages);
		panelExport.add(fileToExport);
		panelSave.add(directSave);
		panelSave.add(textSave);
		panelSave.add(buttonSave);
		JButton okButton = new JButton("");
		ImageIcon iconOk = createImageIcon("images/ok.png");
		Icon okCell = new ImageIcon(iconOk.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
		okButton.setIcon(okCell);
		okButton.setToolTipText("Click this button to get Batch-Mode Analysis.");
		JButton cancelButton = new JButton("");
		ImageIcon iconCancel = createImageIcon("images/cancel.png");
		Icon cancelCell = new ImageIcon(iconCancel.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
		cancelButton.setIcon(cancelCell);
		cancelButton.setToolTipText("Click this button to cancel Batch-Mode Analysis.");
		JPanel panelOptions = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel panelBox = new JPanel();
		panelBox.setLayout(new BoxLayout(panelBox, BoxLayout.Y_AXIS));
		csvFileB = new JRadioButton(".CSV file", true);
		excelFileB = new JRadioButton("EXCEL file");
		ButtonGroup bgroup = new ButtonGroup();
		bgroup.add(csvFileB);
		bgroup.add(excelFileB);
		panelBox.add(panelExport);
		panelBox.add(excelFileB);
		panelBox.add(csvFileB);
		panelOptions.add(panelBox);
		JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
		Dimension dime = separator.getPreferredSize();
		dime.height = panelBox.getPreferredSize().height * 3;
		separator.setPreferredSize(dime);
		panelOptions.add(separator);
		JCheckBox unSharpCheck = new JCheckBox("   UnSharp Mask :   ");
		unSharpCheck.setSelected(true);
		JLabel unSharpRadius = new JLabel("            Radius(σ) in pixels :   ");
		JSpinner unSharpSpinner = new JSpinner(new SpinnerNumberModel(2.0, 0.0, 500.0, 0.5));
		unSharpSpinner.setPreferredSize(new Dimension(60, 20));
		JCheckBox enhanceCheck = new JCheckBox("   Enhance Local Contrast(CLAHE) :   ");
		enhanceCheck.setSelected(true);
		JLabel enhanceBlockSize = new JLabel("            BlockSize :   ");
		JCheckBox medianCheck = new JCheckBox("   Median Filter :   ");
		JLabel enhanceHistogram = new JLabel("   Histogram Bins :   ");
		JSpinner medianSpinner = new JSpinner(new SpinnerNumberModel(2.0, 0.0, 500.0, 0.5));
		medianSpinner.setPreferredSize(new Dimension(60, 20));
		JSpinner blockSpinner = new JSpinner(new SpinnerNumberModel(9, 0, 5000, 1));
		blockSpinner.setPreferredSize(new Dimension(60, 20));
		JSpinner histogramSpinner = new JSpinner(new SpinnerNumberModel(4, 0, 5000, 1));
		histogramSpinner.setPreferredSize(new Dimension(60, 20));
		JLabel radiusMedian = new JLabel("            Radius:   ");
		JPanel unSharpRadiusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		unSharpRadiusPanel.add(unSharpRadius);
		unSharpRadiusPanel.add(unSharpSpinner);
		JPanel unSharpPanelCheck = new JPanel(new FlowLayout(FlowLayout.LEFT));
		unSharpPanelCheck.add(unSharpCheck);
		JPanel unSharpPanel = new JPanel();
		unSharpPanel.setLayout(new BoxLayout(unSharpPanel, BoxLayout.Y_AXIS));
		unSharpPanel.add(unSharpPanelCheck);
		unSharpPanel.add(unSharpRadiusPanel);
		JPanel enhancePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		enhancePanel.add(enhanceCheck);
		JPanel enhanceSubPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		enhanceSubPanel.add(enhanceBlockSize);
		enhanceSubPanel.add(blockSpinner);
		enhanceSubPanel.add(enhanceHistogram);
		enhanceSubPanel.add(histogramSpinner);
		JPanel enhancePanelFinal = new JPanel();
		enhancePanelFinal.setLayout(new BoxLayout(enhancePanelFinal, BoxLayout.Y_AXIS));
		enhancePanelFinal.add(enhancePanel);
		enhancePanelFinal.add(enhanceSubPanel);
		JPanel medianPanel = new JPanel();
		medianPanel.setLayout(new BoxLayout(medianPanel, BoxLayout.Y_AXIS));
		JPanel medianPanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		medianPanel1.add(medianCheck);
		JPanel medianPanelInitial = new JPanel(new FlowLayout(FlowLayout.LEFT));
		medianPanelInitial.add(radiusMedian);
		medianPanelInitial.add(medianSpinner);
		medianPanel.add(medianPanel1);
		medianPanel.add(medianPanelInitial);
		JPanel secondPanel = new JPanel();
		secondPanel.setLayout(new BoxLayout(secondPanel, BoxLayout.Y_AXIS));
		secondPanel.add(unSharpPanel);
		secondPanel.add(enhancePanelFinal);
		secondPanel.add(medianPanel);
		panelOptions.add(secondPanel);
		radiusMedian.setEnabled(false);
		medianSpinner.setEnabled(false);
		JPanel mainPanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		mainPanel1.add(panelOptions);
		JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
		panelButtons.add(okButton);
		panelButtons.add(cancelButton);
		buttonsPanel.add(Box.createVerticalStrut(5));
		buttonsPanel.add(Box.createVerticalStrut(5));
		buttonsPanel.add(panelImages);
		buttonsPanel.add(Box.createVerticalStrut(5));
		buttonsPanel.add(panelSave);
		buttonsPanel.add(mainPanel1);
		buttonsPanel.add(Box.createVerticalStrut(5));
		buttonsPanel.add(panelButtons);

		frameInitial.setSize(725, 450);
		frameInitial.add(buttonsPanel);
		frameInitial.setLocationRelativeTo(null);
		frameInitial.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frameInitial.setVisible(true);

		medianCheck.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					radiusMedian.setEnabled(true);
					medianSpinner.setEnabled(true);
				}
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					radiusMedian.setEnabled(false);
					medianSpinner.setEnabled(false);
				}
			}
		});
		unSharpCheck.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					unSharpRadius.setEnabled(true);
					unSharpSpinner.setEnabled(true);
				}
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					unSharpRadius.setEnabled(false);
					unSharpSpinner.setEnabled(false);
				}
			}
		});
		enhanceCheck.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					enhanceBlockSize.setEnabled(true);
					blockSpinner.setEnabled(true);
					enhanceHistogram.setEnabled(true);
					histogramSpinner.setEnabled(true);
				}
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					enhanceBlockSize.setEnabled(false);
					blockSpinner.setEnabled(false);
					enhanceHistogram.setEnabled(false);
					histogramSpinner.setEnabled(false);
				}
			}
		});

		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				frameInitial.dispatchEvent(new WindowEvent(frameInitial, WindowEvent.WINDOW_CLOSING));
				Thread mainProcess = new Thread(new Runnable() {

					public void run() {
						// ProgressBarDemo frame = new ProgressBarDemo();
						// frame.createAndShowGUI();
						File imageFolder = new File(textImages.getText());
						listOfFiles = imageFolder.listFiles();
						final String[] imageTitles = new String[listOfFiles.length];
						final int MAX = listOfFiles.length;
						final JFrame frame = new JFrame("Analyzing...");

						// creates progress bar
						final JProgressBar pb = new JProgressBar();
						pb.setMinimum(0);
						pb.setMaximum(MAX);
						pb.setStringPainted(true);
						taskOutput = new JTextArea(5, 20);
						taskOutput.setMargin(new Insets(5, 5, 5, 5));
						taskOutput.setEditable(false);
						JPanel panel = new JPanel();
						panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
						panel.add(pb);
						panel.add(Box.createVerticalStrut(5));
						panel.add(new JScrollPane(taskOutput), BorderLayout.CENTER);
						panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

						frame.getContentPane().add(panel);
						frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
						frame.setSize(400, 220);
						frame.setVisible(true);

						prefImages.put(MITOCHONDRIALFLOW_IMAGES_PATH, textImages.getText());
						prefSave.put(MITOCHONDRIALFLOW_SAVE_PATH, textSave.getText());

						for (int i = 0; i < listOfFiles.length; i++) {
							// update progressbar
							if (listOfFiles[i].isFile())
								imageTitles[i] = listOfFiles[i].getName();

							if (imageTitles[i].contains("._") == Boolean.TRUE)
								imageTitles[i].replaceFirst("._", "");

							ImagePlus imp = new ImagePlus(textImages.getText() + File.separator + imageTitles[i]);

							final int currentValue = i + 1;
							try {
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										pb.setValue(currentValue);
										taskOutput.append(
												String.format("Processing Image - %s%%%% -Completed : %f of task.\n",
														imageTitles[currentValue - 1],
														(double) (currentValue * 100.0) / listOfFiles.length));

									}
								});
								java.lang.Thread.sleep(100);
							} catch (InterruptedException e) {
								JOptionPane.showMessageDialog(frame, e.getMessage());
							}
							if (unSharpCheck.isSelected() == Boolean.TRUE) {
								UnsharpMask um = new UnsharpMask();
								ImageProcessor ip = imp.getProcessor();
								FloatProcessor fp = null;
								for (int j = 0; j < ip.getNChannels(); j++) {
									fp = ip.toFloat(j, fp);
									fp.snapshot();
									um.sharpenFloat(fp, (double) unSharpSpinner.getValue(), (float) 0.6);
									ip.setPixels(i, fp);
								}
								imp = new ImagePlus(imageTitles[i], ip);
								// IJ.run(imp, "Unsharp Mask...",
								// String.format("radius=%d mask=0.60", (int) unSharpSpinner.getValue()));
							}
							if (enhanceCheck.isSelected() == Boolean.TRUE)
								mpicbg.ij.clahe.Flat.getInstance().run(imp, (int) blockSpinner.getValue() - 1 / 2,
										(int) histogramSpinner.getValue() - 1, 3,
										(ByteProcessor) imp.getProcessor().convertToByte(true), false);

							if (medianCheck.isSelected() == Boolean.TRUE)
								IJ.run(imp, "Median...", String.format("radius=%f", (double) medianSpinner.getValue()));
							Prefs.blackBackground = true;
							IJ.run(imp, "Convert to Mask", "");
							ImagePlus impBinarized = imp.duplicate();
							IJ.run(imp, "Skeletonize", "");
							ImagePlus impSkeleton = imp.duplicate();
							ResultsTable rtAnalyzer = new ResultsTable();
							ParticleAnalyzer pa = new ParticleAnalyzer(ParticleAnalyzer.ADD_TO_MANAGER,
									Analyzer.ALL_STATS, rtAnalyzer, 0.0, 5555555555555.0);
							pa.setResultsTable(rtAnalyzer);
							pa.analyze(impBinarized, impBinarized.getProcessor());
							AnalyzeSkeleton_ skel = new AnalyzeSkeleton_();
							skel.setup("", imp);
							SkeletonResult skelResult = null;
							skelResult = skel.run(AnalyzeSkeleton_.SHORTEST_BRANCH, false, false, null, true, false);

							// Read the results
							ResultsTable rt = new ResultsTable();
							String[] head = { "Skeleton", "# Branches", "# Junctions", "# End-point voxels",
									"# Junction voxels", "# Slab voxels", "Average Branch Length", "# Triple points",
									"# Quadruple points", "Maximum Branch Length", "Longest Shortest Path", "spx",
									"spy", "spz" };
							String[] headAnalyzer = rtAnalyzer.getHeadings();
							for (int x = 1; x < headAnalyzer.length; x++)
								head = append(head, headAnalyzer[x]);

							List<List<String>> dataListAnalyzer = new ArrayList<List<String>>();
							for (int r = 0; r < rtAnalyzer.size(); r++) {
								List<String> stringsAnalyzer = new ArrayList<String>();
								for (int c = 0; c < headAnalyzer.length; c++) {
									String valuesAnalyzer = rtAnalyzer.getStringValue(headAnalyzer[c], r);
									stringsAnalyzer.add(valuesAnalyzer);

								}
								dataListAnalyzer.add(stringsAnalyzer);
							}

							rm = RoiManager.getRoiManager();
							Roi[] roisTotal = rm.getRoisAsArray();
							int numOfTrees = skelResult.getNumOfTrees();
							int[] numberOfBranches = skelResult.getBranches();
							int[] numberOfJunctions = skelResult.getJunctions();
							int[] numberOfEndPoints = skelResult.getEndPoints();
							int[] numberOfJunctionVoxels = skelResult.getJunctionVoxels();
							int[] numberOfSlabs = skelResult.getSlabs();
							double[] averageBranchLength = skelResult.getAverageBranchLength();
							int[] numberOfTriplePoints = skelResult.getTriples();
							int[] numberOfQuadruplePoints = skelResult.getQuadruples();
							double[] maximumBranchLength = skelResult.getMaximumBranchLength();
							ArrayList<Double> shortestPathList = skelResult.getShortestPathList();
							double[][] spStartPosition = skelResult.getSpStartPosition();
							Graph[] graph = skelResult.getGraph();
							ArrayList<Vertex> vertex = new ArrayList<Vertex>();
							ArrayList<Integer> xCoordinate = new ArrayList<Integer>();
							ArrayList<Integer> yCoordinate = new ArrayList<Integer>();
							for (int k = 0; k < graph.length; k++)
								vertex.add(graph[k].getRoot());

							for (int j = 0; j < numOfTrees; j++) {
								rt.incrementCounter();

								rt.addValue(head[1], numberOfBranches[j]);
								rt.addValue(head[2], numberOfJunctions[j]);
								rt.addValue(head[3], numberOfEndPoints[j]);
								rt.addValue(head[4], numberOfJunctionVoxels[j]);
								rt.addValue(head[5], numberOfSlabs[j]);
								rt.addValue(head[6], averageBranchLength[j]);
								rt.addValue(head[7], numberOfTriplePoints[j]);
								rt.addValue(head[8], numberOfQuadruplePoints[j]);
								rt.addValue(head[9], maximumBranchLength[j]);
								if (null != shortestPathList) {
									rt.addValue(head[10], shortestPathList.get(j));
									rt.addValue(head[11], spStartPosition[j][0]);
									rt.addValue(head[12], spStartPosition[j][1]);
									rt.addValue(head[13], spStartPosition[j][2]);
								}
								
							}
							for (Vertex vert : vertex) {
								// counter++;
									//IJ.log(vert.getPoints().get(0).x + "-------" + (imp.getHeight() - vert.getPoints().get(0).y - 1));
									xCoordinate.add(vert.getPoints().get(0).x);
									yCoordinate.add(vert.getPoints().get(0).y);

							}
							sumNumberOfBranches = 0;
							sumNumberOfJunctions = 0;
							sumNumberOfEndPoints = 0;
							sumNumberOfJunctionVoxels = 0;
							sumNumberOfSlabs = 0;
							sumAverageBranchLength = 0;
							sumNumberOfTriplePoints = 0;
							sumNumberOfQuadruplePoints = 0;
							sumMaximumBranchLength = 0;

							for (int x = 0; x < numberOfBranches.length; x++)
								sumNumberOfBranches = sumNumberOfBranches + numberOfBranches[x];
							double averageNumberOfBranches = sumNumberOfBranches / numberOfBranches.length;
							for (int x = 0; x < numberOfJunctions.length; x++)
								sumNumberOfJunctions = sumNumberOfJunctions + numberOfJunctions[x];
							double averageNumberOfJunctions = sumNumberOfJunctions / numberOfJunctions.length;
							for (int x = 0; x < numberOfEndPoints.length; x++)
								sumNumberOfEndPoints = sumNumberOfEndPoints + numberOfEndPoints[x];
							double averageNumberOfEndPoints = sumNumberOfEndPoints / numberOfEndPoints.length;
							for (int x = 0; x < numberOfJunctionVoxels.length; x++)
								sumNumberOfJunctionVoxels = sumNumberOfJunctionVoxels + numberOfJunctionVoxels[x];
							double averageNumberOfJunctionVoxels = sumNumberOfJunctionVoxels
									/ numberOfJunctionVoxels.length;
							for (int x = 0; x < numberOfSlabs.length; x++)
								sumNumberOfSlabs = sumNumberOfSlabs + numberOfSlabs[x];
							double averageNumberOfSlabs = sumNumberOfSlabs / numberOfSlabs.length;
							for (int x = 0; x < averageBranchLength.length; x++)
								sumAverageBranchLength = sumAverageBranchLength + averageBranchLength[x];
							double averageAverageBranchLength = sumAverageBranchLength / averageBranchLength.length;
							for (int x = 0; x < numberOfTriplePoints.length; x++)
								sumNumberOfTriplePoints = sumNumberOfTriplePoints + numberOfTriplePoints[x];
							double averageNumberOfTriplePoints = sumNumberOfTriplePoints / numberOfTriplePoints.length;
							for (int x = 0; x < numberOfQuadruplePoints.length; x++)
								sumNumberOfQuadruplePoints = sumNumberOfQuadruplePoints + numberOfQuadruplePoints[x];
							double averageNumberOfQuadruplePoints = sumNumberOfQuadruplePoints
									/ numberOfQuadruplePoints.length;
							for (int x = 0; x < maximumBranchLength.length; x++)
								sumMaximumBranchLength = sumMaximumBranchLength + maximumBranchLength[x];
							double averageMaximumBranchLength = sumMaximumBranchLength / maximumBranchLength.length;

							rt.addValue(head[1], averageNumberOfBranches);
							rt.addValue(head[2], averageNumberOfJunctions);
							rt.addValue(head[3], averageNumberOfEndPoints);
							rt.addValue(head[4], averageNumberOfJunctionVoxels);
							rt.addValue(head[5], averageNumberOfSlabs);
							rt.addValue(head[6], averageAverageBranchLength);
							rt.addValue(head[7], averageNumberOfTriplePoints);
							rt.addValue(head[8], averageNumberOfQuadruplePoints);
							rt.addValue(head[9], averageMaximumBranchLength);

							// if (0 == i % 100)
							// rt.show("Results");
							// for (int x = 0; x < pointXY.length; x++) {
							// int counter = 0;
							// for (int j = 0; j < numOfTrees; j++) {

							// }
							// F double sumCol = 0;

							double[] dataListSum = new double[dataListAnalyzer.get(0).size()];
							double[] dataListAver = new double[dataListAnalyzer.get(0).size()];
							for (int x = 0; x < dataListAnalyzer.size(); x++) {
								for (int y = 0; y < dataListAnalyzer.get(x).size(); y++) {
									dataListSum[y] += Double.parseDouble(dataListAnalyzer.get(x).get(y));
									dataListAver[y] = dataListSum[y] / dataListAnalyzer.size();
								}

							}

							for (int j = 0; j < numOfTrees; j++)
									for (int c = 0; c < dataListAnalyzer.size(); c++) {
										for (int u = 0; u < dataListAnalyzer.get(j).size(); u++) {
											if (roisTotal[c].contains(xCoordinate.get(j), yCoordinate.get(j)) == true)
												rt.setValue(headAnalyzer[u], j, dataListAnalyzer.get(c).get(u));

											rt.addValue(headAnalyzer[u], dataListAver[u]);
										}

									}

							// }

							rt.showRowNumbers(true);
							// rt.show("Results-" + i);

							final ResultsTable extra_rt = new ResultsTable();

							final String[] extra_head = { "Branch", "Skeleton ID", "Branch length", "V1 x", "V1 y",
									"V1 z", "V2 x", "V2 y", "V2 z", "Euclidean distance", "running average length",
									"average intensity (inner 3rd)", "average intensity" };

							// Edge comparator (by branch length)
							Comparator<Edge> comp = new Comparator<Edge>() {
								public int compare(Edge o1, Edge o2) {
									final double diff = o1.getLength() - o2.getLength();
									if (diff < 0)
										return 1;
									else if (diff == 0)
										return 0;
									else
										return -1;
								}

								public boolean equals(Object o) {
									return false;
								}
							};
							// Display branch information for each tree
							for (int j = 0; j < numOfTrees; j++) {
								final ArrayList<Edge> listEdges = graph[j].getEdges();
								// Sort branches by length
								Collections.sort(listEdges, comp);
								for (final Edge e : listEdges) {
									extra_rt.incrementCounter();

									extra_rt.addValue(extra_head[1], j + 1);
									extra_rt.addValue(extra_head[2], e.getLength());
									extra_rt.addValue(extra_head[3],
											e.getV1().getPoints().get(0).x * imp.getCalibration().pixelWidth);
									extra_rt.addValue(extra_head[4],
											e.getV1().getPoints().get(0).y * imp.getCalibration().pixelHeight);
									extra_rt.addValue(extra_head[5],
											e.getV1().getPoints().get(0).z * imp.getCalibration().pixelDepth);
									extra_rt.addValue(extra_head[6],
											e.getV2().getPoints().get(0).x * imp.getCalibration().pixelWidth);
									extra_rt.addValue(extra_head[7],
											e.getV2().getPoints().get(0).y * imp.getCalibration().pixelHeight);
									extra_rt.addValue(extra_head[8],
											e.getV2().getPoints().get(0).z * imp.getCalibration().pixelDepth);
									extra_rt.addValue(extra_head[9], calculateDistance(e.getV1().getPoints().get(0),
											e.getV2().getPoints().get(0), imp));
									extra_rt.addValue(extra_head[10], e.getLength_ra());
									extra_rt.addValue(extra_head[11], e.getColor3rd());
									extra_rt.addValue(extra_head[12], e.getColor());
								}

							}
							// extra_rt.show("Branch information-" + i);
							rm.reset();

							File directFolder = new File(textSave.getText() + File.separator + imp.getShortTitle());

							if (!directFolder.exists()) {

								boolean result = false;

								try {
									directFolder.mkdir();
									result = true;
								} catch (SecurityException se) {
									// handle it
								}

							}
							IJ.saveAs(impBinarized, "Tiff", directFolder + File.separator + imp.getShortTitle() + "-Binary.tif");
							IJ.saveAs(impSkeleton, "Tiff",
									directFolder + File.separator + imp.getShortTitle() + "-Skeletonize.tif");

							if (excelFileB.isSelected() == Boolean.TRUE) {

								try {
									rt.saveAs(directFolder + File.separator + "AnalyzeSkeleton_rt" + ".xls");
									rtAnalyzer.saveAs(directFolder + File.separator + "AnalysisParticles_rt" + ".xls");
									extra_rt.saveAs(directFolder + File.separator + "BranchInformation_rt" + ".xls");
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							}

							if (csvFileB.isSelected() == Boolean.TRUE) {
								try {
									rt.saveAs(directFolder + File.separator + "AnalyzeSkeleton_rt" + ".csv");
									rtAnalyzer.saveAs(directFolder + File.separator + "AnalysisParticles_rt" + ".csv");
									extra_rt.saveAs(directFolder + File.separator + "BranchInformation_rt" + ".csv");
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							}
						}

						taskOutput.append("Done!");
						rm.close();
						frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));

					}
				});
				mainProcess.start();

			}
		});

		cancelButton.addActionListener(new java.awt.event.ActionListener() {

			public void actionPerformed(java.awt.event.ActionEvent evt) {
				frameInitial.dispatchEvent(new WindowEvent(frameInitial, WindowEvent.WINDOW_CLOSING));
			}
		});

	}

	public static ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = MitochondrialFlow_.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			return null;
		}
	}

	private double calculateDistance(Point point1, Point point2, ImagePlus imp) {
		return Math.sqrt(Math.pow((point1.x - point2.x) * imp.getCalibration().pixelWidth, 2)
				+ Math.pow((point1.y - point2.y) * imp.getCalibration().pixelHeight, 2)
				+ Math.pow((point1.z - point2.z) * imp.getCalibration().pixelDepth, 2));
	}

	static <T> T[] append(T[] arr, T element) {
		final int N = arr.length;
		arr = Arrays.copyOf(arr, N + 1);
		arr[N] = element;
		return arr;
	}

}
