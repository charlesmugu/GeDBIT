package GeDBIT.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.opensymphony.xwork2.ActionSupport;

@SuppressWarnings("serial")
public class DnaAction extends ActionSupport {
	private String dnastr; //输入DNA串
	private Map<String,String> result=new LinkedHashMap<String,String>();   //用于JSP显示
	private String resdna="";//返回的总串
	private int dnanum=0; //结果数量
	private double time=0.0001;  //搜索时间
	private int allcount=10000; //搜索数据库的大小
	private int page=0;         //显示当前页数
	private int allpage=0;      //总页数
	private double r=0;
	public void setR(double r)
	{
		this.r=r;
	}
	public double getR()
	{
		return r;
	}
	public void setAllpage(int allpage)
	{
		this.allpage=allpage;
	}
	public int getAllpage()
	{
		return allpage;
	}
	public void setPage(int page)
	{
		this.page=page;
	}
	public int getPage()
	{
		return page;
	}
	public void setTime(double time)
	{
		this.time=time;
	}
	public double getTime()
	{
		return time;
	}
	
	public void setAllcount(int allcount)
	{
		this.allcount=allcount;
	}
	public int getAllcount()
	{
		return allcount;
	}
	public String getResdna(){
		return resdna;
	}
	public void setResdna(String resdna){
		this.resdna=resdna;
	}
	public int getDnanum(){
		return dnanum;
	}
	public void setDnanum(int dnanum){
		this.dnanum=dnanum;
	}
	
	public String getDnastr(){
		return dnastr;
	}
	public void setDnastr(String dnastr){
		this.dnastr=dnastr;
	}
	
	 public Map<String, String> getResult() {
		  return result;
	 }

	 public void setResult(Map<String, String> result) {
		  this.result = result;
	 }
	 
	 @SuppressWarnings("unused")
    public void setSour()
	 {
		 int count=0;
		 String res;
		 Pattern patt=Pattern.compile("data:.+"); //这里生成txt文件
	      String fileR="d:/data/dna/arab/arab1_out.con"; // 文件路径   
	      String fileS = "D:/data/dna/arab/arab1.con"; // 文件路径   
		 
		 try												//这里读取txt文件
		 {
			 count=0;
			 File file=new File(fileS);
			 if(file.isFile()&&file.exists())
			 {
				 InputStreamReader read=new InputStreamReader(new FileInputStream(file),"GBK");
				 BufferedReader buffreader=new BufferedReader(read);
				 String lineTxt=null;
				 lineTxt=buffreader.readLine();
				 FileWriter fileW=new FileWriter(fileR);
				 fileW.write(lineTxt+" ");
				 fileW.write(dnastr+"\n");
				 try {
					 while((lineTxt=buffreader.readLine())!=null)
					 {
							fileW.write(lineTxt+"\n");
					 }
				 }
				 catch (IOException e1) {
						// TODO Auto-generated catch block
						System.out.println("Writer Error");
						e1.printStackTrace();
						
					}
				 buffreader.close();
				fileW.flush();
				fileW.close();
				read.close();
			 }
			 else
				 System.out.println("Open Error");
		 }
		 catch(Exception e)
		 {
			 System.out.println("Error");
		 }
	 }
	 public void setRes()
	 {
		 int count=1;
		 int evpg=10; //每一页的数量 
		 String res;
		 Pattern patt=Pattern.compile("fragment:.+source"); 
		 
		 try												//这里读取txt文件
		 {
			 File file=new File("D:/data/dna/dresult.txt");
			 if(file.isFile()&&file.exists())
			 {
				 InputStreamReader read=new InputStreamReader(new FileInputStream(file),"GBK");
				 BufferedReader buffreader=new BufferedReader(read);
				 String lineTxt=null;
				 buffreader.readLine();
				 while((lineTxt=buffreader.readLine())!=null)
				 {
					 Matcher matcher=patt.matcher(lineTxt);
					 System.out.println("OPEN OK");
					 while(matcher.find())
					 {
						 res=matcher.group().toString().replaceFirst("fragment: ","");
						 res=res.replaceFirst(" source","");
						 if((count>(page*evpg))&&(count<=(page+1)*evpg))
						 {
							 result.put(String.valueOf(count), res);
						 }
							
						 System.out.println(res);
						 ++count;
					 }
				 }
				 allpage=((count-1)/evpg)-1;
				 System.out.println(page);
				 System.out.println(allpage);
				 dnanum=count-1;
				 buffreader.close();
				 read.close();
			 }
			 else
				 System.out.println("Open Error");
		 }
		 catch(Exception e)
		 {
			 System.out.println("Error");
		 }
	 }
	 public String execute() throws Exception {
		 String[] tp=dnastr.split(",");
		 dnastr=tp[0];
		 r=Double.valueOf(tp[1]);
		 setSour();
	     new DNAQuery().runQuery(r);
	    
		 setRes();
		 dnastr=dnastr+","+r;
		 allcount=101;
		 time=0.001;

		 return SUCCESS;
		 
	 }

}
