package view.clerk;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import com.formdev.flatlaf.FlatIntelliJLaf;

import bean.Food;
import bean.Order;
import bean.User;
import builder.Director;
import builder.KFCMealBuilder;
import builder.Meal;
import builder.MealBuilder;
import builder.WallaceMealBuilder;
import service.FoodService;
import service.MemberShipService;
import service.OrderService;
import serviceimpl.FoodServiceImpl;
import serviceimpl.MembershipServiceImpl;
import serviceimpl.OrderServiceImpl;
import strategy.MemberShip;
import strategy.Strategy;
import util.ConstUtil;
import view.util.FoodTable;
import view.util.ImagePanel;
import view.util.JTextFieldHintListener;
import view.util.MyTabbedPaneUI;
import view.util.TableCellListener;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.event.MouseAdapter;

public class ClerkFrame extends JFrame {
	
	private static final long serialVersionUID = 1L;
	private Director director;//?????????
	private Vector<Vector<Object>> rowData=new Vector<Vector<Object>>();
    FoodService foodService = new FoodServiceImpl();
    OrderService orderService = new OrderServiceImpl();
    //???????????????????????????????????????
    private List<Food> kfcFoods = new ArrayList<>();
    private List<Food> wallaceFoods = new ArrayList<>();
    
    int RowSize=0;
    Meal meal=new Meal();//?????????
//    List<Food> orderedFoods=new ArrayList<>();
    
    private float primaryPrice;
    private Strategy strategy;//????????????
    private MemberShipService memberShipService=new MembershipServiceImpl();
	
	private ImagePanel contentPane;
	private JTextField total;
	private JTable table;//??????
	private JTable kfcTable;
	private JTable wallaceTable;
	private JTabbedPane tabbedPane;
	private JCheckBox cbx;
	User user;
	
	//??????????????????????????????????????????
//	private Vector<String> columnNames= new Vector<>();//order
	private DefaultTableModel orderTableModel ;
	private JTextField textField;

	/**
	 * Create the frame.
	 */
	public ClerkFrame(User user) {
		this.user=user;
//		setName("MainFrame");
		setTitle("????????????");
		//??????????????????
		FlatIntelliJLaf.setup();
		UIManager.put( "Component.focusWidth", 1 );
		
		kfcFoods = foodService.queryAllFood("KFC");
		wallaceFoods = foodService.queryAllFood("Wallace");
		
		setBounds(100, 100, 1200, 640);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnNewMenu = new JMenu("????????????");
		menuBar.add(mnNewMenu);
		
		JMenuItem changeInfo = new JMenuItem("??????");
		changeInfo.setIcon(new ImageIcon(ConstUtil.imgPath+"edit.png"));
		changeInfo.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {//?????????JMenuItem???????????????mouseClicked??????
				super.mouseClicked(e);
				new ChangeInfoFrame(user).setVisible(true);
			}
		});
		mnNewMenu.add(changeInfo);

		contentPane = new ImagePanel(1200,640,ConstUtil.imgPath+"bg.jpg");
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		tabbedPane.setUI(new MyTabbedPaneUI());
		
		
		JScrollPane orderDeatailSP = new JScrollPane();
		orderDeatailSP.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "\u8BA2\u5355", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		orderDeatailSP.setToolTipText("");
		
		JLabel lblNewLabel_1 = new JLabel("??????:");
		
		total = new JTextField("0.0");
		total.setEditable(false);
		total.setColumns(10);
		
		JButton confirm = new JButton("??????");
		confirm.setIcon(new ImageIcon(ConstUtil.imgPath+"confirm.png"));
		confirm.addActionListener(new ActionListener() {//?????????????????????????????????
			public void actionPerformed(ActionEvent e) {
				//????????????
				List<Food> orderedKfcFoods = new ArrayList<>();
				List<Food> orderedWallaceFoods = new ArrayList<>();
				for (Vector<Object> vector:rowData) {
					if("KFC".equals(vector.get(2))) {
						for(int i=0;i<(int)vector.get(3);i++) {//??????
							orderedKfcFoods.add(new Food((String)vector.get(1),ConstUtil.foodMapKFC.get((String)vector.get(1)),"KFC",(float)vector.get(4)));
						}
					}else {
						for(int i=0;i<(int)vector.get(3);i++) {//??????
							orderedWallaceFoods.add(new Food((String)vector.get(1),ConstUtil.foodMapWallace.get((String)vector.get(1)),"Wallace",(float)vector.get(4)));
						}
					}
				}
				if(orderedKfcFoods.size()>0) {
					MealBuilder builder = new KFCMealBuilder();
					director=new Director(builder);
					for(Food food:director.construct(orderedKfcFoods).getFoods()) {
						meal.addFood(food);
					}
				}
				if(orderedWallaceFoods.size()>0) {
					MealBuilder builder = new WallaceMealBuilder();
					director=new Director(builder);
					for (Food food : director.construct(orderedWallaceFoods).getFoods()) {
						meal.addFood(food);
					}
				}
				meal.setPrice(Float.valueOf(total.getText()));
				JOptionPane.showConfirmDialog(contentPane, "???????????????", "??????",JOptionPane.PLAIN_MESSAGE);
				Long ctm =  System.currentTimeMillis();//?????????????????????????????????
				saveReceipt(ctm);//??????
				if(generateOrder(ctm)) {
					clearOrder();
				}
			}
		});
		
		JButton cancle = new JButton("??????");
		cancle.setIcon(new ImageIcon(ConstUtil.imgPath+"cancle.png"));
		cancle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearOrder();
			}
		});
		
		JLabel hui = new JLabel("??????:");
		
		textField = new JTextField();
		textField.setToolTipText("");
		textField.setColumns(10);
		textField.addFocusListener(new JTextFieldHintListener(textField, ConstUtil.menberHintText));
		
		JButton btnSearchMember = new JButton("??????");
		btnSearchMember.setIcon(new ImageIcon(ConstUtil.imgPath+"search.png"));
		btnSearchMember.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String str = textField.getText().toString();
				if(str.length()!=8&&str.length()!=11) {
					JOptionPane.showMessageDialog(contentPane, "???????????????8??????11???", "??????",JOptionPane.WARNING_MESSAGE); 
				}
				if(str.length()==11) {
					MemberShip mem = memberShipService.queryMemberShipByPhone(str);
					if(mem!=null) {
						setStrategy(mem);
					}else {
						JOptionPane.showMessageDialog(contentPane, "?????????????????????", "??????",JOptionPane.WARNING_MESSAGE); 
					}
				}else if(str.length()==8) {
					MemberShip mem = memberShipService.queryMemberShipByCard(str);
					if(mem!=null) {
						setStrategy(mem);
					}else {
						JOptionPane.showMessageDialog(contentPane, "?????????????????????", "??????",JOptionPane.WARNING_MESSAGE); 
					}
				}
				if(strategy!=null) {
					JOptionPane.showMessageDialog(contentPane, "?????????????????????", "??????", JOptionPane.PLAIN_MESSAGE);
					float price = Float.parseFloat(total.getText().toString());
					total.setText(String.valueOf(strategy.discount(price)));
				}
			}
		});
		
		cbx = new JCheckBox("??????");
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, 550, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(hui)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(textField, GroupLayout.PREFERRED_SIZE, 231, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnSearchMember)))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(lblNewLabel_1)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(total, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addGap(18)
							.addComponent(cbx)
							.addPreferredGap(ComponentPlacement.RELATED, 216, Short.MAX_VALUE)
							.addComponent(cancle)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(confirm)
							.addGap(14))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(orderDeatailSP, GroupLayout.DEFAULT_SIZE, 614, Short.MAX_VALUE)
							.addContainerGap())))
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGap(12)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
						.addComponent(orderDeatailSP, GroupLayout.PREFERRED_SIZE, 458, GroupLayout.PREFERRED_SIZE)
						.addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, 492, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewLabel_1, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
						.addComponent(total, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE)
						.addComponent(hui, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
						.addComponent(textField, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnSearchMember)
						.addComponent(cbx)
						.addComponent(confirm)
						.addComponent(cancle))
					.addContainerGap())
		);
		DefaultTableCellRenderer tcr = new DefaultTableCellRenderer();//??????????????????
        tcr.setHorizontalAlignment(JLabel.CENTER);  //?????????????????????
		
		table = new JTable();
		table.setName("orderTbModel");
		table.setShowVerticalLines(true);
		table.setShowHorizontalLines(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		orderTableModel = new DefaultTableModel(null,ConstUtil.order_columnNames) {
				//?????????????????????
				@Override
				public boolean isCellEditable(int row,int column){
					if(column == 3){
						return true;
					}
					return false;		
				}
		};
		table.setModel(orderTableModel);
		table.setRowHeight(30);
		table.setDefaultRenderer(Object.class,tcr);
		table.getColumnModel().getColumn(0).setMinWidth(60);
		table.getColumnModel().getColumn(1).setMinWidth(190);
		table.getColumnModel().getColumn(2).setMinWidth(150);
		table.getColumnModel().getColumn(3).setMinWidth(100);
		table.getColumnModel().getColumn(4).setResizable(false);
		table.setBackground(new Color(255, 255, 255));
		//????????????????????????
		table.getTableHeader().setReorderingAllowed(false) ;
		Action action = new AbstractAction(){//?????????????????????
            public void actionPerformed(ActionEvent e){
                TableCellListener tcl = (TableCellListener)e.getSource();
                //??????????????????????????????
                int row=tcl.getRow(),column=tcl.getColumn();
                rowData.get(row).set(column, tcl.getNewValue());//??????rowData??????
                totalChange();
            }
        };
        TableCellListener tcl = new TableCellListener(table, action);
		fitTableColumns(table);

		orderDeatailSP.setViewportView(table);
		
		JScrollPane kfcSP = new JScrollPane();
		kfcSP.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		tabbedPane.addTab("KFC", null, kfcSP, null);
		tabbedPane.setForegroundAt(0, Color.BLACK);
		tabbedPane.setBackgroundAt(0, Color.WHITE);
        
		kfcTable = new FoodTable(kfcFoods);
		kfcTable.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				if(e.getClickCount()==2) {
					int row = kfcTable.getSelectedRow();
					Food food = kfcFoods.get(row);
					Vector<Object> newRow = new Vector<>();
					boolean flag=true;//?????????????????????
					int i=0;
					for(;i<rowData.size();i++) {
						if((food.getName()).equals(rowData.get(i).get(1))){
							flag=false;//????????????
							break;
						}
					}
					if(flag){
						newRow.add(++RowSize);
						newRow.add(food.getName());
						newRow.add("KFC");
						newRow.add(1);
						newRow.add(food.getPrice());
						orderTableModel.addRow(newRow);
						rowData.add(newRow);
					}
					else {
						int number=(int)rowData.get(i).get(0);//??????
						int num =(int) rowData.get(i).get(3) + 1;//??????+1
						newRow.add(number);
						rowData.remove(i);
						newRow.add(food.getName());
						newRow.add("KFC");
						newRow.add(num);
						newRow.add(food.getPrice());
						orderTableModel.removeRow(number-1);
						orderTableModel.insertRow(number-1,newRow);
						rowData.add(newRow);
					}
					totalChange();
				}
			}
		});
		fitTableColumns(kfcTable);
		kfcTable.setDefaultRenderer(Object.class, tcr);
		kfcSP.setViewportView(kfcTable);
//		
		JScrollPane wallaceSP = new JScrollPane();
		tabbedPane.addTab("Wallace", null, wallaceSP, null);
		tabbedPane.setBackgroundAt(1, Color.WHITE);
//		
		wallaceTable = new FoodTable(wallaceFoods);
		wallaceTable.addMouseListener(new MouseListener() {
					
					@Override
					public void mouseReleased(MouseEvent e) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void mousePressed(MouseEvent e) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void mouseExited(MouseEvent e) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void mouseEntered(MouseEvent e) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void mouseClicked(MouseEvent e) {
						// TODO Auto-generated method stub
						if(e.getClickCount()==2) {
							int row = wallaceTable.getSelectedRow();
							Food food = wallaceFoods.get(row);
							Vector<Object> newRow = new Vector<>();
							boolean flag=true;//?????????????????????
							int i=0;
							for(;i<rowData.size();i++) {
								if((food.getName()).equals(rowData.get(i).get(1))){
									flag=false;//????????????
									break;
								}
							}
							if(flag){
								newRow.add(++RowSize);
								newRow.add(food.getName());
								newRow.add("Wallace");
								newRow.add(1);
								newRow.add(food.getPrice());
								orderTableModel.addRow(newRow);
								rowData.add(newRow);
							}
							else {
								int number=(int)rowData.get(i).get(0);//??????
								int num =(int) rowData.get(i).get(3) + 1;//??????+1
								newRow.add(number);
								rowData.remove(i);
								newRow.add(food.getName());
								newRow.add("Wallace");
								newRow.add(num);
								newRow.add(food.getPrice());
								orderTableModel.removeRow(number-1);
								orderTableModel.insertRow(number-1,newRow);
								rowData.add(newRow);
							}
							totalChange();
						}
					}
				});
		fitTableColumns(wallaceTable);
		wallaceTable.setDefaultRenderer(Object.class, tcr);
		wallaceSP.setViewportView(wallaceTable);
		contentPane.setLayout(gl_contentPane);
		//????????????
		setLocationRelativeTo(null);
	}
	
	// ??????????????????????????????????????????
    private static void fitTableColumns(JTable myTable){
         JTableHeader header = myTable.getTableHeader();
         int rowCount = myTable.getRowCount();
         Enumeration columns = myTable.getColumnModel().getColumns();
         while(columns.hasMoreElements()){
             TableColumn column = (TableColumn)columns.nextElement();
             int col = header.getColumnModel().getColumnIndex(column.getIdentifier());
             int width = (int)header.getDefaultRenderer().getTableCellRendererComponent
             (myTable, column.getIdentifier(), false, false, -1, col).getPreferredSize().getWidth();
             for(int row = 0; row < rowCount; row++){
                 int preferedWidth = (int)myTable.getCellRenderer(row, col).getTableCellRendererComponent
                 (myTable, myTable.getValueAt(row, col), false, false, row, col).getPreferredSize().getWidth();
                 width = Math.max(width, preferedWidth);
             }
             header.setResizingColumn(column); // ???????????????
             column.setWidth(width+myTable.getIntercellSpacing().width);
         }
    }
    //????????????????????????????????????
//    public static void setTableColumnCentered(JTable table,List<Integer> colList) {    
//    	//???????????????????????????
//		DefaultTableCellRenderer render = new DefaultTableCellRenderer();
//		render.setHorizontalAlignment(SwingConstants.CENTER);
//		for (Integer integer : colList) {
//			table.getTableHeader().getColumnModel().getColumn(integer).setCellRenderer(render);
//		}
//    }

    
    //???????????????
	public void totalChange() {
		float price = 0f;
		for(int i=0;i<rowData.size();i++) {
			price+=Integer.valueOf(rowData.get(i).get(3).toString())*(float)rowData.get(i).get(4);
		}
		if(strategy==null) {
			primaryPrice= price;
			total.setText(String.valueOf(price));
		}
		else {
			total.setText(String.valueOf(strategy.discount(price)));
		}
			
	}
	//???????????????????????????
	public void clearOrder() {
		primaryPrice=0f;
		meal=new Meal();
		strategy=null;
		textField.setText(ConstUtil.menberHintText);
		rowData.clear();
		RowSize=0;
//		kfcFoods.clear();
//		wallaceFoods.clear();
		cbx.setSelected(false);
		total.setText("0.0");
		//????????????
		for (int i = orderTableModel.getRowCount() - 1; i >= 0; i--) {
		    orderTableModel.removeRow(i);
		}
	}
	
	public void setStrategy(Strategy strategy) {
		this.strategy=strategy;
	}
	public void buildFood(List<Food> foods) {
		MealBuilder builder = new KFCMealBuilder();
		director=new Director(builder);
		for (Food food : director.construct(foods).getFoods()) {
			meal.addFood(food);
		}
	}
	
	public void saveReceipt(Long ctm) {
		//?????????
		if(cbx.isSelected()) {
			Date date = new Date(ctm);
			PrintWriter out;
			try {
				out = new PrintWriter(ConstUtil.receiptPath+String.valueOf(ctm)+".txt");
				out.println("????????????:"+ctm+"\t\t??????:"+ConstUtil.sdf.format(date));
				for (Vector<Object> vector:rowData) {
					for (Iterator iterator = vector.iterator(); iterator.hasNext();) {
						out.print( iterator.next()+"\t\t");
					}
					out.println();
				}
				out.print("??????:"+primaryPrice+"\t\t??????:"+total.getText().toString());
				out.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public boolean generateOrder(Long ctm) {//??????????????????
		String number = String.valueOf(ctm);
		String date = ConstUtil.sdf.format(new Date(ctm)).toString();
		String content = "|";
		for (Vector<Object> vector:rowData) {
			content+=(String)vector.get(2)+"-"+(String)vector.get(1)+"*"+String.valueOf((int)vector.get(3))+"|";
		}
		float price = Float.valueOf(total.getText().toString());
		return orderService.insert(new Order(number,content,date,price));
	}
}