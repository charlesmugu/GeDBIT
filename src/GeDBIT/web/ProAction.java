package GeDBIT.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.opensymphony.xwork2.ActionSupport;

@SuppressWarnings("serial")
public class ProAction extends ActionSupport {
	private String prostr; //输入DNA串
	private Map<String,String> result=new LinkedHashMap<String,String>();   //用于JSP显示
	private String resdna="";//返回的总串
	private int pronum=0; //结果数量
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
	public int getPronum(){
		return pronum;
	}
	public void setPronum(int pronum){
		this.pronum=pronum;
	}
	
	public String getProstr(){
		return prostr;
	}
	public void setProstr(String prostr){
		this.prostr=prostr;
	}
	
	 public Map<String, String> getResult() {
		  return result;
	 }

	 public void setResult(Map<String, String> result) {
		  this.result = result;
	 }
	 

	 public void setRes()
	 {
		 int count=1;
		 int evpg=10; //每一页的数量   用于演示可修改
		 String res;
		 Pattern patt=Pattern.compile("fragment:.+source"); //这里生成txt文件
		 @SuppressWarnings("unused")
        Pattern pattend=Pattern.compile(" source: >([^'])+");
		 try												//这里读取txt文件
		 {
			 File file=new File("D:/data/protein/presult.txt");
			 if(file.isFile()&&file.exists())
			 {
				 InputStreamReader read=new InputStreamReader(new FileInputStream(file),"GBK");
				 BufferedReader buffreader=new BufferedReader(read);
				 String lineTxt=null;
				 String tpo;
				 buffreader.readLine();
				 while((lineTxt=buffreader.readLine())!=null)
				 {
					 Matcher matcher=patt.matcher(lineTxt);
					 System.out.println("OPEN OK");
					 while(matcher.find())
					 {
						 res=matcher.group().toString().replaceFirst("fragment: ","");
						 res=res.replaceFirst(" source","");
						 tpo=lineTxt.replace(res,"<strong style='color:blue'>"+res+"</strong>");
						 tpo=tpo.replace("source:","<strong style='color:red'>source:</strong>");
						 tpo=tpo.replace("fragment:","<strong style='color:red'>fragment:</strong>");
						 tpo=tpo.replace("offset:","<strong style='color:red'>offset:</strong>");
						 if((count>(page*evpg))&&(count<=(page+1)*evpg))
						 {
							 result.put(String.valueOf(count), tpo);
						 }
						 System.out.println(res);
						 ++count;
					 }
				 }
				 allpage=((count-1)/evpg)-1;
				 pronum=count-1;
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
		 String[] tp=prostr.split(",");
		 prostr=tp[0];
		 r=Double.valueOf(tp[1]);
		 new ProteinQuery().runQuery(r);
		 setRes();
		 System.out.println(prostr);
		 allcount=100000;
		 time=0.001;
		 prostr=prostr+","+r;
		 return SUCCESS;
		 
	 }

}
