package gdev;

import java.awt.event.WindowAdapter;
import java.awt.*;
import java.text.ParseException;
import java.util.*;
import javax.swing.JPanel;
import javax.swing.JButton;

import java.awt.event.*;
import java.awt.Frame;

import gdev.gawt.GSimpleForm;
import gdev.gbalancer.GProcessedForm;
import gdev.gbalancer.GViewBalancer;
import gdev.gen.AssignValueException;

class MyFrame extends Frame implements ActionListener
{
	private static final long serialVersionUID = 1L;
	GViewBalancer m_viewBalancer;
	JPanel m_mainPanel=new JPanel();
	JButton m_buttonNext=new JButton(">>");;
	JButton m_buttonPrev=new JButton("<<");

	Vector m_vProcessedFormList;
	int m_iCounter = -1;

	public MyFrame()
	{
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
		});
		this.setBackground(Color.lightGray);
		setLayout(null);
		//m_mainPanel.setLayout(new BorderLayout());
		m_mainPanel.setLayout(null);
		this.m_mainPanel.setBounds(10,40,980,630);
		this.m_mainPanel.setBackground(Color.darkGray);
		add(m_mainPanel);

		this.m_buttonPrev.setBounds(460,680,60,24);
		this.add(this.m_buttonPrev);
		this.m_buttonPrev.addActionListener(this);
		this.m_buttonPrev.setEnabled(false);

		this.m_buttonNext.setBounds(560,680,60,24);
		this.add(this.m_buttonNext);
		this.m_buttonNext.addActionListener(this);
	}
	public void build(GViewBalancer vw) throws ParseException, AssignValueException
	{
		m_viewBalancer = vw;
		m_vProcessedFormList = m_viewBalancer.getProcessedFormList();
		onNext();
	}
	private void onNext() throws ParseException, AssignValueException
	{
		m_iCounter++;
		showForm();
		if(m_iCounter+1>=m_vProcessedFormList.size())
			m_buttonNext.setEnabled(false);
		if(m_iCounter>0)
			m_buttonPrev.setEnabled(true);
	}
	private void onPrev() throws ParseException, AssignValueException
	{
		m_iCounter--;
		showForm();
		if(m_iCounter<=0)
			m_buttonPrev.setEnabled(false);
		m_buttonNext.setEnabled(true);
	}
	private void showForm() throws ParseException, AssignValueException
	{
		this.m_mainPanel.removeAll();
		if(m_iCounter>=m_vProcessedFormList.size())
			return;
		GProcessedForm objPF = (GProcessedForm)m_vProcessedFormList.elementAt(m_iCounter);

		GSimpleForm form = new GSimpleForm(objPF,null);
		m_mainPanel.add(form);
		form.setBounds(new Rectangle(new Point(10,10),form.getPreferredSize()));
		String strCaption = "Combination "+(m_iCounter+1)+" of " + m_vProcessedFormList.size();
		this.setTitle(strCaption);

		//unfortunately the combobox arrow is not appearing, I do not nkow why, this is a dirty fix only
		this.setSize(this.getSize().width+1,this.getSize().height+1);
		this.setSize(this.getSize().width-1,this.getSize().height-1);
	}
	final Component c=this;
	public void actionPerformed(ActionEvent e)
	{
		try{
			if(e.getSource().equals(m_buttonNext))
			{
				onNext();
			}
			else if(e.getSource().equals(m_buttonPrev))
			{
				onPrev();
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}
public class GClientModule
{
	/* public static void main(String args[]) throws Exception
    {
        //load input data
        BufferedReader in = new BufferedReader(new FileReader("field_list_new.txt"));
        String dataS = "", buff = "";
        while (buff != null)
        {
            dataS += buff;
            buff = in.readLine();
        }

        //clas jdomParser is a custom class which has usefull method to parser text data an to build a JDOM doc
        // JDOM is a public library to manage XML docs in java (see www.jdom.org).
        // Main JDOM class is the Element class. The XML doc is modeled in jdom as a tree of ELement class.
        // So a JDOM document has
        // a root Element and Each Element must have more child Elemnts. Also an Element has attributes.

        Element doc = jdomParser.readXML(dataS).getRootElement();

        // Now we will create the JDOM output root (see form.dtd ). This root Element must be passed
        // to the viewBalancer instance

        Element rootView = new Element("FORM");

        //We begin to load input parameters
        Dimension endWinDim = new Dimension(Integer.parseInt(doc.getAttributeValue("WIDTH")),
                                            Integer.parseInt(doc.getAttributeValue("HEIGHT")));

        // "to" mean "Object Type", this is an internal value out of this project scope,
        // There is just one TO in each form.
        Integer toRoot = new Integer(1);

        // viewBalancer need a graphic object in order to measure char width a height. But viewBallancer will not buil swing
        // components

        boolean SHOW_IN_FRAME = true;

        Frame frm = null;
        if(SHOW_IN_FRAME)
        {
            frm = new MyFrame();
            frm.setBounds(10, 10, 1000, 720);
            frm.setVisible(true);
        }
        else
        {
            frm= new Frame();
            frm.setBounds(200,100,600,400);
            frm.addWindowListener(new WindowAdapter()
            {
                public void windowClosing(WindowEvent e)
                {
                    System.exit(0);
                }
            });
            frm.setVisible(true);
        }

        Graphics2D gr2D = (Graphics2D) frm.getGraphics();
        FontRenderContext frc = gr2D.getFontRenderContext();
        //Font font= new  Font("Dialog",  Font.PLAIN,  8);//gr2D.getFont();
        Font font = gr2D.getFont();

        // Now instatiating viewBalancer. Parameter INPUT_FORM is needed in order to know that output doc will be used
        // as a form (could be used as an html report). At this project scope we always will fix this value to INPUT_FORM

        GViewBalancer vw = new GViewBalancer(endWinDim,font, frc,true);

        Iterator itr = doc.getChildren("GROUP").iterator();
        //System.out.println("aaaaa....");
        while (itr.hasNext())
        {
            Element nodeGroup = (Element) itr.next();
            int grOrder = nodeGroup.getAttributeValue("ORDER") == null ? 0 :
                Integer.parseInt(nodeGroup.getAttributeValue("ORDER"));
            String grLabel = nodeGroup.getAttributeValue("LABEL");

            int groupId = Integer.parseInt(nodeGroup.getAttributeValue("ID"));

            vw.addGroup(groupId, grLabel, grOrder);

            Iterator iF = nodeGroup.getChildren("FIELD").iterator();
            while (iF.hasNext())
            {
                Element nodeField = (Element) iF.next();
                int id = Integer.parseInt(nodeField.getAttributeValue("ID"));
                int type = Integer.parseInt(nodeField.getAttributeValue("TYPE"));
                int priority = nodeField.getAttributeValue("PRIORITY") == null ?
                    0 : Integer.parseInt(nodeField.getAttributeValue("PRIORITY"));
                int length = nodeField.getAttributeValue("LENGTH") == null ?
                    -1 : Integer.parseInt(nodeField.getAttributeValue("LENGTH"));
                int order = nodeField.getAttributeValue("ORDER") == null ? 0 :
                    Integer.parseInt(nodeField.getAttributeValue("ORDER"));

                Vector vValues = null;
                if (type == GConst.TM_ENUMERATED)
                {
                    vValues = new Vector();
                    Iterator itrVal = nodeField.getChildren("VAL").iterator();
                    while (itrVal.hasNext())
                    {
                        Element elVal = (Element) itrVal.next();
                        int valId = Integer.parseInt(elVal.getAttributeValue("ID"));
                        String strLabel = elVal.getAttributeValue("LABEL");
                        vValues.addElement(new GValue(valId,strLabel));
                    }
                }
                if (type != 0)
                { // "0" type is "table" type.
                    vw.addItem(groupId,
                               type,
                               new Integer(id).toString(),//id,
                               //id,
                               priority,
                               //null,
                               true,
                               //true,
                               nodeField.getAttributeValue("LABEL"),
                               length,
                               nodeField.getAttributeValue("COMMENT") != null,
                               order,
                               true,
                               vValues
                        );
                }
                else
                {
                    //table type are added to viewBalancer from other method
                    int rows = Integer.parseInt(nodeField.getAttributeValue("ROWS"));
                    Element table = (Element) nodeField.clone();
                    table.setName("TABLE");
                    table.setAttribute("ID_CONTEXT", "0");
                    Vector vColumns = new Vector();
                    Iterator iI = table.getChildren("ITEM").iterator();
                    while (iI.hasNext())
                    {
                        Element item = (Element) iI.next();
//                        adaptaField(item);
                        int colId = Integer.parseInt(item.getAttributeValue("ID"));
                        int columnPos = Integer.parseInt(item.getAttributeValue("COLUMN"));
                        String colLabel = item.getAttributeValue("LABEL");
                        int colType = Integer.parseInt(item.getAttributeValue("TYPE"));
                        vColumns.addElement(new GTableColumn(colId,columnPos,colLabel,colType));
                    }

                    vw.addTable(groupId,
                    			new Integer(id).toString(),
                                priority,
                                true,
                                nodeField.getAttributeValue("LABEL"),
                                order,true,rows,vColumns,null);
                }
            }
        }

        //viewBalancer can works with two policies. (see view balancer constructor method)
        vw.process(false);
        if(SHOW_IN_FRAME)
        {
            ((MyFrame)frm).build(vw);
        }
        else
        {
            JDialog dlg= new JDialog( frm,"TEST",true );
            GSimpleForm form = new GSimpleForm(vw.getBestResult(),null);
            dlg.setContentPane(form);
            dlg.pack();
            dlg.repaint();
            dlg.setLocation(200, 100);
            dlg.setVisible(true);
        }
    }

    public static void adaptaField( Element field )
    {
        int type = Integer.parseInt(field.getAttributeValue("TYPE"));
        field.setAttribute("ID_TM", String.valueOf(type));
    }*/
}
