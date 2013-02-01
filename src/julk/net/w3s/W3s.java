package julk.net.w3s;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * This class is only a graphic user interface that
 * allows a user to make searches.
 * 
 * It can be enterely rewriten and discarded without
 * danger.
 * @author Mhyst
 *
 */
public class W3s {
	public JTextField txtURL;
	public JTextField txtTerms;
	public JLabel lDepth;
	public JSlider sldDepth;
	public JLabel lMax;
	public JSlider sldMax;	
	public JCheckBox chkExtOnly;
	public JCheckBox chkIntOnly;
	public JCheckBox chkCutBranches;
	public JCheckBox chkPriority;
	public JTextArea txtOut;
	public JButton btn;
	public JFrame frm;
	private LinksExplorerWorker task;
	//private boolean canceled = false;
	private JTextField txtWorking;
	
	public final String version = "1.0";

	public W3s() {
		btn = new JButton("Search");
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				//if (e.getActionCommand().equals("Search")) {
					frm.setCursor(Cursor
							.getPredefinedCursor(Cursor.WAIT_CURSOR));
					btn.setEnabled(false);
					//btn.setText("Cancel");
					txtOut.setText("");
					try {
						txtOut.append("W3S V" + version + " by Mhyst\n");
						txtOut
								.append("----------------------------------------\n");
						txtOut.append("Searching: " + txtURL.getText() + "\n");
						txtOut.append("Search terms: " + txtTerms.getText()
								+ "\n");
						txtOut.append("This operation might take some minutes. Please wait.\n");
						frm.update(frm.getGraphics());
						task = new LinksExplorerWorker();
						
						task.execute();
						/*LinkExplorer.recursiveSearch(txtURL.getText(),getCadenas(),frm,txtOut,chkIntOnly.isSelected(),chkExtOnly.isSelected(),sldDepth.getValue(),sldMax.getValue());
						 txtOut.append("\nSearching finished. Now you can browse the salida.htm file.\n");
						 //txtOut.append("Le recomiendo abrirlo en su navegador habitual.\n");
						 txtOut.append("Thank you.\n");
						 //String ejemplo = "<a target=_top href=\"/url?sa=p&pref=ig&pval=3&q=http://www.google.es/ig%3Fhl%3Des&usg=AFQjCNH25tZa9pK_qlkku2QH55RuJCamdw\">iGoogle</a> | <a href=\"https://www.google.com/accounts/ManageAccount\">Mi cuenta</a> | <a target=_top href=\"/accounts/ClearSID?continue=http://www.google.com/accounts/Logout%3Fcontinue%3Dhttp://www.google.es/\">Salir</a>";
						 //LinkSearch.procesarLinea(ejemplo);
						 String curDir = System.getProperty("user.dir");
						 //Runtime.getRuntime().exec("explorer "+curDir+"\\salida.htm");
						 Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler "+curDir+"\\salida.htm");
						 */
					} catch (Exception e1) {
						txtOut.append(e1.getMessage());
					}
					/*btn.setEnabled(true);
					 frm.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));*/
				/*} else {
					canceled = true;
					task.cancel(true);
					btn.setText("Search");
				}*/
			}
		});
		
		frm = new JFrame("W3S v"+version+" by Mhyst");

		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		
		frm.setSize(740,340);
		frm.setLocationRelativeTo(null);
		//frm.setTitle("Canal "+channel);
		frm.setResizable(false);

		JPanel jp0, jp1, jp2, jp3;
		jp0 = new JPanel(new BorderLayout());
		jp1 = new JPanel(new BorderLayout());
		jp2 = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		jp3 = new JPanel();
		jp1.add(jp2,BorderLayout.NORTH);
		jp1.add(jp3,BorderLayout.CENTER);
		jp0.add(jp1,BorderLayout.CENTER);
		JPanel pn, pe, pw, ps;
		pn = new JPanel();
		pn.setSize(400,10);
		pe = new JPanel();
		pe.setSize(10,400);
		pw = new JPanel();
		pw.setSize(10,140);
		ps = new JPanel();
		ps.setSize(400,10);
		jp0.add(pn, BorderLayout.NORTH);
		jp0.add(pe, BorderLayout.EAST);
		jp0.add(pw, BorderLayout.WEST);
		jp0.add(ps, BorderLayout.SOUTH);
		frm.setContentPane(jp0);
		c.gridx=0; c.gridy=0;
		c.weightx=1; c.weighty=1;
		c.fill=GridBagConstraints.HORIZONTAL;

		jp2.add(new JLabel("URL:"),c);
		txtURL = new JTextField(40);
		txtURL.setToolTipText("Enter a correct URL. Example: http://www.searchlores.org");
		
		c.gridx=1; c.gridy=0;
		c.weightx=4; c.weighty=1;
		c.fill=GridBagConstraints.HORIZONTAL;
		jp2.add(txtURL,c);
		
		c.gridx=0; c.gridy=1;
		c.weightx=1; c.weighty=1;
		c.fill=GridBagConstraints.HORIZONTAL;
		jp2.add(new JLabel("Search terms:"),c);
		
		txtTerms = new JTextField(40);
		txtTerms.setToolTipText("Enter one or more words. (Comma separated)");
		c.gridx=1; c.gridy=1;
		c.weightx=4; c.weighty=1;
		c.fill=GridBagConstraints.HORIZONTAL;
		jp2.add(txtTerms,c);

		c.gridx=0; c.gridy=2;
		c.weightx=1; c.weighty=1;
		c.fill=GridBagConstraints.HORIZONTAL;
		lDepth = new JLabel("Max depth: 2");
		jp2.add(lDepth,c);
		
		sldDepth = new JSlider(1,19,2);
		sldDepth.setToolTipText("Control search depth. This may help to shorten the search time.");
		sldDepth.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				lDepth.setText("Max depth: "+sldDepth.getValue());
			}
		});
		c.gridx=1; c.gridy=2;
		c.weightx=4; c.weighty=1;
		c.fill=GridBagConstraints.HORIZONTAL;
		jp2.add(sldDepth,c);
		
		c.gridx=0; c.gridy=3;
		c.weightx=1; c.weighty=1;
		c.fill=GridBagConstraints.HORIZONTAL;
		lMax = new JLabel("Max entries limit: 0");
		jp2.add(lMax,c);
		
		sldMax = new JSlider(0,1000,0);
		sldMax.setToolTipText("Highest numer of results. Use it to avoid very long searches. Select '0' for no limit.");
		sldMax.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				lMax.setText("Max entries limit: "+sldMax.getValue());
			}
		});
		c.gridx=1; c.gridy=3;
		c.weightx=4; c.weighty=1;
		c.fill=GridBagConstraints.HORIZONTAL;
		jp2.add(sldMax,c);
		
		c.gridx=0; c.gridy=4;
		c.weightx=1; c.weighty=1;
		c.fill=GridBagConstraints.HORIZONTAL;
		jp2.add(new JLabel("Working on:"),c);
		
		txtWorking = new JTextField();
		txtWorking.setEditable(false);
		
		c.gridx=1; c.gridy=4;
		c.weightx=4; c.weighty=1;
		c.fill=GridBagConstraints.HORIZONTAL;
		jp2.add(txtWorking,c);

		chkExtOnly = new JCheckBox("Follow external links only",false);
		chkExtOnly.setToolTipText("Only process links starting with \"http://\".");
		chkIntOnly = new JCheckBox("Follow internal links only",false);
		chkIntOnly.setToolTipText("Only process links not starting with \"http://\".");
		chkCutBranches = new JCheckBox("Cut low branches",false);
		chkCutBranches.setToolTipText("Doesn't take account of links from pages not matching search terms.");
		chkPriority = new JCheckBox("Priorize",true);
		chkPriority.setToolTipText("The more ocurrences in the father, the more priority for its links");
		jp3.add(chkIntOnly);
		jp3.add(chkExtOnly);
		jp3.add(chkCutBranches);
		jp3.add(chkPriority);
		jp3.add(btn);
		txtOut = new JTextArea(10,40);
		txtOut.setToolTipText("Here you will see what's doing the program. Each dot represents a page being processed.");
		txtOut.setEditable(false);
		txtOut.setAutoscrolls(true);
		JScrollPane sp = new JScrollPane(txtOut);
		
		
		txtOut.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		
		jp1.add(sp,BorderLayout.SOUTH);
		//frm.pack();
	}
	
	private String[] getCadenas() {
		String cadena = txtTerms.getText();
		StringTokenizer st = new StringTokenizer(cadena,",");
		String[] cadenas = new String[st.countTokens()];
		for(int i = 0; i < cadenas.length; i++) {
			cadenas[i] = st.nextToken();
		}
		return cadenas;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		W3s w3s = new W3s();
		w3s.frm.setVisible(true);
	}

	class LinksExplorerWorker extends SwingWorker<List<String>, String> {
		
		public List<String> doInBackground() {
			LinksExplorer le = null;
			Properties setup = new Properties();
			setup.setProperty("DEPTH",""+sldDepth.getValue());
			setup.setProperty("MAXENTRIES",""+sldMax.getValue());
			setup.setProperty("ONLYINTERNAL", chkIntOnly.isSelected() ? "TRUE" : "FALSE");
			setup.setProperty("ONLYEXTERNAL", chkExtOnly.isSelected() ? "TRUE" : "FALSE");
			setup.setProperty("PRIORIZE", chkPriority.isSelected() ? "TRUE" : "FALSE");
			setup.setProperty("CUTBRANCHES", chkCutBranches.isSelected() ? "TRUE" : "FALSE");
			try {				
				le = new LinksExplorer(txtURL.getText(),getCadenas(),setup);
				le.recursiveSearch(this);
			} catch (Exception e) {	
				if (le != null) {
					PrintWriter out = le.getOut();
					try {
						out.close();
					} catch (Exception e2) {}
				}
				txtOut.append(e.getMessage());
				e.printStackTrace();
			}
			return null;
	    }
		
		protected void process(List<String> chunks) {
			for (String s :  chunks) {
				if (s.startsWith("ERR:")) {
					txtOut.append(s.substring(4));
				} else {
					txtWorking.setText(s);
				}
			}
		}
		
		public void doPublish(String s) {
			publish(s);
		}

		protected void done() {
			try {
				txtOut.append("\nSearching finished. Now you can browse the salida.htm file.\n");
				txtOut.append("Thank you.\n");
				String curDir = System.getProperty("user.dir");
				
				Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler "+curDir+"\\salida.htm");
			} catch (Exception e) {
				txtOut.append(e.getMessage());
			}
			btn.setEnabled(true);
			frm.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}	
	}	
}
