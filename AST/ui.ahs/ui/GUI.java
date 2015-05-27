package ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;






import org.apache.commons.io.FileUtils;
import org.terrier.applications.desktop.DesktopTerrier;

import app.Algo1;
import app.Algo3Stemmer;
import arlightstemmerlucene.ArLightStemmerLucene;

/** TO DO
 *  Remove duplicate word to reduce size of the output dataset but in the algorithm not the GUI - done
 *  
 */

/**
 * @author mhmd yahya almusaddar
 * @author mhmd@alaqsa.edu.ps
 * @version 0.8 - 8th version 20/1/2014
 * @version 0.9 - 9th version 30/1/2014
 * @version 1.0 - 10th version 1/2/2014
 */

public class GUI {

	public static String ARABIZED_PATH = "resources/arabized-words/arabized-words-normalized.txt";
	public static JButton btn1;
	public static JButton btn10;
	public static JButton btn11, btn13, btn12, btn14, btn15;
	public static JButton btn2;
	public static JButton btn3;
	public static JButton btn4;
	public static JButton btn5;
	public static JButton btn6;
	public static JButton btn7;
	public static JButton btn8;
	public static JButton btn9;
	public static JButton btnDesktopTerrier, btnViewOwnDataset_larkey,
			btnViewOwnDataset_khoja, btnImportProcessedDataset,
			btnExportProcessedDataset, btnClearLastIndex;
	@SuppressWarnings("rawtypes")
	static JComboBox cb1;
	static JCheckBox chckbx3;
	static JCheckBox chckbx4;
	static JCheckBox chckbx5;
	static JCheckBox chckbx6;
	public static String DATASET_PATH = "resources/dataset/test-dataset.txt";
	public static ArrayList<String> datasetList = null;
	public static ArrayList<String> datasetList_copy;
	public static StringBuilder processedDataset;
	public static StyledDocument doc;
	public static final String ENCODING = "UTF8";
	static JEditorPane ep1, editorPaneArabizedWords, editorPane_OwnDataset;
	static JEditorPane ep2;
	public static ArrayList<String> freeDataSetList = null;
	public static JFrame frmShowResults;
	static handler handle;
	static int index;
	public static String normalizedDataset;
	public static File output_selection_Type, input_selection_Type;

	public static String OUTPUT_PATH = "output/";
	static String[] output_str_array;
	public static JProgressBar progressBar;
	public static JRadioButton rdbtnLightStopwordList,
			rdbtnIntensiveStopwordList, rdbtnTestDataset, rdbtnOsac, rdbtnBbc,
			rdbtnCnn, rdbtnAlgorithm1, rdbtnAlgorithm2, rdbtnAlgorithm3;
	static Reader readerArabizedWords;
	public static String STOP_PATH = "resources/stop-words/intensive-stop-words.txt";
	public static String STOP_PATH_OWN = "resources/stop-words/intensive-stop-words.txt";
	public static ArrayList<String> stopsetList = null;
	public static JTable table1, table2;
	public static JTextPane tp1;
	private final ButtonGroup buttonGroupDataset = new ButtonGroup();
	private final ButtonGroup buttonGroupStopWord = new ButtonGroup();
	JFrame frmStandardArabicStemmer;
	JLabel lblStatus;
	JMenuBar menuBar;
	JToolBar toolBar;
	JMenu mnNewMenu1, mnNewMenu2, mnNewMenu3;
	JMenuItem mntmNew, mntmSave, mntmExit, mntmCopy, mntmPaste, mntmDocs,
			mntmAbout;
	JPanel p1, p2, p3, p4, p5, p6, p8, p10;
	private JPanel panel_2;
	private JPanel panel_5;
	private JScrollPane scrollPane, scrollPane3;
	private JScrollPane scrollPane_2;
	private JScrollPane scrollPane1, scrollPane2;
	public static JTabbedPane tabbedPane;
	public static JTabbedPane tabbedPane_1;
	public static JTabbedPane tabbedPane_2;
	private final ButtonGroup buttonGroup = new ButtonGroup();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					GUI window = new GUI();
					window.frmStandardArabicStemmer.setVisible(true);

					// load Arabized words
					try {
						readerArabizedWords = new FileReader(new File(
								GUI.ARABIZED_PATH));
						GUI.editorPaneArabizedWords.read(readerArabizedWords,
								"");
					} catch (Exception exp) {
						exp.printStackTrace();
					}

					cb1.addActionListener(handle);
					btn1.addActionListener(handle);
					btn12.addActionListener(handle);
					btn3.addActionListener(handle);
					btn2.addActionListener(handle);
					btn4.addActionListener(handle);
					btn5.addActionListener(handle);
					btn14.addActionListener(handle);
					btn8.addActionListener(handle);
					btn7.addActionListener(handle);
					btn15.addActionListener(handle);
					btn6.addActionListener(handle);
					btn11.addActionListener(handle);
					btn13.addActionListener(handle);
					rdbtnLightStopwordList.addActionListener(handle);
					rdbtnIntensiveStopwordList.addActionListener(handle);
					rdbtnTestDataset.addActionListener(handle);
					rdbtnOsac.addActionListener(handle);
					rdbtnBbc.addActionListener(handle);
					rdbtnCnn.addActionListener(handle);
					btnDesktopTerrier.addActionListener(handle);
					btnViewOwnDataset_khoja.addActionListener(handle);
					btnViewOwnDataset_larkey.addActionListener(handle);
					btnClearLastIndex.addActionListener(handle);
					btnImportProcessedDataset.addActionListener(handle);
					btnExportProcessedDataset.addActionListener(handle);
					rdbtnAlgorithm1.addActionListener(handle);
					rdbtnAlgorithm2.addActionListener(handle);
					rdbtnAlgorithm3.addActionListener(handle);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

	/**
	 * export method
	 */
	public static void export(JTable table, String fileName) throws Exception {
		BufferedWriter bfw = new BufferedWriter(new FileWriter(OUTPUT_PATH
				+ fileName));
		for (int i = 0; i < table.getColumnCount(); i++) {
			bfw.write(table.getColumnName(i));
			bfw.write("\t");
		}

		for (int i = 0; i < table.getRowCount(); i++) {
			bfw.newLine();
			for (int j = 0; j < table.getColumnCount(); j++) {
				if (table.getValueAt(i, j) == null) {
					bfw.write("\t");
					;
				} else {
					bfw.write((String) (table.getValueAt(i, j)));
					bfw.write("\t");
					;
				}
			}
		}
		bfw.close();
	}

	/**
	 * Global Pop-up message
	 */
	public static void infoBox(String infoMessage, String location) {
		JOptionPane.showMessageDialog(null, infoMessage,
				"InfoBox: " + location, JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Remove duplicates
	 */
	public static int removeDuplicates(String[] words) {

		// from string array to Array list
		List<String> stringList = Arrays.asList(words);

		int size = stringList.size();
		int duplicates = 0;

		// not using a method in the check also speeds up the execution
		// also i must be less that size-1 so that j doesn't
		// throw IndexOutOfBoundsException
		for (int i = 0; i < size - 1; i++) {
			// start from the next item after strings[i]
			// since the ones before are checked
			for (int j = i + 1; j < size; j++) {
				// no need for if ( i == j ) here
				if (!stringList.get(j).equals(stringList.get(i)))
					continue;
				duplicates++;
				stringList.remove(j);
				// decrease j because the array got re-indexed
				j--;
				// decrease the size of the array
				size--;
			} // for j
		} // for i

		return duplicates;

	}

	/**
	 * Create the application.
	 */
	public GUI() {

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException ex) {
		}
		initialize();
		GUI.tp1.setText("Start of Algorithm");
		doc = GUI.tp1.getStyledDocument();
		handle = new handler();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void initialize() {

		frmStandardArabicStemmer = new JFrame();
		frmStandardArabicStemmer.setResizable(false);
		frmStandardArabicStemmer.setTitle("Arabic Stemming Toolkit");
		frmStandardArabicStemmer.setBounds(100, 100, 834, 789);
		frmStandardArabicStemmer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmStandardArabicStemmer.getContentPane().setLayout(null);

		// Results GUI Start
		frmShowResults = new JFrame();
		scrollPane3 = new JScrollPane();
		table2 = new JTable();
		p10 = new JPanel();

		// frmShowResults.setResizable(false);
		frmShowResults.setTitle("Results");
		frmShowResults.setBounds(100, 100, 1007, 731);
		// frmShowResults.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmShowResults.getContentPane().setLayout(new BorderLayout(0, 0));

		p10.setLayout(new BorderLayout(0, 0));
		p10.add(scrollPane3);

		scrollPane3.setViewportView(table2);

		table2.setCellSelectionEnabled(true);
		table2.setColumnSelectionAllowed(true);
		table2.setFillsViewportHeight(true);
		table2.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		table2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table2.setModel(new DefaultTableModel(new Object[5000][4],
				new String[] { "Proposed Algorithm", "Larkey", "Khoja",
						"Duplicates Summarization" }) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			@SuppressWarnings("rawtypes")
			Class[] columnTypes = new Class[] { String.class, String.class,
					String.class, String.class };

			public Class<?> getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
		});

		table2.getColumnModel().getColumn(0).setPreferredWidth(113);
		table2.getColumnModel().getColumn(1).setPreferredWidth(95);
		table2.getColumnModel().getColumn(2).setPreferredWidth(97);

		frmShowResults.getContentPane().add(p10);

		panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(UIManager
				.getBorder("TitledBorder.border"), "Toolkit Options",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_2.setBounds(16, 22, 805, 668);
		frmStandardArabicStemmer.getContentPane().add(panel_2);
		panel_2.setLayout(null);

		tabbedPane = new JTabbedPane(SwingConstants.TOP);
		tabbedPane.setBounds(6, 16, 789, 100);
		panel_2.add(tabbedPane);
		tabbedPane.setToolTipText("");

		p5 = new JPanel();
		tabbedPane.addTab("Dataset", null, p5, null);
		p5.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		rdbtnTestDataset = new JRadioButton("Test Dataset");
		buttonGroupDataset.add(rdbtnTestDataset);
		p5.add(rdbtnTestDataset);

		rdbtnOsac = new JRadioButton("OSAC");
		buttonGroupDataset.add(rdbtnOsac);
		p5.add(rdbtnOsac);

		rdbtnBbc = new JRadioButton("BBC");
		buttonGroupDataset.add(rdbtnBbc);
		p5.add(rdbtnBbc);

		rdbtnCnn = new JRadioButton("CNN");
		buttonGroupDataset.add(rdbtnCnn);
		p5.add(rdbtnCnn);

		btn1 = new JButton("View Dataset");
		p5.add(btn1);

		btn12 = new JButton("Load Dataset");
		p5.add(btn12);

		JPanel panel_4 = new JPanel();
		tabbedPane.addTab("Stop-words", null, panel_4, null);

		rdbtnIntensiveStopwordList = new JRadioButton(
				"Intensive Stop-word List");
		buttonGroupStopWord.add(rdbtnIntensiveStopwordList);
		panel_4.add(rdbtnIntensiveStopwordList);

		rdbtnLightStopwordList = new JRadioButton("Light Stop-word List");
		buttonGroupStopWord.add(rdbtnLightStopwordList);
		panel_4.add(rdbtnLightStopwordList);

		btn11 = new JButton("View Stop-word list");
		panel_4.add(btn11);

		btn13 = new JButton("Load Stop-word list");
		panel_4.add(btn13);

		// Results GUI End

		p1 = new JPanel();
		tabbedPane.addTab("Proposed Algorithm", null, p1, null);
		p1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		p6 = new JPanel();
		p6.setBorder(new TitledBorder(UIManager
				.getBorder("TitledBorder.border"), "Preprocessing",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		p1.add(p6);
		p6.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		btn3 = new JButton("Stop-word Removal");
		btn3.setEnabled(false);
		p6.add(btn3);

		btn2 = new JButton("Tokenization");
		p6.add(btn2);
		btn2.setEnabled(false);

		btn4 = new JButton("Normalization");
		p6.add(btn4);
		btn4.setEnabled(false);

		btn5 = new JButton("Stemming");
		btn5.setEnabled(false);
		p1.add(btn5);

		JCheckBox chckbxArabicized = new JCheckBox("Solve Arabicized Words");
		chckbxArabicized.setEnabled(false);
		chckbxArabicized.setSelected(true);
		p1.add(chckbxArabicized);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(UIManager
				.getBorder("TitledBorder.border"), "Reduce Size",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		p1.add(panel_1);
		panel_1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		btn15 = new JButton("Remove Duplicates");
		btn15.setEnabled(false);
		panel_1.add(btn15);

		p8 = new JPanel();
		tabbedPane.addTab("Algorithm 2 - Larkey", null, p8, null);
		p8.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		btn7 = new JButton("Run");
		btn7.setEnabled(false);
		p8.add(btn7);

		btnViewOwnDataset_larkey = new JButton("View Own Dataset");
		btnViewOwnDataset_larkey.setEnabled(false);
		p8.add(btnViewOwnDataset_larkey);

		JPanel p9 = new JPanel();
		tabbedPane.addTab("Algorithm 3 - Khoja", null, p9, null);

		btn8 = new JButton("Run");
		btn8.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		});
		p9.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		btn8.setEnabled(false);
		p9.add(btn8);

		btnViewOwnDataset_khoja = new JButton("View Own Dataset");
		btnViewOwnDataset_khoja.setEnabled(false);
		p9.add(btnViewOwnDataset_khoja);

		JPanel panel = new JPanel();
		tabbedPane.addTab("Results", null, panel, null);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		btn14 = new JButton("Comparison");
		btn14.setEnabled(false);
		panel.add(btn14);

		JButton btnAccuracy = new JButton("Accuracy");
		btnAccuracy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		panel.add(btnAccuracy);

		p4 = new JPanel();
		tabbedPane.addTab("Export", null, p4, null);
		p4.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		chckbx3 = new JCheckBox("TXT");
		chckbx3.setEnabled(false);
		chckbx3.setSelected(true);
		p4.add(chckbx3);

		chckbx4 = new JCheckBox("DOC");
		chckbx4.setEnabled(false);
		p4.add(chckbx4);

		chckbx5 = new JCheckBox("XLS");
		chckbx5.setEnabled(false);
		p4.add(chckbx5);

		chckbx6 = new JCheckBox("PDF");
		chckbx6.setEnabled(false);
		p4.add(chckbx6);

		btn6 = new JButton("Export Tables");
		btn6.setEnabled(false);
		p4.add(btn6);

		btnExportProcessedDataset = new JButton("Export Processed Dataset");
		p4.add(btnExportProcessedDataset);

		JPanel panel_3 = new JPanel();
		tabbedPane.addTab("Terrier IR Platform", null, panel_3, null);
		panel_3.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		btnClearLastIndex = new JButton("Clear Last Index");
		panel_3.add(btnClearLastIndex);

		rdbtnAlgorithm1 = new JRadioButton("Algorithm 1");
		buttonGroup.add(rdbtnAlgorithm1);
		panel_3.add(rdbtnAlgorithm1);

		rdbtnAlgorithm2 = new JRadioButton("Algorithm 2");
		buttonGroup.add(rdbtnAlgorithm2);
		panel_3.add(rdbtnAlgorithm2);

		rdbtnAlgorithm3 = new JRadioButton("Algorithm 3");
		buttonGroup.add(rdbtnAlgorithm3);
		panel_3.add(rdbtnAlgorithm3);

		btnImportProcessedDataset = new JButton("Import Processed Dataset");
		panel_3.add(btnImportProcessedDataset);

		btnDesktopTerrier = new JButton("DesktopTerrier");
		panel_3.add(btnDesktopTerrier);

		tabbedPane_1 = new JTabbedPane(SwingConstants.TOP);
		tabbedPane_1.setBounds(6, 127, 391, 410);
		panel_2.add(tabbedPane_1);

		p2 = new JPanel();
		tabbedPane_1.addTab("Dataset", null, p2, null);
		p2.setLayout(new BorderLayout(0, 0));

		scrollPane1 = new JScrollPane();
		p2.add(scrollPane1, BorderLayout.CENTER);

		ep1 = new JEditorPane();
		scrollPane1.setViewportView(ep1);
		// scrollPane1.applyComponentOrientation(arabicOrientation.RIGHT_TO_LEFT);
		ep1.setAutoscrolls(true);
		// ep1.applyComponentOrientation(arabicOrientation.RIGHT_TO_LEFT);
		// ep1.setLocale(new Locale("ar"));

		JPanel p10_1 = new JPanel();
		tabbedPane_1.addTab("Stop-words", null, p10_1, null);
		p10_1.setLayout(new BorderLayout(0, 0));

		scrollPane2 = new JScrollPane();
		p10_1.add(scrollPane2, BorderLayout.CENTER);

		ep2 = new JEditorPane();
		ep2.setLocale(new Locale("ar"));
		scrollPane2.setViewportView(ep2);

		panel_5 = new JPanel();
		tabbedPane_1.addTab("Arabized words", null, panel_5, null);
		panel_5.setLayout(new BorderLayout(0, 0));

		scrollPane_2 = new JScrollPane();
		panel_5.add(scrollPane_2);

		editorPaneArabizedWords = new JEditorPane();
		scrollPane_2.setViewportView(editorPaneArabizedWords);

		// p3 = new JPanel();
		// tabbedPane_1.addTab("Output", null, p3, null);
		// p3.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(6, 548, 789, 110);
		panel_2.add(scrollPane_1);

		tp1 = new JTextPane();
		scrollPane_1.setViewportView(tp1);

		tabbedPane_2 = new JTabbedPane(SwingConstants.TOP);
		tabbedPane_2.setBounds(404, 127, 391, 410);
		panel_2.add(tabbedPane_2);

		scrollPane = new JScrollPane();
		tabbedPane_2.addTab("Output", null, scrollPane, null);

		table1 = new JTable();
		scrollPane.setViewportView(table1);
		table1.setCellSelectionEnabled(true);
		table1.setColumnSelectionAllowed(true);
		table1.setFillsViewportHeight(true);
		table1.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		table1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table1.setModel(new DefaultTableModel(new Object[5000][3],
				new String[] { "Token", "Normalized word", "Stemmed word" }) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			@SuppressWarnings("rawtypes")
			Class[] columnTypes = new Class[] { String.class, String.class,
					String.class };

			public Class<?> getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
		});

		JScrollPane scrollPane_3 = new JScrollPane();
		tabbedPane_2.addTab("Own Stop-words List", null, scrollPane_3, null);

		editorPane_OwnDataset = new JEditorPane();
		scrollPane_3.setViewportView(editorPane_OwnDataset);
		table1.getColumnModel().getColumn(0).setPreferredWidth(113);
		table1.getColumnModel().getColumn(1).setPreferredWidth(95);
		table1.getColumnModel().getColumn(2).setPreferredWidth(97);

		progressBar = new JProgressBar();
		progressBar.setBounds(16, 708, 802, 21);
		frmStandardArabicStemmer.getContentPane().add(progressBar);

//		lblStatus = new JLabel("Status");
//		lblStatus.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
//		lblStatus.setForeground(UIManager.getColor("Label.disabledForeground"));
//		lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
//		lblStatus.setBackground(Color.WHITE);
//		lblStatus.setFont(new Font("Tahoma", Font.PLAIN, 14));
//		lblStatus.setBounds(20, 708, 148, 21);
//		frmStandardArabicStemmer.getContentPane().add(lblStatus);

		toolBar = new JToolBar();
		toolBar.setRollover(true);
		toolBar.setBounds(10, 0, 98, 21);
		frmStandardArabicStemmer.getContentPane().add(toolBar);

		btn9 = new JButton("New");
		toolBar.add(btn9);

		btn10 = new JButton("Clear");
		toolBar.add(btn10);

		cb1 = new JComboBox();
		cb1.setModel(new DefaultComboBoxModel(new String[] { "Please Choose",
				"Algorithm 1", "Algorithm 2", "Algorithm 3" }));
		cb1.setBounds(229, 0, 140, 20);
		frmStandardArabicStemmer.getContentPane().add(cb1);

		JLabel lblChooseAlgorithm = new JLabel("Choose Algorithm");
		lblChooseAlgorithm.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblChooseAlgorithm.setBounds(118, 0, 109, 20);
		frmStandardArabicStemmer.getContentPane().add(lblChooseAlgorithm);

		JLabel lblChooseEncoding = new JLabel("Choose Encoding");
		lblChooseEncoding.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblChooseEncoding.setBounds(379, 0, 98, 20);
		frmStandardArabicStemmer.getContentPane().add(lblChooseEncoding);

		JComboBox cb2 = new JComboBox();
		cb2.setModel(new DefaultComboBoxModel(
				new String[] { "UTF-8", "CP-1256" }));
		cb2.setBounds(487, 0, 88, 20);
		frmStandardArabicStemmer.getContentPane().add(cb2);

		menuBar = new JMenuBar();
		frmStandardArabicStemmer.setJMenuBar(menuBar);

		mnNewMenu1 = new JMenu("Options");
		menuBar.add(mnNewMenu1);

		mntmNew = new JMenuItem("New");
		mnNewMenu1.add(mntmNew);

		mntmSave = new JMenuItem("Save");
		mnNewMenu1.add(mntmSave);

		mntmExit = new JMenuItem("Exit");
		mnNewMenu1.add(mntmExit);

		mnNewMenu2 = new JMenu("Edit");
		menuBar.add(mnNewMenu2);

		mntmCopy = new JMenuItem("Copy");
		mnNewMenu2.add(mntmCopy);

		mntmPaste = new JMenuItem("Paste");
		mnNewMenu2.add(mntmPaste);

		mnNewMenu3 = new JMenu("Help");
		menuBar.add(mnNewMenu3);

		mntmDocs = new JMenuItem("Docs");
		mnNewMenu3.add(mntmDocs);

		mntmAbout = new JMenuItem("About");
		mnNewMenu3.add(mntmAbout);

	}
}

class handler implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == GUI.cb1) {
			GUI.index = GUI.cb1.getSelectedIndex();
			if (GUI.index == 1) {
				GUI.btn2.setEnabled(true);
				GUI.btn3.setEnabled(true);
				GUI.btn4.setEnabled(false);
				GUI.btn5.setEnabled(false);
				GUI.btn7.setEnabled(false);
				GUI.btnViewOwnDataset_larkey.setEnabled(false);
				// GUI.btn12.setEnabled(true);
				// GUI.btn13.setEnabled(true);
				GUI.btn8.setEnabled(false);
				GUI.btnViewOwnDataset_khoja.setEnabled(false);

			} else if (GUI.index == 2) {
				GUI.btn2.setEnabled(false);
				GUI.btn3.setEnabled(false);
				GUI.btn4.setEnabled(false);
				GUI.btn5.setEnabled(false);
				// GUI.btn12.setEnabled(false);
				GUI.btn15.setEnabled(false);
				GUI.btn7.setEnabled(true);
				GUI.btnViewOwnDataset_larkey.setEnabled(true);
				GUI.btn8.setEnabled(false);
			} else if (GUI.index == 3) {
				GUI.btn2.setEnabled(false);
				GUI.btn3.setEnabled(false);
				GUI.btn4.setEnabled(false);
				GUI.btn5.setEnabled(false);
				// GUI.btn12.setEnabled(false);
				GUI.btn15.setEnabled(false);
				GUI.btn7.setEnabled(false);
				GUI.btn8.setEnabled(true);
				GUI.btnViewOwnDataset_khoja.setEnabled(true);
			}
		} else if (event.getSource() == GUI.btn1) {
			Reader reader = null;
			try {
				reader = new FileReader(new File(GUI.DATASET_PATH));
				GUI.ep1.read(reader, "");
				GUI.tabbedPane_1.setSelectedIndex(0);
			} catch (Exception exp) {
				exp.printStackTrace();
			} finally {
				try {
					reader.close();
					GUI.progressBar.setValue(10);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if (event.getSource() == GUI.btn11) {

			Reader reader = null;
			try {

				reader = new FileReader(new File(GUI.STOP_PATH));
				GUI.ep2.read(reader, "");
				GUI.tabbedPane_1.setSelectedIndex(1);
			} catch (Exception exp) {
				exp.printStackTrace();
			} finally {
				try {
					reader.close();
					GUI.progressBar.setValue(15);
				} catch (Exception e) {
				}
			}
		} else if (event.getSource() == GUI.btn12) {
			try {
				GUI.datasetList = Algo1.readDataSetFile(GUI.DATASET_PATH);
				GUI.doc.insertString(GUI.doc.getLength(),
						"\nRead Dataset, Size  : " + GUI.datasetList.size(),
						null);
				GUI.tabbedPane.setSelectedIndex(1);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		} else if (event.getSource() == GUI.btn13) {
			try {
				GUI.stopsetList = Algo1.readStopWordFile(GUI.STOP_PATH);
				GUI.doc.insertString(GUI.doc.getLength(),
						"\nRead Stopset, Size  : " + GUI.stopsetList.size(),
						null);
				GUI.tabbedPane.setSelectedIndex(2);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		} else if (event.getSource() == GUI.btn14) {
			GUI.tabbedPane.setSelectedIndex(6);
			GUI.frmShowResults.setVisible(true);
		}
		//
		else if (event.getSource() == GUI.btn3) {

			if (GUI.datasetList == null) {
				GUI.infoBox("Please Load Dataset", "Loading Error");
			} else {
				try {
					GUI.freeDataSetList = Algo1.removeStopWords(
							GUI.stopsetList, GUI.datasetList);
					// GUI.freeDataSetList =
					// Algo1.removeStopWords(GUI.stopsetList,
					// GUI.normalizedDataset);
					GUI.doc.insertString(GUI.doc.getLength(),
							"\nRemove Stop-words, Match  : "
									+ Algo1.stopWordCounter, null);
					GUI.btn2.setEnabled(true);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}

		} else if (event.getSource() == GUI.btn2) {
			GUI.btn4.setEnabled(true);
			GUI.tabbedPane_2.setSelectedIndex(0);
			java.util.Iterator<String> itr = GUI.freeDataSetList.iterator();
			int y = 0;
			while (itr.hasNext()) {
				String element = itr.next();
				GUI.table1.setValueAt(element, y++, 0);
			}
			try {
				GUI.doc.insertString(GUI.doc.getLength(),
						"\nTokenize Dataset, size: " + y, null);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}

		} else if (event.getSource() == GUI.btn4) {
			// GUI.normalizedDataset =
			// Algo1.normalizeArabicDataSet(GUI.freeDataSetList); mhmd edit
			GUI.normalizedDataset = Algo1
					.normalizeArabicDataSet(GUI.datasetList);
			String[] str = GUI.normalizedDataset.split(" ");
			for (int j = 0; j < str.length; j++) {

				GUI.table1.setValueAt(str[j], j, 1);
			}
			try {
				GUI.doc.insertString(GUI.doc.getLength(),
						"\nNormalize Dataset, new size: " + str.length, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
			GUI.btn5.setEnabled(true);
		} else if (event.getSource() == GUI.btn5) {

			try {
				GUI.processedDataset = Algo1
						.stemmingArabicTokens(GUI.normalizedDataset);
				GUI.output_str_array = GUI.processedDataset.toString().split(
						" ");
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}

			for (int j = 0; j < GUI.output_str_array.length; j++) {
				GUI.table1.setValueAt(GUI.output_str_array[j], j, 2);
				GUI.table2.setValueAt(GUI.output_str_array[j], j, 0);
			}
			try {
				int x = GUI.output_str_array.length;
				GUI.doc.insertString(GUI.doc.getLength(),
						"\nStem Dataset, size by Algorithm 1 : " + --x, null);
				GUI.tabbedPane.setSelectedIndex(5);
			} catch (Exception e) {
				e.printStackTrace();
			}
			GUI.btn6.setEnabled(true);
			// GUI.chckbx3.setEnabled(true);
			// GUI.chckbx4.setEnabled(true);
			// GUI.chckbx5.setEnabled(true);
			// GUI.chckbx6.setEnabled(true);
			GUI.btn14.setEnabled(true);
			GUI.btn15.setEnabled(true);
		}
		// summarization
		else if (event.getSource() == GUI.btn15) {

			// list is some List of Strings
			List<String> stringList = Arrays.asList(GUI.output_str_array);
			// remove duplication beside maintain order using linked hash set
			Set<String> s = new LinkedHashSet<String>(stringList);
			java.util.Iterator<String> itr = s.iterator();
			int y = 0;
			while (itr.hasNext()) {
				String element = itr.next();
				GUI.table2.setValueAt(element, y++, 3);
			}

			try {
				GUI.doc.insertString(GUI.doc.getLength(),
						"\nDuplicates summarization, size: " + s.size(), null);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}

			// ShereenKhojaStemmer
		} else if (event.getSource() == GUI.btn8) {

			if (GUI.datasetList == null) {
				GUI.infoBox("Please Load Dataset", "Loading Error Algo3");
			} else {
				Algo3Stemmer Stemmer = new Algo3Stemmer();
				GUI.datasetList_copy = Algo1.readDataSetFile(GUI.DATASET_PATH);
				java.util.Iterator<String> itr_algo3 = GUI.datasetList_copy
						.iterator();
				int counter = 0;
				StringBuilder stringBuilder = new StringBuilder();
				try {
					while (itr_algo3.hasNext()) {
						String element = itr_algo3.next();
						String root = Stemmer.stemWord(element);
						GUI.table2.setValueAt(root, counter++, 2);
						stringBuilder.append(root + " ");
					}

					GUI.doc.insertString(GUI.doc.getLength(),
							"\nStem Dataset, size by Algorithm 3 : " + counter,
							null);
					String finalString = stringBuilder.toString();
					FileUtils.writeStringToFile(new File(
							"output/output_algorithm3.txt"), finalString);
					GUI.doc.insertString(
							GUI.doc.getLength(),
							"\nWrite Processed Dataset to output/output_algorithm3.txt ",
							null);
				} catch (Exception e) {
					e.printStackTrace();
				}

				GUI.btn14.setEnabled(true);
			}
			// LarkeyALgorithm and MotazSaad implementation
		} else if (event.getSource() == GUI.btn7) {

			if (GUI.datasetList == null) {
				GUI.infoBox("Please Load Dataset", "Loading Error Algo2");
			} else {
				GUI.datasetList_copy = Algo1.readDataSetFile(GUI.DATASET_PATH);
				java.util.Iterator<String> itr_algo2 = GUI.datasetList_copy
						.iterator();
				int counter = 0;
				StringBuilder stringBuilder = new StringBuilder();
				while (itr_algo2.hasNext()) {
					String element = itr_algo2.next();
					// System.out.println("Word: " + element + "  Root: " +
					// Stemmer.stemWord(element));
					String root = ArLightStemmerLucene.lightStem(element);
					GUI.table2.setValueAt(root, counter++, 1);
					stringBuilder.append(root + " ");

				}
				String finalString = stringBuilder.toString();
				GUI.btn14.setEnabled(true);
				try {
					FileUtils.writeStringToFile(new File(
							"output/output_algorithm2.txt"), finalString);
					GUI.doc.insertString(GUI.doc.getLength(),
							"\nStem Dataset, size by Algorithm 2 : " + counter,
							null);
					GUI.doc.insertString(
							GUI.doc.getLength(),
							"\nWrite Processed Dataset to output/output_algorithm2.txt ",
							null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
		// export
		else if (event.getSource() == GUI.btn6) {
			try {
				GUI.export(GUI.table2, "Table_All_Algorithms.txt");
				GUI.export(GUI.table1, "Table_Algorithm1.txt");
				GUI.doc.insertString(GUI.doc.getLength(),
						"\nExport Results Tables to output txt Files", null);
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else if (event.getSource() == GUI.btnDesktopTerrier) {
			DesktopTerrier desktopTerrier = new DesktopTerrier();
			desktopTerrier.makeVisible();
		} else if (event.getSource() == GUI.rdbtnLightStopwordList) {
			GUI.STOP_PATH = "resources/stop-words/light-stop-words.txt";
		} else if (event.getSource() == GUI.rdbtnIntensiveStopwordList) {
			GUI.STOP_PATH = "resources/stop-words/intensive-stop-words.txt";
		} else if (event.getSource() == GUI.rdbtnTestDataset) {
			GUI.DATASET_PATH = "resources/dataset/test-dataset.txt";
		} else if (event.getSource() == GUI.rdbtnOsac) {
			GUI.DATASET_PATH = "resources/dataset/OSAC.txt";
		} else if (event.getSource() == GUI.rdbtnBbc) {
			GUI.DATASET_PATH = "resources/dataset/BBC.txt";
		} else if (event.getSource() == GUI.rdbtnCnn) {
			GUI.DATASET_PATH = "resources/dataset/CNN.txt";
		} else if (event.getSource() == GUI.btnViewOwnDataset_larkey) {
			GUI.STOP_PATH_OWN = "algo2_Files/stopwords.txt";
			Reader reader = null;
			try {
				reader = new FileReader(new File(GUI.STOP_PATH_OWN));
				GUI.editorPane_OwnDataset.read(reader, "");
				reader.close();
				GUI.progressBar.setValue(50);
				GUI.tabbedPane_2.setSelectedIndex(1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (event.getSource() == GUI.btnViewOwnDataset_khoja) {
			GUI.STOP_PATH_OWN = "algo3_Files/stopwords.txt";
			Reader reader = null;
			try {
				reader = new FileReader(new File(GUI.STOP_PATH_OWN));
				GUI.editorPane_OwnDataset.read(reader, "");
				reader.close();
				GUI.progressBar.setValue(70);
				GUI.tabbedPane_2.setSelectedIndex(1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (event.getSource() == GUI.btnClearLastIndex) {
			try {
				File del = new File("terrier/var/index");
				FileUtils.deleteDirectory(del);
				GUI.doc.insertString(GUI.doc.getLength(),
						"\nDelete Index Files @ " + del, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (event.getSource() == GUI.rdbtnAlgorithm1) {
			GUI.output_selection_Type = new File("output/output_algorithm1.txt");
			GUI.input_selection_Type = new File(
					"terrier/doc/input_algorithm1.txt");

		} else if (event.getSource() == GUI.rdbtnAlgorithm2) {
			GUI.output_selection_Type = new File("output/output_algorithm2.txt");
			GUI.input_selection_Type = new File(
					"terrier/doc/input_algorithm2.txt");

		} else if (event.getSource() == GUI.rdbtnAlgorithm3) {
			GUI.output_selection_Type = new File("output/output_algorithm3.txt");
			GUI.input_selection_Type = new File(
					"terrier/doc/input_algorithm3.txt");

		} else if (event.getSource() == GUI.btnExportProcessedDataset) {
			try {
				Algo1.writeOutput(GUI.processedDataset);
				GUI.tabbedPane.setSelectedIndex(7);
				GUI.doc.insertString(
						GUI.doc.getLength(),
						"\nWrite output of Algorithm 1 to output/output_algorithm1.txt",
						null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (event.getSource() == GUI.btnImportProcessedDataset) {
			try {
				File doc_path = new File("terrier/doc");
				FileUtils.cleanDirectory(doc_path);
				GUI.doc.insertString(GUI.doc.getLength(), "\nClean " + "\""
						+ doc_path + "\"" + " path", null);
				FileUtils.copyFile(GUI.output_selection_Type,
						GUI.input_selection_Type);
				GUI.doc.insertString(GUI.doc.getLength(), ", Copy " + "\""
						+ GUI.output_selection_Type + "\"" + " to " + "\""
						+ doc_path + "\"", null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}